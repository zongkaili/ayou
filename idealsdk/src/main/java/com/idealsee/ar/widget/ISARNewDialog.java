package com.idealsee.ar.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

/**
 *  自定义dialog 重写构造方法 
 * @author zongkaili
 */
public class ISARNewDialog extends Dialog{
    private static int default_width = 307; //默认宽度
    private static int default_height = 275;//默认高度
    private WebView mWebView;
    private View mCloseView;
    
    public ISARNewDialog(Context context, View layout, int style) {
        this(context, default_width, default_height, layout, style);
    }
    
    public ISARNewDialog(Context context, int width, int height, View layout, int style) {
        super(context, style);
        setContentView(layout);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    public void setWebView(WebView webView) {
        this.mWebView = webView;
    }

    public WebView getWebView() {
        return mWebView;
    }

    public void setCloseView(View view) {
        this.mCloseView = view;
    }

    public View getCloseView() {
        return this.mCloseView;
    }
}
