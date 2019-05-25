package com.mrlu.hybrid.webview;

import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by : mr.lu
 * Created at : 2019-05-21 at 23:19
 * Description: 初始化WebView通用接口
 */
public interface IWebViewInitializer {
    WebView initWebView(WebView webView);

    WebViewClient initWebViewClient();

    WebChromeClient initWebChromeClient();
}
