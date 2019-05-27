package com.mrlu.hybrid.activityresult;

import android.content.Intent;

/**
 * Created by : mr.lu
 * Created at : 2019-05-24 at 10:31
 * Description:startActivityForResult()之后，onActivityResult()处理者；
 *
 * <h2>一、背景</h2>
 * <p>在我们的NativeEventHandler中，可能会碰到打开相机、相册或其它组件等等以startActivityForResult方式启动其它
 * Activity的情况。当然这时候我们NativeEventHandler所依赖的activity应该是功能单一的且纯粹的，不应该包含过多的
 * 逻辑或者与NativeEventHandler过多的耦合，所以，在其onActivityResult中不应该执行本该NativeEventHandler执行的
 * 事件，这就意味着，NativeEventHandler必须来执行onActivityResult的事件；</p>
 * <h2>二、使用方式：</h2>
 * <h3>仅需要3步：</h3>
 * <ul>
 * <li>1.在NativeEventHandler中实现；</li>
 * <li>2.在NativeEventHandler的构造函数或初始化的地方使用{@link ActivityResultHandlerManager}的add()方法；</li>
 * <li>3.在依赖的activity的onActivityResult中使用ActivityResultHandlerManager的handleResult()方法；</li>
 *
 * </ul>
 */
public interface IActivityResultHandler {

    /**
     * startActivityForResult中的requestCode；
     *
     * @return int：requestCode
     */
//    int getRequestCode();

    /**
     * 代理执行本该在所依赖的activity的onActivityResult执行的事件；
     *
     * @param intent data
     */
    void handleActivityResult(Intent intent);
}
