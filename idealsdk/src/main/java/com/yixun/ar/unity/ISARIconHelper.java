package com.yixun.ar.unity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.yixun.sdk.util.ISARBitmapUtil;
import com.yixun.sdk.util.ISARColorUtil;

import java.io.ByteArrayOutputStream;

/**
 * Created by yaolei on 17-9-20.
 */

public class ISARIconHelper {

    /**
     * Unity callback draw custom icon.
     *
     * @param width        width
     * @param height       height
     * @param hasIcon      has icon
     * @param type         type
     * @param text         text
     * @param cornerRadius corner radius
     * @param color        color
     * @param textColor    text color
     * @param fontSize     font size
     * @return image byte array
     */
    public static byte[] ISARCustomControlImage(Context context, int width, int height, boolean hasIcon, int type,
                                                String text, float cornerRadius, float[] color, float[] textColor,
                                                int fontSize) {
        // use format height
        int bmpHeight = 20;
        boolean isBrightnessColor = (((color[0] * 299.0f + color[1] * 587.0f + color[2] * 114.0f) / 1000.0f) < 0.5f);
        int resourceId = ISARBitmapUtil.getARButtonBitmapId(type, isBrightnessColor);
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resourceId);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float[] startColor = ISARColorUtil.getStartColor(color);
        int bgStartColor = Color.argb(255, (int) startColor[0], (int) startColor[1], (int) startColor[2]);
        int bgEndColor = Color.argb(255, (int) Math.round(color[0] * 255.0), (int) Math.round(color[1] * 255.0),
                (int) Math.round(color[2] * 255.0));
        LinearGradient lg = new LinearGradient(0, 0, width, height, bgStartColor, bgEndColor, Shader.TileMode.MIRROR);
        paint.setShader(lg);
        if (cornerRadius == 0f) {
            canvas.drawRect(new Rect(0, 0, width, height), paint);
        } else {
            canvas.drawRoundRect(new RectF(new Rect(0, 0, width, height)), cornerRadius, cornerRadius, paint);
        }
        paint.setShader(null);
        int bmpWidth = 0;
        if (bmp != null) {
            bmp = ISARBitmapUtil.scaleBitmapByHeight(bmp, bmpHeight);
            // calculate position of bmp and text
            bmpWidth = bmp.getWidth();
        }
        float txtWidth = 0;
        float textHeight = 0;
        float fontLead = 0;
        if (null != text && !"".equals(text)) {
            int txtColor = Color.argb(255, (int) Math.round(textColor[0] * 255.0),
                    (int) Math.round(textColor[1] * 255.0), (int) Math.round(textColor[2] * 255.0));
            if (!isBrightnessColor) {
                txtColor = Color.BLACK;
            }
            paint.setColor(txtColor);
            paint.setTextSize(fontSize);
            Paint.FontMetrics fM = paint.getFontMetrics();
            // 指定笔的文字高度
            float fontHeight = fM.descent - fM.ascent;
            // 指定笔离文字顶部的距离
            fontLead = fM.leading - fM.ascent;
            txtWidth = paint.measureText(text);
            textHeight = fontHeight;

            float txtX = (width - txtWidth - bmpWidth) / 2 + bmpWidth + 2;
            float txtY = (height - textHeight) / 2 + fontLead;
            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawText(text, txtX, txtY, paint);
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        }
        if (hasIcon && bmp != null) {
            float bmpX = (width - txtWidth - bmpWidth) / 2;
            float bmpY = (height - bmpHeight) / 2;
            paint.setColor(bgEndColor);
            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawBitmap(bmp, bmpX, bmpY, paint);
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] arr = bos.toByteArray();

