package com.mrlu.hybrid.router;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.mrlu.hybrid.proxy.BaseWebViewFragment;


/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 09:47
 * Description:
 */
public class Router implements IRouter {
    private static final String TAG = "Router";

    private Router() {
    }


    private static class Holder {
        private final static Router INSTANCE = new Router();
    }

    public static Router getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean handleHrefUrl(BaseWebViewFragment fragment, String url) {

        if (url.contains("google")) {
            Toast.makeText(fragment.getContext(),"在中国大陆无法访问google",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "handleHrefUrl:无法访问 " + url);
            return true;
        }


        return false;
    }

    public void loadPage(BaseWebViewFragment fragment, String url) {
        if (fragment != null && fragment.getWebView() != null) {
            loadPage(fragment.getWebView(), url);
        }
    }

    public void loadPage(WebView webView, String url) {
        if (url.contains("http")) {
            loadWebUrl(webView, url);
        } else {
            loadLocalPage(webView, url);
        }
    }

    /**
     * 放在asset 文件夹下的文件
     *
     * @param webView
     * @param url
     */
    private void loadLocalPage(WebView webView, String url) {
        if (webView != null && !TextUtils.isEmpty(url)) {
            webView.loadUrl("file:///android_asset/" + url);
        }
    }


    private void loadWebUrl(WebView webView, String url) {
        if (webView != null && !TextUtils.isEmpty(url)) {
            webView.loadUrl(url);
        }
    }
}
