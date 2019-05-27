package com.mrlu.hybrid.webview;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.mrlu.hybrid.config.ConfigEnum;
import com.mrlu.hybrid.config.MagicConfigurator;

/**
 * Created by : mr.lu
 * Created at : 2019-05-21 at 23:21
 * Description:
 */
public class WebViewInitializer {
    @SuppressLint("SetJavaScriptEnabled")
    public final static WebView createWebView(WebView webView) {
        final boolean DEBUG = MagicConfigurator.getInstance().getConfig(ConfigEnum.DEBUG);
        //允许调试
        WebView.setWebContentsDebuggingEnabled(DEBUG);

        //禁止横向滚动
        webView.setHorizontalScrollBarEnabled(false);
        //禁止纵向滚动
        webView.setVerticalScrollBarEnabled(false);

        //屏蔽长按事件
        webView.setOnLongClickListener(v -> true);

        //初始化WebSettings
        final WebSettings settings = webView.getSettings();

        //开放javaScript通道，否则不能交互
        settings.setJavaScriptEnabled(true);
        //支持通过JS打开新窗口
//        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        //添加UA标识
        final String ua = settings.getUserAgentString();
        final String HYBRID_BRIDGE = MagicConfigurator.getInstance().getConfig(ConfigEnum.HYBRID_BRIDGE_NAME);
        settings.setUserAgentString(ua +
                (TextUtils.isEmpty(HYBRID_BRIDGE)
                        ?
                        HybridAgreementEnum.MAGIC_HYBRID_BRIDGE.name()
                        :
                        HYBRID_BRIDGE)
        );

        //隐藏缩放空间
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        //禁止缩放
        settings.setSupportZoom(false);
        //文件权限
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowContentAccess(true);

        //缓存相关
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        //不使用缓存,相同地址每次都重新加载
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        settings.setTextZoom(100);
        return webView;
    }

}
