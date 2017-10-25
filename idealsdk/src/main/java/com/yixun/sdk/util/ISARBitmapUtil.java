/**
 * 项目名称: IdealseeAR
 * 类名称:  BitmapUtil
 * 类描述:
 * 创建人: Ly
 * 创建时间: 2013-3-14 上午11:01:00
 * 修改人:
 * 修改时间:
 * 备注:
 *
 * @version
 */

package com.yixun.sdk.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;

import com.yixun.sdk.R;

/**
 * Bitmap decode, save.
 *
 * @author Ly
 */
public class ISARBitmapUtil {
    private ISARBitmapUtil() {

    }

    /**
     * save bitmap.
     *
     * @param bitmap bitmap
     * @return path
     */
    public static String saveBitmap(Bitmap bitmap) {
        return saveBitmap(bitmap, 100);
    }

    /**
     * save bitmap.
     *
     * @param bitmap bitmap
     * @param format format
     * @return path
     */
    public static String saveBitmap(Bitmap bitmap, CompressFormat format) {
        String dst = ISARConstants.APP_IMAGE_DIRECTORY + File.separator + ISARStringUtil.currentTimeToString() + ".png";
        saveBitmap(bitmap, dst, CompressFormat.PNG, 100);
        return dst;
    }

    /**
     * save bitmap.
     *
     * @param bitmap  bitmap
     * @param quality quality
     * @return path
     */
    public static String saveBitmap(Bitmap bitmap, int quality) {
        String dst = ISARConstants.APP_IMAGE_DIRECTORY + File.separator + ISARStringUtil.currentTimeToString() + ".jpg";
        saveBitmap(bitmap, dst, CompressFormat.JPEG, quality);
        return dst;
    }

