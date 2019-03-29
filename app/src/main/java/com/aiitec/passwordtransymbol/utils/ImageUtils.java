package com.aiitec.passwordtransymbol.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.PostProcessor;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Author ailibin
 * @Version 1.0
 * Created on 2018/6/15
 * @effect 图像处理工具类
 */

public class ImageUtils {

    /**
     * 将一般视图转化为bitmap
     *
     * @param saveView
     * @return
     */
    public static Bitmap TransViewToBitmap(View saveView) {
        saveView.clearFocus();
        saveView.setPressed(false);
        boolean willNotCache = saveView.willNotCacheDrawing();
        saveView.setWillNotCacheDrawing(false);
        int color = saveView.getDrawingCacheBackgroundColor();
        saveView.setDrawingCacheBackgroundColor(0);
        if (color != 0) {
            saveView.destroyDrawingCache();
        }
        saveView.buildDrawingCache();
        Bitmap cacheBitmap = saveView.getDrawingCache();
        if (cacheBitmap == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
        saveView.destroyDrawingCache();
        saveView.setWillNotCacheDrawing(willNotCache);
        saveView.setDrawingCacheBackgroundColor(color);
        return bitmap;
    }

    /**
     * 判断照片角度
     *
     * @param path
     * @return
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转照片
     *
     * @param bitmap
     * @param degress
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }

    /**
     * 将ScrollView转化为bitmap
     *
     * @param scrollView
     * @return
     */
    public static Bitmap getBitmapByView(ScrollView scrollView) {
        int h = 0;
        Bitmap bitmap = null;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
        }
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        return bitmap;
    }

    /**
     * 将ImageView转为Drawable
     */
    public static Drawable getDrawable(Context context, int width, int height, ImageView.ScaleType scaleType) {
        ImageView imageView = new ImageView(context);
        imageView.setBackgroundColor(Color.parseColor("#e5e5e5"));
        if (scaleType == null) {
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            imageView.setScaleType(scaleType);
        }
        LinearLayout.LayoutParams params;
        if (width == 0 && height == 0) {
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        } else {
            params = new LinearLayout.LayoutParams(width, height);
        }
        imageView.setLayoutParams(params);
        //一般视图转化为drawable
        Drawable drawable = imageView.getDrawable();
        return drawable;
    }

    /**
     * 根据路径获取返回bitmap对象
     */
    public static Bitmap getBitmap(String imgPath) {
        // Get bitmap through image path
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = false;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;
        // Do not compress
        newOpts.inSampleSize = 1;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(imgPath, newOpts);
    }

    /**
     * 将资源文件转化为drawable
     *
     * @param context
     * @param resId
     * @return
     */
    public static Drawable getDrawableFromRes(Context context, int resId) {
        Drawable drawable = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(resId, null);
        } else {
            drawable = context.getResources().getDrawable(resId);
        }
        return drawable;
    }

    /**
     * 将资源图片转化成Drawable，再转化为Bitmap
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap drawableResToBitmap(Context context, int resId) {
        Drawable drawable = getDrawableFromRes(context, resId);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;

    }

    /**
     * 将drawable转化为bitmap对象
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * 获得圆角图片的方法
     *
     * @param bitmap
     * @param roundPx
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {


        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * 通过线程池创建圆角图片
     *
     * @param context
     * @param source
     * @param dp
     * @return
     */
