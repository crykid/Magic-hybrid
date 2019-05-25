package com.mrlu.magic_hybrid;

import android.os.Bundle;

import com.mrlu.hybrid.event.native_event.NativeEventManager;
import com.mrlu.hybrid.proxy.BaseWebViewActivity;
import com.mrlu.magic_hybrid.native_event_handler.MediaAlbumHandler;
import com.mrlu.magic_hybrid.native_event_handler.MediaCameraHandler;

/**
 *
 */
public class MainActivity extends BaseWebViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //将我们的eventHandler添加进来
        NativeEventManager.getInstance()
                .addHandler(new MediaAlbumHandler())
                .addHandler(new MediaCameraHandler());

        //本地assets目录下的
        loadPate("magic_hybrid_page.html");
        //网页
//        loadPate("https://www.baidu.com");

    }
}
