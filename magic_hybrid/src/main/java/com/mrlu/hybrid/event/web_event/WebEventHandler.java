package com.mrlu.hybrid.event.web_event;

import android.os.Handler;
import android.util.Log;

import com.mrlu.hybrid.config.ConfigEnum;
import com.mrlu.hybrid.config.MagicConfigurator;
import com.mrlu.hybrid.event.native_event.IEventHandler;
import com.mrlu.hybrid.proxy.BaseHybridFragment;
import com.orhanobut.logger.Logger;
import com.mrlu.hybrid.event.native_event.BaseNativeEventHandler;

/**
 * Created by : mr.lu
 * Created at : 2019-05-23 at 12:44
 * Description:<h2>Native请求web执行的事件处理者；</h2><br>
 * <p>与其说这是一个Native请求web执行的事件的执行者，倒不如说这是一个<strong>中介</strong>，因为它其实并不会执行
 * 具体的事件，他只负责将我们Native请求Web执行的事件传递给Web；</p>
 */
public class WebEventHandler implements IEventHandler {
    private static final String TAG = "WebEventHandler";

    private static WebEventHandler webEventHandler = null;
    private final Handler HANDLER;

    private WebEventHandler() {
        HANDLER = MagicConfigurator.getInstance().getConfig(ConfigEnum.HANDLER);
    }

    /**
     * 因为WebEventHandler并不像{@link BaseNativeEventHandler}一样负责处理具体事情并且会有多种类型，所以我们
     * 不需要对每种事件做一个单独的封装，只需要传递一下事件而已，所以我不太希望这个它重复的创建多个实例；
     *
     * @return WebEventHandler： WebEventHandler的实例。
     */
    public static WebEventHandler create() {
        if (webEventHandler == null) {
            webEventHandler = new WebEventHandler();
        }
        return webEventHandler;
    }

    public String execute(BaseHybridFragment fragment, WebEvent event){

        Log.d(TAG, "execute: <<< OPERATION_TYPE <<< : "+event.getOperationType());
        return execute(fragment, event.toString());
    }


    @Override
    public String execute(BaseHybridFragment fragment, String params) {
        HANDLER.post(() -> {
            Logger.json(params);
            fragment.getWebView().loadUrl("javascript:webExecute(" + params + ")");
        });

        return null;
    }
}