//    private static Bitmap roundBitmap(Context context, Bitmap source, int dp) {
//        BitmapPool bitmapPool = Glide.get(context).getBitmapPool();
//        float mRadius = Resources.getSystem().getDisplayMetrics().density * dp;
//        if (source == null) {
//            return null;
//        }
//
//        int width = source.getWidth();
//        int height = source.getHeight();
//        Bitmap result = bitmapPool.get(width, height, Bitmap.Config.ARGB_8888);
//        if (result == null) {
//            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        }
//
//        Canvas canvas = new Canvas(result);
//        Paint paint = new Paint();
//        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
//        paint.setAntiAlias(true);
//        canvas.drawRoundRect(new RectF(0, 0, width, height), mRadius, mRadius, paint);
//        return result;
//    }

    /**
     * 获得带倒影的图片方法
     *
     * @param bitmap
     * @return
     */
    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
        final int reflectionGap = 4;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(bitmap,
                0, height / 2, width, height / 2, matrix, false);

        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(bitmap, 0, 0, null);
        Paint deafalutPaint = new Paint();
        canvas.drawRect(0, height, width, height + reflectionGap,
                deafalutPaint);

        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0,
                bitmap.getHeight(), 0, bitmapWithReflection.getHeight()
                + reflectionGap, 0xccffffff, 0x00ffffff, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        // Set the Transfer mode to be porter duff and destination yjb_scrollbar_animation_in_1
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        // Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
                + reflectionGap, paint);

        return bitmapWithReflection;
    }

    /**
     * 将xml布局文件转化为bigmap
     *
     * @param contentLayout
     * @return
     */
    public static Bitmap getViewBitmap(View contentLayout) {
        contentLayout.setDrawingCacheEnabled(true);
        contentLayout.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        contentLayout.layout(0, 0, contentLayout.getMeasuredWidth(),
                contentLayout.getMeasuredHeight());
        contentLayout.buildDrawingCache();
        Bitmap bitmap = contentLayout.getDrawingCache();
        return bitmap;
    }

    /**
     * 将服务器上的base64字符串转成图片对象
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 通过旧的图片路径获取到图片，然后压缩再保存到缓存路径，然后返回缓存路径
     *
     * @param context
     * @param filePath
     * @param compressSize 压缩大小 单位kb
     * @return
     */
    public static String getCompressFile(Context context, String filePath, long compressSize) {
        String fileName = "";
        int index = filePath.lastIndexOf("/");
        if (index > 0) {
            fileName = filePath.substring(index + 1, filePath.length()).replaceAll(" ", "");
        }
        String cachePath = getCacheDir(context);
        String cacheFilePath = cachePath + fileName;
        File file = new File(cacheFilePath);
        if (file.exists()) {
            //如果有缓存，直接返回缓存路径
            return cacheFilePath;
        }
        return bitmapToFile(context, compressImageFromFile(context, filePath), fileName, compressSize);
    }

    /**
     * 通过旧的图片路径获取到图片，然后压缩再保存到缓存路径，然后返回缓存路径
     *
     * @param context
     * @param filePath
     * @return
     */
    public static String getCompressFile(Context context, String filePath) {
        String fileName = "";
        int index = filePath.lastIndexOf("/");
        if (index > 0) {
            fileName = filePath.substring(index + 1, filePath.length()).replaceAll(" ", "");
        }
        String cachePath = getCacheDir(context);
        String cacheFilePath = cachePath + fileName;
        File file = new File(cacheFilePath);
        if (file.exists()) {
            //如果有缓存，直接返回缓存路径
            return cacheFilePath;
        }
        return bitmapToFile(context, compressImageFromFile(context, filePath), fileName);
    }

    @SuppressWarnings("deprecation")
    public static Bitmap compressImageFromFile(Context context, String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 只读边,不读内容
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = ScreenUtils.getScreenHeight(context);
        float ww = ScreenUtils.getScreenWidth(context);
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 1;
        }
        // 设置采样率
        newOpts.inSampleSize = be;
        // 该模式是默认的,可不设
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        // 同时设置才会有效
        newOpts.inPurgeable = true;
        // 。当系统内存不够时候图片自动被回收
        newOpts.inInputShareable = true;
        newOpts.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return bitmap;

    }

    /**
     * 使用android9.0 API28新特性的ImageDecoder来解码图片,遗弃掉BitmapFactory和BitmapFactory.Options
     *
     * @param context
     * @param srcPath
     * @return
     */
    public static Bitmap imageDecoderFromFile(Context context, String srcPath) {
        File file = new File(srcPath);
        ImageDecoder.Source source = ImageDecoder.createSource(file);

        //If the encoded image is an animated GIF or WEBP,
        // decodeDrawable will return an AnimatedImageDrawable.
        // To start its animation, call AnimatedImageDrawable#start:
        Drawable drawable;
        try {
            drawable = ImageDecoder.decodeDrawable(source, new ImageDecoder.OnHeaderDecodedListener() {
                @Override
                public void onHeaderDecoded(@NonNull ImageDecoder decoder, @NonNull ImageDecoder.ImageInfo info, @NonNull ImageDecoder.Source source) {

                    //设置解码后的图是宽高是原图宽高的一半
                    decoder.setTargetSampleSize(2);

                    decoder.setPostProcessor(new PostProcessor() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public int onPostProcess(@NonNull Canvas canvas) {
                            // This will create rounded corners.这可以创建圆角图片
                            Path path = new Path();
                            path.setFillType(Path.FillType.INVERSE_EVEN_ODD);
                            int width = canvas.getWidth();
                            int height = canvas.getHeight();
                            path.addRoundRect(0, 0, width, height, 20, 20, Path.Direction.CW);
                            Paint paint = new Paint();
                            paint.setAntiAlias(true);
                            paint.setColor(Color.TRANSPARENT);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                            canvas.drawPath(path, paint);
                            return PixelFormat.TRANSLUCENT;
                        }
                    });

                    //If the encoded image is incomplete or contains an error,
                    // or if an Exception occurs during decoding, a DecodeException will be thrown.
                    // In some cases, the ImageDecoder may have decoded part of the image.
                    // In order to display the partial image,
                    // an OnPartialImageListener must be passed to setOnPartialImageListener. For example:
                    //有些情况下，如果在解码的过程中解析出错，出现异常的情况下，相当于当前图片只解析了部分的图片资源，
                    //那么我们就需要展示部分已经解析出来的图片,可以通过设置以下监听
                    decoder.setOnPartialImageListener(new ImageDecoder.OnPartialImageListener() {
                        @Override
                        public boolean onPartialImage(@NonNull ImageDecoder.DecodeException e) {
                            // Returning true indicates to create a Drawable or Bitmap even
                            // if the whole image could not be decoded. Any remaining lines
                            // will be blank.
                            //返回值为true表明即使整张图片没有解码成功也会创建一个Drawable或者Bitmap。
                            //任何剩余行都将为空
                            return true;
                        }
                    });


                }
            });
            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = null;
        try {
            bitmap = ImageDecoder.decodeBitmap(source, listener);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;

    }

    /**
     * To change the default settings
     * For example, to create a sampled image with half the width and height of the original image,
     * call setTargetSampleSize(2) inside
     */
    static ImageDecoder.OnHeaderDecodedListener listener = new ImageDecoder.OnHeaderDecodedListener() {
        @Override
        public void onHeaderDecoded(ImageDecoder decoder, ImageDecoder.ImageInfo info, ImageDecoder.Source source) {
            decoder.setTargetSampleSize(2);
        }
    };

    public static String getCacheDir(Context context) {
        String cachePath = "";
        if (context.getExternalCacheDir() != null) {
            cachePath = context.getExternalCacheDir().getAbsolutePath() + "/";
        } else if (ScreenUtils.isSDCardEnable()) {
            cachePath = ScreenUtils.getSDCardPath() + "/" + context.getPackageName() + "/";
            File file = new File(cachePath);
            if (!file.exists()) {
                file.mkdir();
            }
        }
        return cachePath;
    }


    /**
     * 把bitmap 保存到缓存路径
     *
     * @param context
     * @param image
     * @param imageSize 图片大小单位kb
     * @return
     */
    public static String bitmapToFile(Context context, Bitmap image, String fileName, long imageSize) {

        String cachePath = getCacheDir(context);
        String filePath = cachePath + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        // 循环判断如果压缩后图片是否大于200kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > imageSize) {
            baos.reset();// 重置baos即清空baos
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            if (options <= 10) {
                options = options / 2 + 1;
            } else {
                // 每次都减少10
                options -= 10;
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] result = baos.toByteArray();
            try {
                fileOutputStream.write(result);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 把压缩后的数据baos存放到ByteArrayInputStream中
        return filePath;
    }


    /**
     * 把bitmap 保存到缓存路径
     *
     * @param context
     * @param image
     * @return
     */
    public static String ImageDecoderToFile(Context context, Bitmap image, String fileName) {

        String cachePath = getCacheDir(context);
        String filePath = cachePath + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        // 循环判断如果压缩后图片是否大于200kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > 300) {
            baos.reset();// 重置baos即清空baos
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            if (options <= 10) {
                options = options / 2 + 1;
            } else {
                // 每次都减少10
                options -= 10;
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] result = baos.toByteArray();
            try {
                fileOutputStream.write(result);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 把压缩后的数据baos存放到ByteArrayInputStream中
        return filePath;
    }

    /**
     * 把bitmap 保存到缓存路径
     *
     * @param context
     * @param image
     * @return
     */
    public static String bitmapToFile(Context context, Bitmap image, String fileName) {

        String cachePath = getCacheDir(context);
        String filePath = cachePath + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        // 循环判断如果压缩后图片是否大于200kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > 300) {
            baos.reset();// 重置baos即清空baos
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            if (options <= 10) {
                options = options / 2 + 1;
            } else {
                // 每次都减少10
                options -= 10;
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] result = baos.toByteArray();
            try {
                fileOutputStream.write(result);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 把压缩后的数据baos存放到ByteArrayInputStream中
        return filePath;
    }

    public static Bitmap safeDecodeStream(Context context, Uri uri,
                                          int minWidth, int minHeight) {
        if (context == null || uri == null) {
            return null;
        }
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        ContentResolver resolver = context.getContentResolver();
        try {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(
                    new BufferedInputStream(resolver.openInputStream(uri),
                            16 * 1024), null, options);
            int oldWidth = options.outWidth;
            int oldHeight = options.outHeight;
            int scale = 1;
            if (oldWidth > oldHeight && oldHeight > minHeight) {
                scale = (int) (oldHeight / (float) minHeight);
            } else if (oldWidth <= oldHeight && oldWidth > minWidth) {
                scale = (int) (oldWidth / (float) minWidth);
            }
            scale = scale < 1 ? 1 : scale;
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(new BufferedInputStream(
                    resolver.openInputStream(uri), 16 * 1024), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    // 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
    @SuppressLint("NewApi")
    public static String getPathByUri4kitkat(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // DownloadsProvider
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }// MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {// MediaStore
            // (and
            // general)
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {// File
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used yjb_scrollbar_animation_in_1 the query.
     * @param selectionArgs (Optional) Selection arguments used yjb_scrollbar_animation_in_1 the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
