package com.mrlu.hybrid.config;

/**
 * Created by : mr.lu
 * Created at : 2019-05-21 at 23:26
 * Description:
 */
public enum ConfigEnum {

    /**
     * 是否debug
     */
    DEBUG,

    /**
     * 主线程的handler
     */
    HANDLER,

    /**
     *
     */
    APPLICATION_CONTEXT,

    /**
     * Web与Native交互的协议，在js中，js通过协议调用native的方法
     */
    HYBRID_BRIDGE_NAME,

    /**
     * h5地址host
     */
    WEB_API_HOST,

}
