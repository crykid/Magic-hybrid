package com.mrlu.hybrid.config;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by : mr.lu
 * Created at : 2019-05-21 at 23:23
 * Description: Hybrid配置管理，建议在app的Applicaiton或webview初始化之前添加一下配置
 */
public class MagicConfigurator {

    private final Map<ConfigEnum, Object> CONFIGS_MAP = new HashMap<>();


    private MagicConfigurator() {
        CONFIGS_MAP.put(ConfigEnum.DEBUG, false);
    }

    private static final class Holder {
        private final static MagicConfigurator INSTANCE = new MagicConfigurator();
    }

    public static MagicConfigurator getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 是否debug
     *
     * @param debug
     * @return
     */
    public MagicConfigurator debug(boolean debug) {
        CONFIGS_MAP.put(ConfigEnum.DEBUG, debug);
        return this;
    }

    /**
     * 主线程的handler
     *
     * @param handler
     * @return
     */
    public MagicConfigurator handler(Handler handler) {
        CONFIGS_MAP.put(ConfigEnum.HANDLER, handler);
        return this;
    }

    /**
     * ApplicationContext
     *
     * @param context
     * @return
     */
    public MagicConfigurator context(Context context) {
        CONFIGS_MAP.put(ConfigEnum.APPLICATION_CONTEXT, context);
        return this;
    }

    /**
     * 交互协议
     *
     * @param bridge
     * @return
     */
    public MagicConfigurator hybridBridge(@NonNull String bridge) {
        CONFIGS_MAP.put(ConfigEnum.APPLICATION_CONTEXT, bridge);
        return this;
    }

    /**
     * 如果访问外部H5，在此配置H5 Host
     *
     * @param webHost
     * @return
     */
    public MagicConfigurator webHost(@NonNull String webHost) {
        CONFIGS_MAP.put(ConfigEnum.WEB_API_HOST, webHost);
        return this;
    }


    /**
     * 获取配置
     *
     * @param configEnum
     * @param <T>
     * @return
     */
    public <T> T getConfig(ConfigEnum configEnum) {

        return (T) CONFIGS_MAP.get(configEnum);
    }

}
