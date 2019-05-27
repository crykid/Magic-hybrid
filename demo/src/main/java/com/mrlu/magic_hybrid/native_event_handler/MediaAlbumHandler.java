package com.mrlu.magic_hybrid.native_event_handler;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.mrlu.hybrid.activityresult.ActivityResultHandlerManager;
import com.mrlu.hybrid.activityresult.IActivityResultHandler;
import com.mrlu.hybrid.event.native_event.BaseNativeEventHandler;
import com.mrlu.hybrid.event.web_event.WebEvent;
import com.mrlu.hybrid.proxy.BaseWebViewFragment;
import com.mrlu.magic_hybrid.entity.AlbumEntity;
import com.mrlu.magic_hybrid.web_event.MagicWebEvents;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 17:21
 * Description: web调用原生相机
 */
public class MediaAlbumHandler extends BaseNativeEventHandler implements IActivityResultHandler {

    private final String IMAGE_TYPE = "image/*";
    private final String [] album_permission = {Manifest.permission.READ_EXTERNAL_STORAGE};

    public MediaAlbumHandler() {
        /*
        IActivityResultHandler使用方法：
        1.实现接口IActivityResultHandler;
        2.在实现类调用  ActivityResultHandlerManager.getInstance().addHandler(this);
        3.在依赖的activity的onActivityResult方法中使用ActivityResultHandlerManager.getInstance().handleResult()
         */
        ActivityResultHandlerManager.getInstance().addHandler(this);
    }

    @NonNull
    @Override
    public String getHandleOperationType() {
        return MagicNativeEvent.MEDIA_ALBUM.name();
    }

    @Override
    public String execute(BaseWebViewFragment fragment, String params) {

        try {
            JSONObject paramsJson = new JSONObject(params);
            String num = paramsJson.getString("num");
            //模拟打开相册操作
            Toast.makeText(fragment.getContext(), "打开相册，挑选" + num + "张照片", Toast.LENGTH_SHORT).show();

            new AlertDialog.Builder(fragment.getContext())
                    .setTitle("web调用native")
                    .setMessage("web调用native的相册，确定打开相册吗")
                    .setPositiveButton("确定", (dialog, which) ->
                            openAlbum()
                    )
                    .setNegativeButton("取消", (dialog, which) -> {
                    })
                    .show();


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 打开相册。以下代码仅供演示，实际会在相关工具类中
     */
    private void openAlbum() {
        // 判断api是否大于19（Android 4.4）
        boolean isKitKatO = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        Intent intent;
        if (isKitKatO) {
            // 6.0以上使用，获取图片路径
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.setType(IMAGE_TYPE);
        startActivityForResult(intent,5371);
    }

    @Override
    public int getRequestCode() {
        return 5371;
    }

    @Override
    public void handleActivityResult(Intent intent) {
//        Bitmap bm = null;
        ContentResolver resolver = getActivity().getContentResolver();
        try {
            Uri uri = intent.getData();
            Log.e("Tag", "Uri===" + uri);

            //bm = MediaStore.Images.Media.getBitmap(resolver, uri);
            //显得到bitmap图片
            //imgShow.setImageBitmap(bm);

//                  用Gilide加载uri
//                Glide.with(this).load(uri).into(imgShow);

            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = resolver.query(uri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                Log.e("Tag", "column_index===" + column_index);
                // 获取到图片路径
                String path = cursor.getString(column_index);
                Log.e("Tag", "path===" + path);

                passToWeb(path);


            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Tag", "Exception===" + e);
        }
    }

    /**
     * 将打开相册选择到的图片传递给Web。
     * <p>在以下代码中我只是把<strong>图片地址</strong>传递给Web用来<strong>演示</strong>将图片从Native传递给Web，
     * 当然实际情况肯定不能这么做!!!在我们项目中，图片是以base64的方式传递给Web的</p>
     *
     * @param path String：图片地址。
     */
    private void passToWeb(String path) {
        AlbumEntity albumEntity = new AlbumEntity(path, path, path);
        WebEvent<AlbumEntity> webEvent = new WebEvent<>(MagicWebEvents.MEDIA_IMAGE, albumEntity);

        passEventToWeb(webEvent);
    }

}
