package com.mrlu.hybrid.proxy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.mrlu.hybrid.R;
import com.mrlu.hybrid.activityresult.ActivityResultHandlerManager;
import com.mrlu.hybrid.activityresult.IActivityResultHandler;


/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 15:28
 * Description:webView承载activity
 */
public abstract class BaseWebViewActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_webview);


    }

    protected void loadPate(String url) {
        final WebFragmentIml fragment = WebFragmentIml.create(url);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction tran = fragmentManager.beginTransaction();
        tran.replace(R.id.fl_base_webcontainer, fragment);
        tran.commitAllowingStateLoss();
    }

    /**
     * 当nativeEventHandler实现了{@link IActivityResultHandler}时候，首先需要在其实现类初始化的时候，调用
     * {@link ActivityResultHandlerManager}的#addHandler(handler)将其添加到manager中，然后就在其所依赖的activity
     * 的onActivityResult中调用  ActivityResultHandlerManager.getInstance().handleResult(requestCode, data);
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ActivityResultHandlerManager.getInstance().handleResult(requestCode, data);
        }

    }
}
