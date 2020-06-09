package com.mrlu.magic_hybrid.native_event_handler;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.mrlu.hybrid.event.native_event.BaseNativeEventHandler;
import com.mrlu.hybrid.proxy.BaseHybridFragment;

import pub.devrel.easypermissions.AfterPermissionGranted;

/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 17:36
 * Description:Web请求native执行事件demo；
 */
public class MediaCameraHandler extends BaseNativeEventHandler {

    private final String[] PERMISSIONS_CAMERA = {Manifest.permission.CAMERA};
    private final int PERMISSION_CAMERA = 123;

    public MediaCameraHandler() {

    }

    @NonNull
    @Override
    public Enum getHandleOperationType() {
        return MagicNativeEvent.MEDIA_CAMERA;
    }

    @Override
    public String execute(BaseHybridFragment fragment, String params) {
        new AlertDialog.Builder(fragment.getContext())
                .setTitle("web调用native")
                .setMessage("web调用native的相机，确定打开相机吗")
                .setPositiveButton("确定", (dialog, which) -> {
                    if (hasPermissions(PERMISSION_CAMERA, PERMISSIONS_CAMERA)) {
                        openCamera();
                    }

                })
                .setNegativeButton("取消", (dialog, which) -> {
                })
                .show();

        return null;
    }

    /**
     * EasyPermissions的应用
     */
    @AfterPermissionGranted(PERMISSION_CAMERA)
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 启动系统相机
        startActivityForResult(intent, 345);
    }
}
