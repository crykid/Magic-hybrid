package com.mrlu.hybrid.proxy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mrlu.hybrid.router.Router;
import com.mrlu.hybrid.router.RouterKeys;


/**
 * Created by : mr.lu
 * Created at : 2019-05-21 at 23:18
 * Description:
 */
public class WebFragmentIml extends BaseHybridFragment {

    private boolean initialized = false;

    public static WebFragmentIml create(String url) {
        final Bundle bundle = new Bundle();
        bundle.putString(RouterKeys.URL.name(), url);
        final WebFragmentIml fragment = new WebFragmentIml();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static WebFragmentIml create(String url, boolean lazyLoad) {
        final Bundle bundle = new Bundle();
        bundle.putString(RouterKeys.URL.name(), url);
        bundle.putBoolean(RouterKeys.LAZY_LOAD.name(), lazyLoad);
        final WebFragmentIml fragment = new WebFragmentIml();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View webView = getWebView();
        if (!TextUtils.isEmpty(getUrl()) && netWorkConnected() && !isLazyLoad()) {
            Router.getInstance().loadPage(this, getUrl());
        }
        initialized = true;
        return webView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isLazyLoad() && initialized) {

            Router.getInstance().loadPage(this, getUrl());
        }
    }
}
