package com.mrlu.hybrid.client;

import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mrlu.hybrid.proxy.BaseWebViewFragment;
import com.mrlu.hybrid.router.IRouter;
import com.mrlu.hybrid.router.Router;


/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 09:38
 * Description:处理
 */
public class WebViewClientIml extends WebViewClient {

    private BaseWebViewFragment fragment;
    private final IRouter ROUTER;

    public WebViewClientIml(BaseWebViewFragment fragment) {
        ROUTER = Router.getInstance();
        this.fragment = fragment;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return ROUTER.handleHrefUrl(fragment, request.getUrl().toString());
//        return super.shouldOverrideUrlLoading(view, request);
    }
}
