package com.yixun.sdk.util;

public class ISARColorUtil {
    
    /**
     * get start color from end color.
     * @param color float(0~1) r, g, b
     * @return 255 255 255
     */
    public static float[] getStartColor(float[] color) {
        float R = color[0] * 255f;
        float G = color[1] * 255f;
        float B = color[2] * 255f;
        float[] hsb = function2HSB(R, G, B);
        hsb[2] += .4 ;
        hsb[1] -= .2 ;
        float[] rgb = function2RGB(hsb[0], hsb[1], hsb[2]);
        
        rgb[0] = rgb[0] > 255f ? 255f : rgb[0];
        rgb[1] = rgb[1] > 255f ? 255f : rgb[1];
        rgb[2] = rgb[2] > 255f ? 255f : rgb[2];
        return rgb;
    }
    
    private static float[] function2HSB(float R, float G, float B) {//rgb转hsb
        
        float HSB_H = 0F;
        float HSB_S = 0f;
        float HSB_B = 0f;
        float var_Min = Math.min(Math.min(R, G), B);
        float var_Max = Math.max(Math.max(R, G), B);
        if (var_Min == var_Max) {
            HSB_H = 0;
        } else if (var_Max == R && G >= B) {
            HSB_H = 60 * ((G - B) / (var_Max - var_Min));
        } else if (var_Max == R && G < B) {
            HSB_H = 60 * ((G - B) / (var_Max - var_Min)) + 360;
        } else if (var_Max == G) {
            HSB_H = 60 * ((B - R) / (var_Max - var_Min)) + 120;
        } else if (var_Max == B) {
            HSB_H = 60 * ((R - G) / (var_Max - var_Min)) + 240;
        }

        if (var_Max == 0) {
            HSB_S = 0;
        } else {
            HSB_S = 1 - (var_Min / var_Max);
        }

        float var_R = (R / 255f);
        float var_G = (G / 255f);
        float var_B = (B / 255f);
        HSB_B = Math.max(Math.max(var_R, var_G), var_B);
        HSB_H = (HSB_H >= 360f) ? 0 : HSB_H;
        return new float[] {HSB_H, HSB_S, HSB_B};
    }

    private static float[] function2RGB(float H, float S, float B) {// 修改hsb参数后，转回rgb(即渐变的开始颜色)
        // float[] rgb = {R=0, G=0, B=0};
        float colorR = 0f;
        float colorG = 0f;
        float colorB = 0f;
        int i;
        float f, p, q, t;
        H = (H >= 360) ? 0 : H;

        if (S == 0) {
            colorR = B * 255f;
            colorG = B * 255f;
            colorB = B * 255f;
        } else {
            i = (int) (Math.floor(H / 60) % 6);
            f = H / 60f - i;
            p = B * (1f - S);
            q = B * (1f - S * f);
            t = B * (1f - S * (1f - f));
            switch (i) {
            case 0:
                colorR = B;
                colorG = t;
                colorB = p;
                break;

            case 1:
                colorR = q;
                colorG = B;
                colorB = p;
                break;
            case 2:
                colorR = p;
                colorG = B;
                colorB = t;
                break;

            case 3:
                colorR = p;
                colorG = q;
                colorB = B;
                break;

            case 4:
                colorR = t;
                colorG = p;
                colorB = B;
                break;

            case 5:
                colorR = B;
                colorG = p;
                colorB = q;
                break;
            }
            colorR = colorR * 255f;
            colorG = colorG * 255f;
            colorB = colorB * 255f;
        }
        return new float[] { colorR, colorG, colorB };
    }
}