    /**
     * 保存bitmap到指定位置.
     *
     * @param bitmap  需要保存的图片
     * @param path    保存路径
     * @param format  图片格式
     * @param quality 图片质量
     * @return true success
     */
    public static boolean saveBitmap(Bitmap bitmap, String path, CompressFormat format, int quality) {
        if (ISARFilesUtil.isSDCardExist()) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(format, quality, fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将bitmap转换为argb byte 数组.
     *
     * @param bmp bitmap
     * @return byte array
     */
    public static byte[] convertBitmap2argb(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        ByteBuffer bb = ByteBuffer.allocateDirect(width * height * 4);
        bb.order(ByteOrder.nativeOrder());
        IntBuffer ib = bb.asIntBuffer();

        for (int y = height - 1; y > -1; y--) {
            for (int x = 0; x < width; x++) {
                int px = bmp.getPixel(x, y);
                int alpha = (px & 0xFF000000) >> 24;
                int red = (px & 0xFF0000) >> 16;
                int green = (px & 0xFF00) >> 8;
                int blue = (px & 0xFF);
                ib.put((alpha << 24) | (blue << 16) | (green << 8) | (red));
            }
        }
        return bb.array();
    }

    /**
     * create bitmap with text.
     *
     * @param bgBmp    background bitmap
     * @param text     text
     * @param textSize text size
     * @param margin   margin
     * @param color    color
     * @return bitmap
     */
    public static Bitmap createTextBitmap(Bitmap bgBmp, String text, float textSize, int margin, int color) {
        return createTextBitmap(bgBmp, text, textSize, margin, color, Alignment.ALIGN_CENTER);
    }

    /**
     * 生成自动换行的bitmap.
     *
     * @param bgBmp background bitmap
     * @param text  text
     * @return bitmap
     */
    public static Bitmap createTextBitmap(Bitmap bgBmp, String text, float textSize, int margin, int color,
                                          Alignment align) {
        Bitmap newBitmap = Bitmap.createBitmap(bgBmp.getWidth(), bgBmp.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bgBmp, 0, 0, null);
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        textPaint.setColor(color);
        StaticLayout sl = new StaticLayout(text, textPaint, newBitmap.getWidth() - margin, align, 1.0f, 0.0f, true);
        int startx = margin / 2;
        int starty = (bgBmp.getHeight() - sl.getHeight()) / 2;
        if (starty < 0) {
            starty = bgBmp.getHeight() / 8;
        }
        canvas.translate(startx, starty);
        sl.draw(canvas);
        return newBitmap;
    }

    /**
     * 根据宽度缩放图片.
     *
     * @param bitmap bitmap
     * @param width  width
     * @return bitmap
     */
    public static Bitmap scaleBitmapByWidth(Bitmap bitmap, int width) {
        int rawwidth = bitmap.getWidth();
        int rawheight = bitmap.getHeight();
        int height = width * rawheight / rawwidth;
        Bitmap tmp = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return tmp;
    }

    /**
     * 根据高度生成按扭图片.
     *
     * @param bitmap bitmap
     * @param height height
     * @return bitmap
     */
    public static Bitmap scaleBitmapByHeight(Bitmap bitmap, int height) {
        int rawwidth = bitmap.getWidth();
        int rawheight = bitmap.getHeight();
        int width = height * rawwidth / rawheight;
        Bitmap tmp1 = Bitmap.createScaledBitmap(bitmap, width, height, false);
        // Matrix m = new Matrix();
        // m.postScale(1, -1);// 上下翻转
        // Bitmap tmp2 = Bitmap.createBitmap(tmp1, 0, 0, width, height, m, true);
        // tmp1.recycle();
        // tmp1 = null;
        return tmp1;
    }

    /**
     * 生成圆角图片.
     *
     * @param bitmap  bitmap
     * @param roundPX round
     * @return bitmap
     */
    public static Bitmap createRoundCornerBitmap(Bitmap bitmap, float roundPX) {
        if (roundPX > 0) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            Bitmap bitmap2 = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap2);

            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, width, height);
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawRoundRect(rectF, roundPX, roundPX, paint);

            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            bitmap.recycle();
            bitmap = null;
            return bitmap2;
        } else {
            return bitmap;
        }
    }

    /**
     * 生成圆角图片.
     *
     * @param path       path
     * @param scalewidth scale width
     * @param roundpx    round
     * @return bitmap
     */
    public static Bitmap createRoundCornerBitmap2(Context context, String path, int scalewidth, float roundpx) {
        Bitmap bitmap1 = decodeFile(context, path);
        int width = bitmap1.getWidth();
        int height = bitmap1.getHeight();
        int dstwidth;
        int startx;
        int starty;

        if (width > height) {
            dstwidth = height - 10; // 除去图片的边框
            startx = (width - dstwidth) / 2;
            starty = 5;
        } else {
            dstwidth = width - 10;// 除去图片的边框
            startx = 5;
            starty = (height - dstwidth) / 2;
        }
        Bitmap bitmap2 = Bitmap.createBitmap(bitmap1, startx, starty, dstwidth, dstwidth);
        Bitmap bitmap3 = Bitmap.createScaledBitmap(bitmap2, scalewidth, scalewidth, true);
        Bitmap bitmap4 = Bitmap.createBitmap(scalewidth, scalewidth, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap4);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, scalewidth, scalewidth);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, roundpx, roundpx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap3, rect, rect, paint);
        bitmap1.recycle();
        bitmap1 = null;
        bitmap2.recycle();
        bitmap2 = null;
        bitmap3.recycle();
        bitmap3 = null;
        return bitmap4;
    }

    /**
     * 根据路径生成bitmap.
     *
     * @param path path
     * @return bitmap
     */
    public static Bitmap decodeFile(Context context, String path) {
        if (!ISARFilesUtil.isSDCardExist()) {
//            TipsUtil.showShortToast(context, R.string.msg_sdcard_not_available);
            return null;
        }
        try {
            File file = new File(path);
            if (file.exists()) { // 检测文件是否存在
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, opt);
                Logger.LOGD("BitmapUtil===>>path:" + path + ", " + opt.outWidth + " x " + opt.outHeight);
                opt.inJustDecodeBounds = false;
                Bitmap tmp = BitmapFactory.decodeFile(path, opt);
                // 如果图片存在,返回为null,表明图片没有下载完成
                if (tmp == null) {
                    file.delete();
                }
                return tmp;
            }
        } catch (OutOfMemoryError err) {
            ISARBitmapLoader.getInstance().release();
            return null;
        }
        return null;
    }

    /**
     * 根据路径和参数生成bitmap.
     *
     * @param path      path
     * @param reqWidth  require width
     * @param reqHeight require height
     * @return bitmap
     */
    public static Bitmap decodeFile(Context context, String path, int reqWidth, int reqHeight) {
        if (!ISARFilesUtil.isSDCardExist()) {
//            TipsUtil.showShortToast(context, R.string.msg_sdcard_not_available);
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opt);
        int picWidth = opt.outWidth;
        int picHeight = opt.outHeight;
        opt.inSampleSize = calculateInSampleSize(opt, reqWidth, reqHeight);
        int dstWidth;
        int dstHeight;
        if (picWidth > picHeight) {
            if (picWidth > reqWidth) {
                dstWidth = reqWidth;
                dstHeight = dstWidth * picHeight / picWidth;
            } else {
                dstWidth = picWidth;
                dstHeight = picHeight;
            }
        } else {
            if (picHeight > reqHeight) {
                dstHeight = reqHeight;
                dstWidth = dstHeight * picWidth / picHeight;
            } else {
                dstWidth = picWidth;
                dstHeight = picHeight;
            }
        }
        opt.inJustDecodeBounds = false;
        opt.outWidth = dstWidth;
        opt.outHeight = dstHeight;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path, opt);
            Logger.LOGD("decodeFile->width:" + bitmap.getWidth() + ", height:" + bitmap.getHeight());
            Bitmap tmp = null;
            if (bitmap != null) {
                tmp = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
                //bitmap.recycle();
                bitmap = null;
                System.gc();
            }
            return tmp;
        } catch (OutOfMemoryError err) {
            ISARBitmapLoader.getInstance().release();
            return null;
        }
    }

    /**
     * decode bitmap.
     *
     * @param path path
     * @return bitmap
     */
    public static Bitmap decodeBitmap565FromFile(Context context, String path) {
        if (!ISARFilesUtil.isSDCardExist()) {
//            TipsUtil.showShortToast(context, R.string.msg_sdcard_not_available);
            return null;
        }
        try {
            File file = new File(path);
            if (file.exists()) { // 检测文件是否存在
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, opt);
                Logger.LOGD("BitmapUtil===>>path:" + path + ", " + opt.outWidth + " x " + opt.outHeight);
                opt.inJustDecodeBounds = false;
                Bitmap tmp = BitmapFactory.decodeFile(path, opt);
                // 如果图片存在,返回为null,表明图片没有下载完成
                if (tmp == null) {
                    file.delete();
                }
                int i = tmp.getWidth();
                int j = tmp.getHeight();
                Bitmap localBitmap = Bitmap.createBitmap(i, j, Config.RGB_565);
                Canvas localCanvas = new Canvas(localBitmap);
                localCanvas.drawBitmap(tmp, new Rect(0, 0, i, j), new Rect(0, 0, i, j), null);
                tmp.recycle();
                return localBitmap;
            }
        } catch (OutOfMemoryError err) {
            ISARBitmapLoader.getInstance().release();
            return null;
        }
        return null;
    }

    /**
     * 根据不同的屏幕密度,生成不同的bitmap.
     *
     * @param resources resource
     * @param id        id
     * @return bitmap
     */
    public static Bitmap decodeResource(Resources resources, int id) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = resources.getDisplayMetrics().densityDpi;
        opts.inDensity = resources.getDisplayMetrics().densityDpi;
        return BitmapFactory.decodeResource(resources, id, opts);
    }

    /**
     * 生成有边框的bitmap.
     *
     * @param bitmap bitmap
     * @return bitmap
     */
    public static Bitmap createSolidBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap bitmap2 = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap2);

        final Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        Rect rect = new Rect(0, 0, width, height);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(10);
        canvas.drawRect(rect, paint);
        return bitmap2;
    }

    /**
     * 获取bitmap的option.
     *
     * @param path path
     * @return bitmap
     */
    public static BitmapFactory.Options getBitmapOption(Context context, String path) {
        if (!ISARFilesUtil.isSDCardExist()) {
//            TipsUtil.showShortToast(context, R.string.msg_sdcard_not_available);
            return null;
        }
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opt);
        return opt;
    }

    /*private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            // final float totalReqPixelsCap = reqWidth * reqHeight * 2;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }*/
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            //使用需要的宽高的最大值来计算比率
            final int suitedValue = reqHeight > reqWidth ? reqHeight : reqWidth;
            final int heightRatio = Math.round((float) height / (float) suitedValue);
            final int widthRatio = Math.round((float) width / (float) suitedValue);

            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;//用最大
        }

        return inSampleSize;
    }

    /**
     * bitmap to base64.
     *
     * @param bitmap
     * @return base64 string
     */
    public static String convertBitmapToString(Bitmap bitmap, int bitmapQuality) {
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, bitmapQuality, bStream);
        byte[] bytes = bStream.toByteArray();
        string = Base64.encodeToString(bytes, Base64.DEFAULT);
        return string;
    }

    /**
     * base64 to bitmap.
     *
     * @param string base64 string
     * @return bitmap
     */
    public static Bitmap convertStringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String generateBitmapByFilePath(Context context, String filePath, int width, int height) {
        String dst = null;
        // int[] rgb = NativeUtil.byte2Int(data);
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        InputStream in = null;
        byte[] b = null;
        try {
            in = new FileInputStream(file);
            b = new byte[(int) file.length()];
            in.read(b);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int[] rgb = ISARNativePicUtil.covertByteToColor(b);
        try {
            Bitmap bmp = Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);
            dst = storageBitmap(context, bmp);
            bmp.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dst;
    }

    public static String storageBitmap(Context context, Bitmap bitmap) {
        File file = new File(ISARConstants.APP_CACHE_DIRECTORY + File.separator + "tmp1.jpg");
        String dst = ISARConstants.APP_CACHE_DIRECTORY + File.separator + "tmp2.jpg";
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(CompressFormat.JPEG, 30, fos);
            fos.close();
            ISARNativePicUtil.rgb2gray(context, file.getAbsolutePath(), dst);
            return dst;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * check it is brightness color.
     *
     * @param color float(0~1)
     * @return
     */
    public static boolean isBrightnessColor(float[] color) {
        return ((color[0] * 299.0f + color[1] * 587.0f + color[2] * 114.0f) / 1000.0f) < 0.5f;
    }

    /**
     * get bitmap id for type.
     *
     * @param type
     * @param isBrightnessColor
     * @return
     */
    public static int getARButtonBitmapId(int type, boolean isBrightnessColor) {
        if (isBrightnessColor) {
            switch (type) {
                case 0:
                    return R.drawable.draw_phone_2x;
                case 1:
                    return R.drawable.draw_web_2x;
                case 2:
                    return R.drawable.draw_guid_2x;
                case 3:
                    return R.drawable.draw_content_2x;
                case 6:
                    return R.drawable.draw_music_2x;
                case 8:
                    return R.drawable.draw_app_2x;
                case 11:
                    return R.drawable.draw_effect;
                case 14:
                    return R.drawable.draw_video;
                case 15:
                    return R.drawable.draw_skip;
                case 16:
                    return R.drawable.draw_paused_2x;
                case 17:
                    return R.drawable.draw_model_reset;
                case 18:
                    return R.drawable.draw_model_run;
                case 21:
                    return R.drawable.draw_nearby_3x;
                case 22:
                    return R.drawable.draw_list_3x;
                case 24:
                    return R.drawable.draw_share_weixin_3x;
                case 25:
                    return R.drawable.draw_share_pyq_3x;
                case 26:
                    return R.drawable.draw_share_weibo_3x;
            }
        } else {
            switch (type) {
                case 0:
                    return R.drawable.draw_phone_2x_b;
                case 1:
                    return R.drawable.draw_web_2x_b;
                case 2:
                    return R.drawable.draw_guid_2x_b;
                case 3:
                    return R.drawable.draw_content_2x_b;
                case 6:
                    return R.drawable.draw_music_2x_b;
                case 8:
                    return R.drawable.draw_app_2x_b;
                case 11:
                    return R.drawable.draw_effect_b;
                case 14:
                    return R.drawable.draw_video_b;
                case 15:
                    return R.drawable.draw_skip_b;
                case 16:
                    return R.drawable.draw_paused_2x;
                case 17:
                    return R.drawable.draw_model_reset_b;
                case 18:
                    return R.drawable.draw_model_run_b;
                case 21:
                    return R.drawable.draw_nearby_3x_b;
                case 22:
                    return R.drawable.draw_list_3x_b;
                case 24:
                    return R.drawable.draw_share_weixin_3x_b;
                case 25:
                    return R.drawable.draw_share_pyq_3x_b;
                case 26:
                    return R.drawable.draw_share_weibo_3x_b;
            }
        }
        return -1;
    }
}