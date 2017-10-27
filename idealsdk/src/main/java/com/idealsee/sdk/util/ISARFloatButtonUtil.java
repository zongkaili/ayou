package com.idealsee.sdk.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.idealsee.sdk.model.ISARButtonInfo;
import com.unity3d.player.UnityPlayer;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by yaolei on 17-6-22.
 */

public class ISARFloatButtonUtil implements View.OnClickListener {
    public static final String TAG = ISARFloatButtonUtil.class.getSimpleName();

    private static final int FLOAT_BUTTON_WIDTH = 50;
    private static final int FLOAT_IMAGE_WIDTH = 20;
    private static final int FLOAT_BUTTON_LEFT = 20;

    private Context mContext;
    private RelativeLayout mFloatButtonsLayout;
    private OnFloatButtonTouchListener mTouchListener;

    public interface OnFloatButtonTouchListener {
        void doFloatBtnTouchEvent(String jsonStr, String btnId);
    }

    public ISARFloatButtonUtil(Context context, OnFloatButtonTouchListener listener) {
        mContext = context;
        mTouchListener = listener;
    }

    public void refreshFloatButtons(String jsonStr, UnityPlayer unityPlayer) {
        if (TextUtils.isEmpty(jsonStr)) {
            return;
        }

        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int[] positionY = new int[5];
        positionY[2] = (dm.heightPixels - ISARDensityUtil.dip2px(mContext, FLOAT_BUTTON_WIDTH)) / 2;
        int segment = ISARDensityUtil.dip2px(mContext, FLOAT_BUTTON_WIDTH + FLOAT_BUTTON_WIDTH / 2);
        positionY[1] = positionY[2] - segment;
        positionY[0] = positionY[1] - segment;
        positionY[3] = positionY[2] + segment;
        positionY[4] = positionY[3] + segment;
        Logger.LOGD(TAG + " refresh floatButtons buttons str : " + jsonStr);
        if (mFloatButtonsLayout != null) {
            mFloatButtonsLayout.removeAllViews();
        } else {
            ViewGroup unityParentView = (ViewGroup) (unityPlayer.getParent());
            mFloatButtonsLayout = new RelativeLayout(mContext);
            unityParentView.addView(mFloatButtonsLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        try {
            JSONArray array = new JSONArray(jsonStr);
            int size = array.length();
            if (size > 5) {
                Logger.LOGD(TAG + " FlowButtons size error , size = " + size);
                return;
            }
            for (int i = size - 1; i > -1; i--) {
                ISARButtonInfo info = new ISARButtonInfo(array.getJSONObject(i));
                View floatButton = floatButtonFactory(info);
                int width = ISARDensityUtil.dip2px(mContext, FLOAT_BUTTON_WIDTH);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width);
                params.topMargin = positionY[size - 1 - i];
                params.leftMargin = ISARDensityUtil.dip2px(mContext, FLOAT_BUTTON_LEFT);
                floatButton.setLayoutParams(params);
                if (null != mFloatButtonsLayout)
                    mFloatButtonsLayout.addView(floatButton);
                final AnimatorSet showAnimator = initFloatButtonShowAnimation(floatButton);
                AnimatorSet delayAnimator = initFloatButtonDelayAnimation(floatButton, info.startTime * 1000);
                delayAnimator.addListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showAnimator.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }
                });
                delayAnimator.start();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clearFloatButtons() {
        if (mFloatButtonsLayout != null) {
            mFloatButtonsLayout.removeAllViews();
        }
    }

    private AnimatorSet initFloatButtonShowAnimation(View view) {
        int animDuration = 200;

        PropertyValuesHolder menuBiggerX = PropertyValuesHolder.ofFloat("scaleX", 0, 1.0f);
        PropertyValuesHolder menuBiggerY = PropertyValuesHolder.ofFloat("scaleY", 0, 1.0f);

        ObjectAnimator animBigger = ObjectAnimator.ofPropertyValuesHolder(view, menuBiggerX, menuBiggerY);
        animBigger.setInterpolator(new AccelerateInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(animDuration);
        animatorSet.play(animBigger);
        return animatorSet;
    }

    private AnimatorSet initFloatButtonDelayAnimation(View view, int duration) {
        PropertyValuesHolder menuBiggerX = PropertyValuesHolder.ofFloat("scaleX", 0, 0);
        PropertyValuesHolder menuBiggerY = PropertyValuesHolder.ofFloat("scaleY", 0, 0);

        ObjectAnimator animBigger = ObjectAnimator.ofPropertyValuesHolder(view, menuBiggerX, menuBiggerY);
        animBigger.setInterpolator(new AccelerateInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration);
        animatorSet.play(animBigger);
        return animatorSet;
    }

    private View floatButtonFactory(ISARButtonInfo info) {
        RelativeLayout layout = new RelativeLayout(mContext);
        ImageView back = new ImageView(mContext);
        int backWidth = ISARDensityUtil.dip2px(mContext, FLOAT_BUTTON_WIDTH);
        RelativeLayout.LayoutParams backParams = new RelativeLayout.LayoutParams(backWidth, backWidth);
        back.setLayoutParams(backParams);
        if (TextUtils.isEmpty(info.md5)) {
            int bgEndColor = Color.argb((int) info.backgroundColor[3], (int) info.backgroundColor[0], (int) info.backgroundColor[1],
                    (int) info.backgroundColor[2]);
            GradientDrawable backDrawable = new GradientDrawable();
            backDrawable.setShape(GradientDrawable.RECTANGLE);
            int radius = ISARDensityUtil.dip2px(mContext, info.radius);
            backDrawable.setCornerRadius(radius);
            backDrawable.setColor(bgEndColor);
            back.setBackground(backDrawable);
            layout.addView(back);
            Logger.LOGD(TAG + " floatButtonFactory TextUtils.isEmpty(info.actionJson)=" + TextUtils.isEmpty(info.actionJson) + ",info.actionJson=" + info.actionJson);
            if (!TextUtils.isEmpty(info.actionJson)) {
                float[] backGroundRRB = new float[]{(info.backgroundColor[0] / 255.0f), info.backgroundColor[1] / 255.0f,
                        info.backgroundColor[2] / 255.0f};
                boolean isBrightnessColor = ISARBitmapUtil.isBrightnessColor(backGroundRRB);
                int frontIconId;
                if(info.actionType == 24){//分享
                    frontIconId = ISARBitmapUtil.getARButtonBitmapId(info.actionType + info.shareWay, isBrightnessColor);
                }else {
                    frontIconId = ISARBitmapUtil.getARButtonBitmapId(info.actionType, isBrightnessColor);
                }
                if (frontIconId != -1 && info.showFrontIcon) {
                    ImageView front = new ImageView(mContext);
                    int width = ISARDensityUtil.dip2px(mContext, FLOAT_IMAGE_WIDTH);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width);
                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    front.setLayoutParams(params);
                    front.setBackgroundResource(frontIconId);
                    layout.addView(front);
                }
            }
        } else {
            String url = ISARNetUtil.getUrlFromMD5(info.md5);
            back.setTag(url);
            ImageDownloader loader = new ImageDownloader();
            loader.execute(back);
            layout.addView(back);
        }

        layout.setTag(info);
        layout.setOnClickListener(this);
        return layout;
    }

    @Override
    public void onClick(final View v) {
        AnimatorSet animatorSet = initFloatButtonClickAnimation(v);
        animatorSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ISARButtonInfo info = (ISARButtonInfo) v.getTag();
                Logger.LOGD(TAG + " initFloatButtonsLayout v=" + info.actionId + ", y=" + v.getY());
                if (!TextUtils.isEmpty(info.actionJson)) {
                    mTouchListener.doFloatBtnTouchEvent(info.actionJson, info.actionId);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        animatorSet.start();
    }

    private AnimatorSet initFloatButtonClickAnimation(View view) {
        float scaleTo = 0.7f;
        int animDuration = 100;

        PropertyValuesHolder menuSmallX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, scaleTo);
        PropertyValuesHolder menuSmallY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, scaleTo);

        ObjectAnimator animSmall = ObjectAnimator.ofPropertyValuesHolder(view, menuSmallX, menuSmallY);
        animSmall.setInterpolator(new AccelerateInterpolator());

        PropertyValuesHolder menuBiggerX = PropertyValuesHolder.ofFloat("scaleX", scaleTo, 1.0f);
        PropertyValuesHolder menuBiggerY = PropertyValuesHolder.ofFloat("scaleY", scaleTo, 1.0f);

        ObjectAnimator animBigger = ObjectAnimator.ofPropertyValuesHolder(view, menuBiggerX, menuBiggerY);
        animBigger.setInterpolator(new AccelerateInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(animDuration);
        animatorSet.play(animSmall).before(animBigger);
        return animatorSet;
    }

    private class ImageDownloader extends AsyncTask<View, Integer, View> {

        @Override
        protected View doInBackground(View... params) {
            ISARBitmapLoader.getInstance().loadBitmapByUrlOnHttp(mContext, (String) params[0].getTag());
            return params[0];
        }

        @Override
        protected void onPostExecute(View view) {
            Bitmap bitmap = ISARBitmapLoader.getInstance().loadBitmapByUrlNoHttp(mContext, (String) view.getTag());
            if (bitmap != null) {
                int x = 0;
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int size = width;
                if (width > height) {
                    size = height;
                    x = (width - height) / 2;
                }
                bitmap = Bitmap.createBitmap(bitmap, x, 0, size, size);
                ((ImageView) view).setImageBitmap(bitmap);
            }
        }
    }
}
