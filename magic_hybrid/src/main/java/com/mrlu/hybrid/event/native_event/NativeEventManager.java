package com.mrlu.hybrid.event.native_event;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 01:03
 * Description:Native事件管理者
 */
public class NativeEventManager {

    private final Map<String, BaseNativeEventHandler> EVENT_HANDLER_MAP = new HashMap<>();

    private final String EVENT_HANDLER = "EVENT_HANDLER";


    private NativeEventManager() {
    }

    private static class Holder {
        private final static NativeEventManager INSTANCE = new NativeEventManager();
    }

    public static NativeEventManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 添加一个新的事件处理者
     *
     * @param eventHandler BaseNativeEventHandler：新的事件处理者
     * @return NativeEventManager：事件管理者
     */
    public NativeEventManager addHandler(BaseNativeEventHandler eventHandler) {
        final BaseNativeEventHandler existHandler = existeHandler(eventHandler);
        if (existHandler != null) {
            replace(existHandler, eventHandler);
        } else {
            laterComersSurpassTheFormers(eventHandler);
        }
        return this;
    }


    /**
     * 长江后浪推前浪--将新添加进来重复的handler替换原有的，并将存储到原来的handler中的handler存放到后来的
     * handler中
     *
     * @param origin    原来已经存在的handler
     * @param newcommer 新添加的handler
     */
    private void replace(BaseNativeEventHandler origin, BaseNativeEventHandler newcommer) {
        //1.取出重复的handler的nextHandler
        final BaseNativeEventHandler next = origin.nextHandler();
        //2.然后存储到新来的handler的nextHandler
        newcommer.setNextHandler(next);
        //3.替换掉原来的
        origin = newcommer;
    }

    /**
     * 后来居上
     *
     * @param newcomer 新添加的EventHandler
     */
    private void laterComersSurpassTheFormers(BaseNativeEventHandler newcomer) {
        final BaseNativeEventHandler origin = EVENT_HANDLER_MAP.get(EVENT_HANDLER);
        if (origin != null) {
            newcomer.setNextHandler(origin);
        }
        EVENT_HANDLER_MAP.put(EVENT_HANDLER, newcomer);
    }

    /**
     * 遍历存储链得到与新添加的handler类型相同的handler
     *
     * @param newcommer BaseNativeEventHandler：新添加的handler
     * @return
     */
    private BaseNativeEventHandler existeHandler(BaseNativeEventHandler newcommer) {
        BaseNativeEventHandler handler = EVENT_HANDLER_MAP.get(EVENT_HANDLER);
        if (handler != null) {
            while (handler.nextHandler() != null) {
                if (handler.getHandleOperationType().equals(newcommer.getHandleOperationType())) {
                    return handler;
                }
                handler = handler.nextHandler();
            }
        }
        return null;
    }

    /**
     * 获取存储的NativeEvent-handler
     *
     * @return
     */
    public BaseNativeEventHandler getEventHandler() {
        return EVENT_HANDLER_MAP.get(EVENT_HANDLER);
    }
}
