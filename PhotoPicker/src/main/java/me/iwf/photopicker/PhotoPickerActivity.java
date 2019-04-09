package me.iwf.photopicker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aiitec.toollibrary.ImageUtils;
import com.aiitec.toollibrary.ThreadPoolManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import me.iwf.photopicker.entity.Photo;
import me.iwf.photopicker.event.OnItemCheckListener;
import me.iwf.photopicker.fragment.ImagePagerFragment;
import me.iwf.photopicker.fragment.PhotoPickerFragment;
import me.iwf.photopicker.utils.StatusBarUtil;

import static android.widget.Toast.LENGTH_LONG;
import static me.iwf.photopicker.PhotoPicker.ADD_STATUS_BAR;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_COMPRESS_SIZE;
import static me.iwf.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static me.iwf.photopicker.PhotoPicker.EXTRA_IS_COMPRESS;
import static me.iwf.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static me.iwf.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS_OBJECT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static me.iwf.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;
import static me.iwf.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS_OBJECT;

/**
 * @author ailibin
 * @time 2018/11/29
 */
public class PhotoPickerActivity extends AppCompatActivity {

    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;
    private MenuItem menuDoneItem;

    private int maxCount = DEFAULT_MAX_COUNT;

    /**
     * to prevent multiple calls to inflate menu
     */
    private boolean menuIsInflated = false;

