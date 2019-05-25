package com.mrlu.magic_hybrid.native_event_handler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import com.mrlu.hybrid.event.native_event.BaseNativeEventHandler;
import com.mrlu.hybrid.proxy.BaseWebViewFragment;

/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 17:36
 * Description:
 */
public class MediaCameraHandler extends BaseNativeEventHandler {
    @NonNull
    @Override
    public String getHandleOperationType() {
        return MagicNativeEvent.MEDIA_CAMERA.name();
    }

    @Override
    public String execute(BaseWebViewFragment fragment, String params) {
        new AlertDialog.Builder(fragment.getContext())
                .setTitle("web调用native")
                .setMessage("web调用native的相机，确定打开相机吗")
                .setPositiveButton("确定", (dialog, which) -> {


                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();

        return null;
    }
}
