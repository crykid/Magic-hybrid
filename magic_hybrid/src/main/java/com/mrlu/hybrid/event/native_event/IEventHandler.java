package com.mrlu.hybrid.event.native_event;


import com.mrlu.hybrid.proxy.BaseWebViewFragment;

/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 00:10
 * Description:请求执行者
 */
public interface IEventHandler {

    String execute(BaseWebViewFragment fragment, String params);
}
