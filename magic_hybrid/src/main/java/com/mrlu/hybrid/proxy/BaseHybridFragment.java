package com.mrlu.hybrid.proxy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;


import com.mrlu.hybrid.chromeclient.WebViewChromeClientIml;
import com.mrlu.hybrid.client.WebViewClientIml;
import com.mrlu.hybrid.config.ConfigEnum;
import com.mrlu.hybrid.config.MagicConfigurator;
import com.mrlu.hybrid.permission.PermissionsManager;
import com.mrlu.hybrid.router.RouterKeys;
import com.mrlu.hybrid.webview.HybridAgreementEnum;
import com.mrlu.hybrid.webview.IWebViewInitializer;
import com.mrlu.hybrid.webview.WebInterface;
import com.mrlu.hybrid.webview.WebViewInitializer;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by : mr.lu
 * Created at : 2019-05-21 at 23:18
 * Description: 将系统的webView统一为腾讯x5 webView。
 * 因为不同的厂商不同系统之间，webView内核版本不一致，导致不同的页面在不同的手机表现不一致。所以统一为腾讯X5浏览器
 */
public abstract class BaseHybridFragment extends Fragment implements IWebViewInitializer {

    private String mUrl = null;
    private boolean lazyLoad = false;
    private WebView mWebView;
    private boolean webViewAvailable = false;
    private final ReferenceQueue<WebView> WEBVIEW_QUEUE = new ReferenceQueue<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        mUrl = args.getString(RouterKeys.URL.name());
        lazyLoad = args.getBoolean(RouterKeys.LAZY_LOAD.name());
        initWebView();
    }


    private void initWebView() {
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
        } else {
            IWebViewInitializer initializer = this;

            //初始化webview
            final WeakReference<WebView> webViewWeakReference = new WeakReference<>(new WebView(getContext()), WEBVIEW_QUEUE);
            mWebView = webViewWeakReference.get();
            mWebView = initializer.initWebView(mWebView);

            mWebView.setWebChromeClient(initializer.initWebChromeClient());
            mWebView.setWebViewClient(initializer.initWebViewClient());

            //我们自定义的协议名称
            final String HYBRID_BRIDGE = MagicConfigurator.getInstance().getConfig(ConfigEnum.HYBRID_BRIDGE_NAME);
            //添加协议--如果我们没有自定义，那么就使用默认的协议名称
            mWebView.addJavascriptInterface(WebInterface.create(this),
                    TextUtils.isEmpty(HYBRID_BRIDGE)
                            ? HybridAgreementEnum.MAGIC_HYBRID_BRIDGE.name()
                            : HYBRID_BRIDGE);

            webViewAvailable = true;
        }
    }


    public String getUrl() {
        if (TextUtils.isEmpty(mUrl)) {
            throw new NullPointerException(" URL is null!");
        }

        return mUrl;
    }

    public WebView getWebView() {
        if (mWebView == null) {
            throw new NullPointerException("WebView is null !");
        }

        return webViewAvailable ? mWebView : null;
    }

    public boolean isLazyLoad() {
        return lazyLoad;
    }

    @Override
    public WebChromeClient initWebChromeClient() {
        return new WebViewChromeClientIml(this);
    }

    @Override
    public WebView initWebView(WebView webView) {
        return WebViewInitializer.createWebView(webView);
    }

    @Override
    public WebViewClient initWebViewClient() {
        return new WebViewClientIml(this);
    }

    public boolean netWorkConnected() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private NetworkInfo getActiveNetworkInfo() {
        return ((ConnectivityManager) getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, PermissionsManager.getInstance().get(requestCode));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
            webViewAvailable = false;
        }
    }
}
