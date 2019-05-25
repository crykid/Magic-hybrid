package com.mrlu.hybrid.chromeclient;

import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.mrlu.hybrid.proxy.BaseWebViewFragment;


/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 09:39
 * Description:处理WebView内部js事件
 */
public class WebViewChromeClientIml extends WebChromeClient {

    private BaseWebViewFragment fragment;

    public WebViewChromeClientIml(BaseWebViewFragment fragment) {
        this.fragment = fragment;
    }

    /**
     * 对js对话框的处理
     *
     * @param view
     * @param url
     * @param message
     * @param result
     * @return boolean：true-拦截后自己处理
     */
    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return super.onJsAlert(view, url, message, result);
    }

    /**
     * 加载进度监控
     *
     * @param view
     * @param newProgress
     */
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
    }
}