        bitmap.recycle();
        return arr;
    }

    /**
     * Unity callback draw music icon.
     *
     * @param width        width
     * @param height       height
     * @param cornerRadius corner radius
     * @param color        color
     * @return image byte array
     */
    public static byte[] ISARCustomMusicImage(Context context, int width, int height, float cornerRadius, float[] color) {
        boolean isBrightnessColor = (((color[0] * 299.0f + color[1] * 587.0f + color[2] * 114.0f) / 1000.0f) < 0.5f);
        // use format height
        int bmpHeight = 20;
        int resourceId = ISARBitmapUtil.getARButtonBitmapId(6, isBrightnessColor);
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resourceId);
        bmp = ISARBitmapUtil.scaleBitmapByHeight(bmp, bmpHeight);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float[] startColor = ISARColorUtil.getStartColor(color);
        int bgStartColor = Color.argb(255, (int) startColor[0], (int) startColor[1], (int) startColor[2]);
        int bgEndColor = Color.argb(255, (int) Math.round(color[0] * 255.0), (int) Math.round(color[1] * 255.0),
                (int) Math.round(color[2] * 255.0));
        LinearGradient lg = new LinearGradient(0, 0, width, height, bgStartColor, bgEndColor, Shader.TileMode.MIRROR);
        paint.setShader(lg);

        if (cornerRadius == 0f) {
            canvas.drawCircle(width / 2, height / 2, width / 2, paint);
        } else {
            canvas.drawRoundRect(new RectF(new Rect(0, 0, width, height)), cornerRadius, cornerRadius, paint);
        }
        float bmpX = (width - bmp.getWidth()) / 2;
        float bmpY = (height - bmpHeight) / 2;
        canvas.drawBitmap(bmp, bmpX, bmpY, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] arr = bos.toByteArray();

        bitmap.recycle();
        return arr;
    }


    /**
     * unity call back to draw shape.
     *
     * @param width  width
     * @param height height
     * @param radius radius
     * @param colorr r
     * @param colorg g
     * @param colorb b
     * @return image byte array
     */
    public static byte[] ISARDrawShapeControl(int width, int height, int radius, float colorr, float colorg, float colorb) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float[] color = {colorr, colorg, colorb};
        float[] startColor = ISARColorUtil.getStartColor(color);
        int bgStartColor = Color.argb(255, (int) startColor[0], (int) startColor[1], (int) startColor[2]);
        int bgEndColor = Color.argb(255, (int) Math.round(color[0] * 255.0), (int) Math.round(color[1] * 255.0),
                (int) Math.round(color[2] * 255.0));
        LinearGradient lg = new LinearGradient(0, 0, width, height, bgStartColor, bgEndColor, Shader.TileMode.MIRROR);
        paint.setShader(lg);
        canvas.drawRoundRect(new RectF(new Rect(0, 0, width, height)), radius, radius, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] arr = bos.toByteArray();

        bitmap.recycle();
        return arr;
    }


    /**
     * unity call back to draw text.
     *
     * @param str      string
     * @param fontSize font size
     * @param width    width
     * @param height   height
     * @param colorr   r
     * @param colorg   g
     * @param colorb   b
     * @return image byte array
     */
    public static byte[] ISARDrawTextControl(String str, int fontSize, int width, int height, float colorr, float colorg,
                                             float colorb) {
        // text size
        float value = fontSize * 1.4f;
        // text color
        int color = Color.argb(255, (int) Math.round(colorr * 255.0), (int) Math.round(colorg * 255.0),
                (int) Math.round(colorb * 255.0));
        TextPaint textPaint = new TextPaint();
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(color);
        textPaint.setTextSize(value);
        Typeface font = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD);
        textPaint.setTypeface(font);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        // 获取文字高度，暂时不用
        // int lineHeight = (int) Math.ceil(fm.bottom - fm.top);
        StaticLayout layout = new StaticLayout(str, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
                false);
        int layoutHeight = layout.getHeight();
        // 保持宽高比
        int bHeight = (height > layoutHeight) ? height : layoutHeight;
        float ratio = (float) width / (float) height;
        int bWidth = (int) (bHeight * ratio);
        Bitmap bitmap = Bitmap.createBitmap(bWidth, bHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        canvas.translate(0, 0);
        layout.draw(canvas);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] arr = bos.toByteArray();

        bitmap.recycle();
        return arr;
    }

    /**
     * draw image text with gradient color.
     *
     * @param width      width of widget
     * @param height     height of widget
     * @param colorStart start color
     * @param colorEnd   end color
     * @param text       text
     * @return byte of image
     */
    public static byte[] ISARDrawNoWidgets(int width, int height, float[] colorStart, float[] colorEnd, String text) {
        int cornerRadius = 30;
        int fontSize = 30;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        int startColor = Color.argb(255, (int) Math.round(colorStart[0] * 255.0),
                (int) Math.round(colorStart[1] * 255.0), (int) Math.round(colorStart[2] * 255.0));
        int endColor = Color.argb(255, (int) Math.round(colorEnd[0] * 255.0), (int) Math.round(colorEnd[1] * 255.0),
                (int) Math.round(colorEnd[2] * 255.0));
        // paint.setColor(bgColor);
        LinearGradient lg = new LinearGradient(0, 0, width, height, startColor, endColor, Shader.TileMode.MIRROR);
        paint.setShader(lg);
        canvas.drawRoundRect(new RectF(new Rect(0, 0, width, height)), cornerRadius, cornerRadius, paint);
        paint.setShader(null);

        float txtWidth = 0;
        float textHeight = 0;
        float fontLead = 0;
        if (null != text && !"".equals(text)) {
            int txtColor = Color.WHITE;
            paint.setColor(txtColor);
            paint.setTextSize(fontSize);
            Paint.FontMetrics fM = paint.getFontMetrics();
            // 指定笔的文字高度
            float fontHeight = fM.descent - fM.ascent;
            // 指定笔离文字顶部的距离
            fontLead = fM.leading - fM.ascent;
            txtWidth = paint.measureText(text);
            textHeight = fontHeight;

            float txtX = (width - txtWidth) / 2 + 2;
            float txtY = (height - textHeight) / 2 + fontLead;
            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawText(text, txtX, txtY, paint);
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] arr = bos.toByteArray();

        bitmap.recycle();
        return arr;
    }
}
