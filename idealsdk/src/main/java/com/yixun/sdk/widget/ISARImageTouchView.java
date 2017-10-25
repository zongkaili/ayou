/**
 * Copyright © 2014成都理想境界科技有限公司. All rights reserved.
 *	
 * 项目名称: Idealsee-AR2
 * 类名称: ScaleView
 * 类描述: 
 * 创建人: ly  
 * 创建时间: 2014年7月22日 下午7:52:01
 * 修改人: 
 * 修改时间: 
 * 备注: 
 *
 * @version 
 */
package com.yixun.sdk.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * 
 * implement image translate, zoom
 * @author hongen
 * 
 */
public class ISARImageTouchView extends ImageView {

    private PointF mStartPoint = new PointF();
    private Matrix mMatrix = new Matrix();
    private Matrix mCurrentMatrix = new Matrix();

    private int mMode = 0; // scale or drag
    private static final int DRAG = 1;
    private static final int SCALE = 2; // scale
    private float mStartDis = 0;
    private PointF mMidPoint; // center point

    private OnClickListener mListener;
    
    public ISARImageTouchView(Context context) {
        super(context);
    }

    public ISARImageTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            mMode = DRAG;
            mCurrentMatrix.set(this.getImageMatrix());
            mStartPoint.set(event.getX(), event.getY());
            break;
        case MotionEvent.ACTION_MOVE:
            this.setScaleType(ScaleType.MATRIX);
            if (mMode == DRAG) {
                float dx = event.getX() - mStartPoint.x;
                float dy = event.getY() - mStartPoint.y;
                mMatrix.set(mCurrentMatrix); // move from current position
                mMatrix.postTranslate(dx, dy);
            } else if (mMode == SCALE) {
                float endDis = distance(event);
                if (endDis > 10f) {
                    float scale = endDis / mStartDis; // scale number
                    mMatrix.set(mCurrentMatrix);
                    mMatrix.postScale(scale, scale, mMidPoint.x, mMidPoint.y);
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mMode == DRAG) {
                float dx = event.getX() - mStartPoint.x;
                float dy = event.getY() - mStartPoint.y;
                // if drag move less than 10, perform click event
                if (Math.abs(dx) < 10 && Math.abs(dy) < 10) {
                    mListener.onClick(this);
                }
            }
            mMode = 0;
            break;
        case MotionEvent.ACTION_POINTER_UP:
            mMode = 0;
            break;
        case MotionEvent.ACTION_POINTER_DOWN:
            mMode = SCALE;
            mStartDis = distance(event);
            if (mStartDis > 10f) {
                mMidPoint = midDistance(event);
                mCurrentMatrix.set(this.getImageMatrix());
            }
            break;
        default:
            break;
        }
        this.setImageMatrix(mMatrix);
        return true;
    }
    
    /**
     * two point distance
     * @param event
     * @return
     */
    private static float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * mid distance of two point
     * @param event
     * @return
     */
    private static PointF midDistance(MotionEvent event) {
        float midx = event.getX(1) + event.getX(0);
        float midy = event.getY(1) + event.getY(0);
        return new PointF(midx/2, midy/2);
    }
    
    @Override
    public void setOnClickListener(OnClickListener l) {
        mListener = l;
    }

}
