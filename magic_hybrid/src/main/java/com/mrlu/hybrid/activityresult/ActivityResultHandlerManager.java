package com.mrlu.hybrid.activityresult;

import android.content.Intent;

import com.mrlu.hybrid.IManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by : mr.lu
 * Created at : 2019-05-24 at 10:32
 * Description:
 */
public class ActivityResultHandlerManager implements IManager<Integer, IActivityResultHandler> {

    private final Map<Integer, IActivityResultHandler> RESULTHANDLER_MAP = new HashMap<>();

    private ActivityResultHandlerManager() {
    }


    private final static class Holder {
        private final static ActivityResultHandlerManager INSTANCE = new ActivityResultHandlerManager();
    }

    public static ActivityResultHandlerManager getInstance() {
        return Holder.INSTANCE;
    }


//    public ActivityResultHandlerManager addHandler(IActivityResultHandler handler) {
//        if (handler.getRequestCode() < 10) {
//            throw new IllegalArgumentException("RequestCode should be at least double digits !");
//        }
//        if (RESULTHANDLER_MAP.containsKey(handler.getRequestCode())) {
//            throw new IllegalArgumentException("RequestCode has exit ! Please reset a completely unique code !");
//        }
//        RESULTHANDLER_MAP.put(handler.getRequestCode(), handler);
//        return this;
//    }
//
//    private final IActivityResultHandler getHandler(Object key) {
//        int requestCode = (int) key;
//
//        return RESULTHANDLER_MAP.get(requestCode);
//    }

    @Override
    public IManager add(Integer requestCode, IActivityResultHandler handler) {

        if (requestCode < 10) {
            throw new IllegalArgumentException("RequestCode should be at least double digits !");
        }
        if (RESULTHANDLER_MAP.containsKey(requestCode)) {
            throw new IllegalArgumentException("RequestCode has exit ! Please reset a completely unique code !");
        }
        RESULTHANDLER_MAP.put(requestCode, handler);
        return this;
    }

    @Override
    public IActivityResultHandler get(Integer requestCode) {

        return RESULTHANDLER_MAP.get(requestCode);
    }


    /**
     * @param requestCode int: startActivityForResult()时候的requestCode，同时也是{@link IActivityResultHandler}
     *                    的getRequestCode()返回的内容；
     * @param data
     */
    public final void handleResult(int requestCode, Intent data) {
//        final IActivityResultHandler RESULTHANDLER = getHandler(requestCode);
        final IActivityResultHandler RESULTHANDLER = get(requestCode);
        if (RESULTHANDLER != null) {
            RESULTHANDLER.handleActivityResult(data);
        }
    }

}
