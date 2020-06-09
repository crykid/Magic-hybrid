package com.mrlu.hybrid.client;


import com.mrlu.hybrid.proxy.BaseHybridFragment;
import com.mrlu.hybrid.router.IRouter;
import com.mrlu.hybrid.router.Router;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;


/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 09:38
 * Description:处理
 */
public class WebViewClientIml extends WebViewClient {

    private BaseHybridFragment fragment;
    private final IRouter ROUTER;

    public WebViewClientIml(BaseHybridFragment fragment) {
        ROUTER = Router.getInstance();
        this.fragment = fragment;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return ROUTER.handleHrefUrl(fragment, request.getUrl().toString());
//        return super.shouldOverrideUrlLoading(view, request);
    }
}
