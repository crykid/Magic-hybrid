package com.mrlu.hybrid.router;


import com.mrlu.hybrid.proxy.BaseWebViewFragment;

/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 09:49
 * Description:
 */
public interface IRouter {

    boolean handleHrefUrl(BaseWebViewFragment fragment, String url);
}
