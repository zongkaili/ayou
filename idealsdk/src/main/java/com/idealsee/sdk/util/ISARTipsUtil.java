/**
 * 项目名称: IDSeeAR 
 * 类名称:  TipsUtil 
 * 类描述: 
 * 创建人: Ly
 * 创建时间: 2013-1-24 下午3:49:34  
 * 修改人:
 * 修改时间: 
 * 备注: 
 * @version 
 * 
 */

package com.idealsee.sdk.util;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Toast;


/**
 * Toast utility.
 * 
 * @author idealsee
 * 
 */
public class ISARTipsUtil {
    private static Toast mToast = null;

    private ISARTipsUtil() {

    }

    public static void showLongToast(final Context context, final int id) {
        new Handler(context.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                makeToast(context, id, Toast.LENGTH_SHORT);
            }
        });
    }

    public static void showLongToast(final Context context, final String msg) {
        new Handler(context.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                makeToast(context, msg, Toast.LENGTH_LONG);
            }
        });
    }

    public static void showShortToast(final Context context, final int id) {
        new Handler(context.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                makeToast(context, id, Toast.LENGTH_SHORT);
            }
        });
    }

    public static void showShortToast(final Context context, final String msg, final int gravity) {
        new Handler(context.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                makeToast(context, msg, Toast.LENGTH_SHORT);
            }
        });
    }

    public static void showShortToastAboveView(final Context context, final String msg, final View view) {
        new Handler(context.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                int width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                int height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                toast.getView().measure(width, height);
                int viewWidth = toast.getView().getMeasuredWidth();
                int viewHeight = toast.getView().getMeasuredHeight();
                int[] targetLocation = new int[2];
                view.getLocationOnScreen(targetLocation);

                int xOffset = targetLocation[0] + view.getWidth() / 2 + -viewWidth / 2;
                int yOffset = targetLocation[1] - viewHeight * 2;
                toast.setGravity(Gravity.START | Gravity.TOP, xOffset, yOffset);
                toast.show();
            }
        });
    }

    public static void showShortToastAboveView(final Context context, final String msg, final View view, final int resId) {
        new Handler(context.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setView(view);
                toast.show();
            }
        });
    }

    public static void refreshToast() {
        mToast = null;
    }

    public static void clearToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    /**
     * show short toast.
     * 
     * @param msg
     *            content
     */
    public static void showShortToast(final Context context, final String msg) {
        new Handler(context.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                makeToast(context, msg, Toast.LENGTH_SHORT);
            }
        });
    }

    private static void makeToast(Context context, int id, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context, id, duration);
        }
        mToast.setDuration(duration);
        mToast.setText(id);
        mToast.show();
    }

    public static void makeToast(Context context, String msg, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context, msg, duration);
        }
        mToast.setDuration(duration);
        mToast.setText(msg);
        mToast.show();
    }

    public static void makeToast(Context context, String msg, int duration, int gravity) {
        if (mToast == null) {
            mToast = Toast.makeText(context, msg, duration);
        }
        mToast.setGravity(gravity, 0, 0);
        mToast.setDuration(duration);
        mToast.setText(msg);
        mToast.show();
    }
}