    private boolean showGif = false;
    private ArrayList<String> originalPhotos = null;
    private ArrayList<Photo> originalPhotosObject = null;
    private boolean isCompress = false;
    private int imageSize;
    private ExecutorService cachedThreadPool;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, false);
        //是否进行图片压缩
        isCompress = getIntent().getBooleanExtra(EXTRA_IS_COMPRESS, false);
        imageSize = getIntent().getIntExtra(EXTRA_COMPRESS_SIZE, 0);
        //线程模式为缓存模式,就是上一个线程使用完之后,执行下一个任务会重用上一个线程,而不会重新创建一个线程
        cachedThreadPool = ThreadPoolManager.newCachedThreadPool();
        boolean previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);
        boolean addStatusBar = getIntent().getBooleanExtra(ADD_STATUS_BAR, true);

        setShowGif(showGif);
        setContentView(R.layout.__picker_activity_photo_picker);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        StatusBarUtil.setStatusBarColor(this, R.color._picker_colorPrimary);
        setTitle(R.string.__picker_title);
        ActionBar actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionBar.setElevation(25);
        }

        maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        int columnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        originalPhotos = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS);
        originalPhotosObject = getIntent().getParcelableArrayListExtra(EXTRA_ORIGINAL_PHOTOS_OBJECT);

        if (originalPhotosObject != null && originalPhotosObject.size() > 0 && isCompress) {
            //进行压缩的而且传过来的数据不为空
            originalPhotos.clear();
            for (Photo photo : originalPhotosObject) {
                originalPhotos.add(photo.getPath());
            }
        }

        if (originalPhotos != null) {
            Log.e("ailibin", "originalPhotos: " + originalPhotos);
        }

        pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag("tag");
        if (pickerFragment == null) {
            pickerFragment = PhotoPickerFragment
                    .newInstance(showCamera, showGif, previewEnabled, columnNumber, maxCount, originalPhotos);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, pickerFragment, "tag")
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
            @Override
            public boolean onItemCheck(int position, Photo photo, final int selectedItemCount) {

                menuDoneItem.setEnabled(selectedItemCount > 0);

                if (maxCount <= 1) {
                    List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                    if (!photos.contains(photo.getPath())) {
                        photos.clear();
                        pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
                    }
                    return true;
                }

                if (selectedItemCount > maxCount) {
                    Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, maxCount),
                            LENGTH_LONG).show();
                    return false;
                }
                if (maxCount > 1) {
                    menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, selectedItemCount, maxCount));
                } else {
                    menuDoneItem.setTitle(getString(R.string.__picker_done));
                }
                //刷新底部的预览按钮界面
                if (pickerFragment != null) {
                    pickerFragment.toUpdatePreviewSelected(selectedItemCount);
                }
                return true;
            }
        });

    }

    /**
     * 刷新右上角按钮文案
     */
    public void updateTitleDoneItem() {
        if (menuIsInflated) {
            if (pickerFragment != null && pickerFragment.isResumed()) {
                List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                int size = photos == null ? 0 : photos.size();
                menuDoneItem.setEnabled(size > 0);
                if (maxCount > 1) {
                    menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, size, maxCount));
                } else {
                    menuDoneItem.setTitle(getString(R.string.__picker_done));
                }

            } else if (imagePagerFragment != null && imagePagerFragment.isResumed()) {
                //预览界面 完成总是可点的，没选就把默认当前图片
                menuDoneItem.setEnabled(true);
                List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                int size = photos == null ? 0 : photos.size();
                if (maxCount > 1) {
                    menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, size, maxCount));
                } else {
                    menuDoneItem.setTitle(getString(R.string.__picker_done));
                }
            }

        }
    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
    @Override
    public void onBackPressed() {
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            super.onBackPressed();
        }
    }


    public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        this.imagePagerFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.imagePagerFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!menuIsInflated) {
            getMenuInflater().inflate(R.menu.__picker_menu_picker, menu);
            menuDoneItem = menu.findItem(R.id.done);
            if (originalPhotos != null && originalPhotos.size() > 0) {
                menuDoneItem.setEnabled(true);
                menuDoneItem.setTitle(
                        getString(R.string.__picker_done_with_count, originalPhotos.size(), maxCount));
            } else {
                menuDoneItem.setEnabled(false);
            }
            menuIsInflated = true;
            return true;
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.done) {
            //点击完成按钮
            final Intent intent = new Intent();
            ArrayList<String> selectedPhotos = null;
            if (pickerFragment != null) {
                selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
            }

            //当在列表没有选择图片，又在详情界面时默认选择当前图片
            if (selectedPhotos.size() <= 0) {
                if (imagePagerFragment != null && imagePagerFragment.isResumed()) {
                    // 预览界面
                    selectedPhotos = imagePagerFragment.getCurrentPath();
                }
            }

            final int originSize = selectedPhotos.size();
            final ArrayList<String> tempFilePath = new ArrayList<>();
            final ArrayList<Photo> tempSelectedPhotos = new ArrayList<>();
            //获取当前目录下的photo对象数组,因为压缩之后的图片路径改变了,记录选中的图片状态就不起作用了,需要用到id
            List<Photo> photos = pickerFragment.getPhotoGridAdapter().getCurrentPhotos();
            tempFilePath.clear();
            tempSelectedPhotos.clear();
            for (Photo photo : photos) {
                if (selectedPhotos.contains(photo.getPath())) {
                    tempSelectedPhotos.add(photo);
                }
            }
            //需要压缩
            if (isCompress) {
                for (final String path : selectedPhotos) {
                    cachedThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (imageSize <= 0) {
                                String filePath = ImageUtils.getCompressFile(PhotoPickerActivity.this, path);
                                tempFilePath.add(filePath);
                            } else {
                                String filePath = ImageUtils.getCompressFile(PhotoPickerActivity.this, path, imageSize);
                                tempFilePath.add(filePath);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (tempFilePath.size() == originSize) {
                                        //压缩完了
                                        if (tempFilePath != null && tempFilePath.size() > 0) {
                                            intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, tempFilePath);
                                            intent.putParcelableArrayListExtra(KEY_SELECTED_PHOTOS_OBJECT, tempSelectedPhotos);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            } else {
                if (selectedPhotos != null && selectedPhotos.size() > 0) {
                    intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public PhotoPickerActivity getActivity() {
        return this;
    }

    public boolean isShowGif() {
        return showGif;
    }

    public void setShowGif(boolean showGif) {
        this.showGif = showGif;
    }
}
