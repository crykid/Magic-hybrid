package com.mrlu.hybrid.webview;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.mrlu.hybrid.event.native_event.BaseNativeEventHandler;
import com.mrlu.hybrid.event.native_event.NativeEventManager;
import com.mrlu.hybrid.event.native_event.NativeEvent;
import com.mrlu.hybrid.proxy.BaseWebViewFragment;
import com.orhanobut.logger.Logger;


/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 00:03
 * Description:js调用的native接口
 */
public class WebInterface {

    private static final String TAG = "WebInterface";
    private final BaseWebViewFragment FRAGMENT;

    private WebInterface(BaseWebViewFragment fragment) {
        this.FRAGMENT = fragment;
    }

    public static WebInterface create(BaseWebViewFragment fragment) {
        return new WebInterface(fragment);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String nativeExecute(String jsMsg) {

        // TODO: 2019-05-24 注意这里可能有线程切换的问题 ，如果有需要，后续可以在具体handler中自己切换线程

        final BaseNativeEventHandler eventHandler = NativeEventManager.getInstance().getEventHandler();
        final NativeEvent nativeEvent = new NativeEvent(jsMsg);
        Log.d(TAG, "nativeExecute: >>> OPERATION_TYPE >>> : " + nativeEvent.getRequestOperationType());
        Logger.json(jsMsg);

        return eventHandler.handleEvent(FRAGMENT, nativeEvent);
    }
}
