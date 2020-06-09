package com.mrlu.magic_hybrid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mrlu.hybrid.event.native_event.NativeEventManager;
import com.mrlu.hybrid.proxy.BaseHybridActivity;
import com.mrlu.magic_hybrid.native_event_handler.MediaAlbumHandler;
import com.mrlu.magic_hybrid.native_event_handler.MediaCameraHandler;

/**
 * Created by : mr.lu
 * Created at : 2020/6/9 at 09:49
 * Description: 示例Activity
 */
public class MainActivity extends BaseHybridActivity {

    private final static String INTENT_TARGET_URL = "intent_target_url";

    /**
     * 跳转方法
     *
     * @param context
     * @param url
     */
    public static void start(Context context, String url) {
        context.startActivity(new Intent(context, MainActivity.class)
                .putExtra(INTENT_TARGET_URL, url));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //将我们的eventHandler添加进来
        NativeEventManager.getInstance()
                .addHandler(new MediaAlbumHandler())
                .addHandler(new MediaCameraHandler());

        //从别的页面跳转过来
//        Intent intent = getIntent();
//        String intent_url = intent.getStringExtra(INTENT_TARGET_URL);
//        loadPate(intent_url);

        //本地assets目录下的
        loadPate("magic_hybrid_page.html");
        //网页
//        loadPate("https://www.baidu.com");

    }
}
