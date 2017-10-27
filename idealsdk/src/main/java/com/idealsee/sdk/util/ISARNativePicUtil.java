/**
 * 
 */

package com.idealsee.sdk.util;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * combine picture.
 * 
 * @author hongen
 * 
 */
public class ISARNativePicUtil {
//    static {
//        System.loadLibrary("ISARNatvieModel");
//    }

    /**
     * rgb2gray.
     * 
     * @param srcpath
     *            source path
     * @param dstpath
     *            target path
     */
    public static void rgb2gray(Context context, String srcpath, String dstpath) {
        Bitmap srcB = ISARBitmapUtil.decodeFile(context, srcpath, 480, 640);
        int w = srcB.getWidth(), h = srcB.getHeight();
        int[] pix = new int[w * h];
        srcB.getPixels(pix, 0, w, 0, 0, w, h);

        int alpha = 0xFF << 24;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // 获得像素的颜色
                int color = pix[w * i + j];
                int red = ((color & 0x00FF0000) >> 16);
                int green = ((color & 0x0000FF00) >> 8);
                int blue = color & 0x000000FF;
                color = (red + green + blue) / 3;
                color = alpha | (color << 16) | (color << 8) | color;
                pix[w * i + j] = color;
            }
        }
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        result.setPixels(pix, 0, w, 0, 0,w, h);
        File file = new File(dstpath);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            result.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            result.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        /*final Bitmap scaleFilterBitmap = Bitmap.createScaledBitmap(filterBitmap, size[0], size[1], false);
        BufferedImage bufferedImage
                = ImageIO.read(new File(System.getProperty("user.dir" + "/test.jpg"));
        BufferedImage grayImage =
                new BufferedImage(bufferedImage.getWidth(),
                        bufferedImage.getHeight(),
                        bufferedImage.getType());


        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                final int color = bufferedImage.getRGB(i, j);
                final int r = (color >> 16) & 0xff;
                final int g = (color >> 8) & 0xff;
                final int b = color & 0xff;
                int gray = (int) (0.3 * r + 0.59 * g + 0.11 * b);;
                System.out.println(i + " : " + j + " " + gray);
                int newPixel = colorToRGB(255, gray, gray, gray);
                grayImage.setRGB(i, j, newPixel);
            }
        }
        File newFile = new File(System.getProperty("user.dir") + "/ok.jpg");
        ImageIO.write(grayImage, "jpg", newFile);*/
    }
   // public static native void rgb2gray(String srcpath, String dstpath);

    /**
     * 处理图片.
     * 
     * @param src
     *            source path
     * @param dst
     *            target path
     * @param dstWidth
     *            target width
     * @param dstHeight
     *            target height
     */
   // public static native void convertBitmap(String src, String dst, int dstWidth, int dstHeight);

    /**
     * blur bitmap.
     * 
     * @param src
     *            source path
     * @param dst
     *            target path
     * @param dstWidth
     *            if 0, using src->width
     * @param dstHeight
     *            if 0, using src->height
     */
   // public static native void blurBitmap(String src, String dst, int dstWidth, int dstHeight);

    /**
     * set torch mode.
     * 
     * @param mode
     *            true or false
     */
  //  public static native void setFlashTorchMode(boolean mode);

    /**
     * combine 2 image to 1.
     * 
     * @param srcTopImage
     *            for top image
     * @param srcBottomImage
     *            for bottom image
     * @return target path
     */
   // public static native int combineBitmap(String srcTopImage, String srcBottomImage, String dstImage);

    /**
     * combine 2 image to 1.
     * 
     * @param srcLeftImage
     *            for left image
     * @param srcRightImage
     *            for right image
     * @return target path
     */
    //public static native int combineBottomBitmap(String srcLeftImage, String srcRightImage, String dstImage);

    /**
     * combine bitmap for weather module.
     * @param srcTopBackgroundPath top background image
     * @param srcTopForegroundPath top foreground image
     * @param dstPath result image of top part
     * @return result image path, null if error.
     */
    //public static native int combineWeatherBitmap(String srcTopBackgroundPath, String srcTopForegroundPath, String dstPath);

    /**
     * combine bitmap for weather module.
     * @param srcTopBackgroundPath top background image
     * @param srcTopForegroundPath top foreground image
     * @param srcBottomPath bottom image
     * @param dstPathTop result image of top part
     * @param combinedPath result image of combined part
     * @return result image path, null if error.
     */
    //public static native int combineWeatherBitmapAll(String srcTopBackgroundPath, String srcTopForegroundPath,
    //        String srcBottomPath, String dstPathTop, String combinedPath);
    /**
     * convert byte to int.
     * 
     * @param data
     *            byte
     * @return int
     */
    public static int convertByteToInt(byte data) {
        int heightBit = (int) ((data >> 4) & 0x0F);
        int lowBit = (int) (0x0F & data);
        return heightBit * 16 + lowBit;
    }

    /**
     * covert byte of rgb image to byte of color bitmap.
     * 
     * @param data
     *            data array
     * @return int array
     */
    public static int[] covertByteToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        int arg = 0;
        if (size % 3 != 0) {
            arg = 1;
        }

        int[] color = new int[size / 3 + arg];
        int red;
        int green;
        int blue;
        if (arg == 0) {
            for (int i = 0; i < color.length; i++) {
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);
                color[i] = (red << 16) | (green << 8) | blue | 0XFF000000;
            }
        } else {
            for (int i = 0; i < color.length - 1; i++) {
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);
                color[i] = (red << 16) | (green << 8) | blue | 0XFF000000;
            }
            color[color.length - 1] = 0xFF000000;
        }
        return color;
    }

    private static int colorToRGB(int alpha, int red, int green, int blue) {

        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;

    }
}
