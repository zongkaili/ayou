/**
 * Copyright © 2013成都理想境界科技有限公司. All rights reserved.
 * <p>
 * 项目名称: Idealsee-syx
 * 类名称: SyxApplication
 * 类描述:
 * 创建人: ly
 * 创建时间: 2013年9月5日 上午10:29:37
 * 修改人:
 * 修改时间:
 * 备注:
 *
 * @version
 */
package com.yixun.sdk.demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.yixun.sdk.demo.R;
import com.idealsee.sdk.util.ISARDensityUtil;
import com.idealsee.sdk.util.Logger;

public class CharSlideMenu extends View {
    private static final String TAG = CharSlideMenu.class.getSimpleName();
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    private String[] chars = {"A", "B", "C", "D", "E"};
    private int choose = -1;
    private Paint paint = new Paint();
    private int textSize = 0;
    private int mViewHeight;
    private int singleHeight;
    private Context context;
    private int radius1, radius2;
    private int selectColor;
    private int mPageCount = 0;

    public CharSlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CharSlideMenu(Context context) {
        super(context);
        init(context);
    }

    public CharSlideMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        textSize = width / 25;
        radius1 = ISARDensityUtil.dip2px(context, 5);
        radius2 = ISARDensityUtil.dip2px(context, 5);

        singleHeight = ISARDensityUtil.dip2px(context, 30);
        selectColor = context.getResources().getColor(R.color.color_char_slide_select_bg);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 纵向画page小圆点
        int width = getWidth();
        for (int i = 0; i < chars.length; i++) {
            Logger.LOGD(TAG + "[CharSlideMenu] onDrwa i=" + i + ",choose=" + choose);
            paint.setAntiAlias(true);
            float cXPos = width / 2;
            float cYPos = singleHeight * i + singleHeight;
            if (i == choose) {
                paint.setTextSize(textSize);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                paint.setColor(selectColor);
                paint.setFakeBoldText(true);
                canvas.drawCircle(cXPos, cYPos, radius2, paint);
                paint.setColor(Color.WHITE);

                cXPos = cXPos - paint.measureText(chars[i]) / 2;
                cYPos = cYPos + paint.measureText(chars[i]) / 2;
                // canvas.drawText(chars[i], cXPos, cYPos, paint);

            } else {
                paint.setColor(Color.WHITE);
                canvas.drawCircle(cXPos, cYPos, radius1, paint);
            }
            paint.reset();
        }
    }

    public void setMaxNumber(int count) {
        Logger.LOGD(TAG + "[CharSlideMenu] setMaxNumber count=" + count);
        mPageCount = count;
        chars = null;
        chars = new String[count];
        for (int i = 0; i < count; i++) {
            chars[i] = String.valueOf(i + 1);
        }
        /*
         * mViewWidth = (chars.length + 1) * DensityUtil.dip2px(context, 30);
         * param.width = mViewWidth;
         * param.addRule(RelativeLayout.CENTER_HORIZONTAL);
         * setLayoutParams(param);
         */
        // Relayout make layout align parent right.
        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) getLayoutParams();
        mViewHeight = (chars.length + 1) * ISARDensityUtil.dip2px(context, 30);
        param.height = mViewHeight;
        param.addRule(RelativeLayout.CENTER_VERTICAL);
        setLayoutParams(param);
        // this.setBackgroundResource(R.drawable.char_slide_menu_bg);
        choose = 0;
        invalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        /*
         * final int action = event.getAction(); final float x = event.getX();
         * 
         * final int oldChoose = choose; Logger.LOGD("x: " + x); final
         * OnTouchingLetterChangedListener listener =
         * onTouchingLetterChangedListener; int c = 0; for (int i = 0; i <
         * chars.length; i++) { if (x > (1.5 + i) * singleWidth) { if (i ==
         * chars.length - 1) { c = i; } } else { c = i; break; } }
         * 
         * switch (action) { case MotionEvent.ACTION_DOWN: if (oldChoose != c &&
         * listener != null) { listener.onTouchingLetterChanged(c, false);
         * choose = c; invalidate(); } break; case MotionEvent.ACTION_MOVE: if
         * (oldChoose != c && listener != null) {
         * listener.onTouchingLetterChanged(c, false); choose = c; invalidate();
         * } break; case MotionEvent.ACTION_UP: if (oldChoose == c && listener
         * != null) { listener.onTouchingLetterChanged(c, true); choose = c;
         * invalidate(); } break; case MotionEvent.ACTION_CANCEL: invalidate();
         * break; }
         */
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Logger.LOGD(TAG + "onTouchEvent");
        return super.onTouchEvent(event);
    }

    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(int index, boolean isUp);
    }

    public void setChoose(int choose) {
        this.choose = choose;
    }

    public int getMaxCount() {
        return this.mPageCount;
    }
}
