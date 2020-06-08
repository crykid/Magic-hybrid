package com.mrlu.magic_hybrid;

import android.app.Application;
import android.os.Handler;

import com.mrlu.hybrid.BuildConfig;
import com.mrlu.hybrid.config.MagicConfigurator;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.tencent.smtt.sdk.QbSdk;

/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 15:40
 * Description:
 */
public class MagicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();

        //配置Hybrid
        MagicConfigurator.getInstance()
                .context(this)
                .handler(new Handler(getMainLooper()))
                .debug(BuildConfig.DEBUG)
                .hybridBridge("MAGIC_HYBRID_BRIDGE");

        //腾讯X5浏览器-2,x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), null);

    }

    private void initLogger() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)
                .methodCount(0)
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }

            @Override
            public void log(int priority, String tag, String message) {
                super.log(Logger.ERROR, tag, message);
            }
        });
    }
}
