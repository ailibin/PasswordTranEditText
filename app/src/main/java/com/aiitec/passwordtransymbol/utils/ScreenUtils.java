package com.aiitec.passwordtransymbol.utils;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;

/**
 * <pre>
 *     author: ailibin
 *     time  : 2019/1/26
 *     desc  : utils about screen
 * </pre>
 */
public final class ScreenUtils {

    public static DisplayMetrics metric = new DisplayMetrics();

    /**
     * 得到屏幕高度
     *
     * @param context
     * @return
     * @author shc DateTime 2014-11-20 下午3:04:44
     * @deprecated 已过时 建议使用 ScreenUtils 类
     */
    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    /**
     * 得到屏幕宽度
     *
     * @param context
     * @return
     * @author shc DateTime 2014-11-20 下午3:04:44
     * @deprecated 已过时 建议使用 ScreenUtils 类
     */
    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }

    /**
     * @param context
     * @param dpValue dp值
     * @return 像素值
     * @deprecated 已过时 建议使用 ScreenUtils 类
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * @param context
     * @param pxValue 像素值
     * @return dp值
     * @deprecated 已过时 建议使用 ScreenUtils 类
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {

        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 判断SDCard是否可用
     *
     * @return
     */
    public static boolean isSDCardEnable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);

    }

    /**
     * 获取SD卡路径
     *
     * @return
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator;
    }
}
