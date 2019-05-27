package com.mrlu.hybrid.permission;

import com.mrlu.hybrid.IManager;

import java.util.HashMap;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by : mr.lu
 * Created at : 2019-05-27 at 13:45
 * Description:权限申请全局管理；
 * <H2>作用：</H2>
 * <p>因为我们的NativeEventhandler并不能直接执行activity相关事件，需要通过回调的方式，所以通过这种方式全局管理</p>
 */
public class PermissionsManager implements IManager<Integer, EasyPermissions.PermissionCallbacks> {

    private final Map<Integer, EasyPermissions.PermissionCallbacks> PERMISSIONS_MAP = new HashMap<>();


    private final static class Holder {
        private final static PermissionsManager INSTANCE = new PermissionsManager();
    }

    public static PermissionsManager getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public IManager add(Integer key, EasyPermissions.PermissionCallbacks target) {
        if (PERMISSIONS_MAP.containsKey(key)) {
            throw new IllegalArgumentException("please reset a completely unique code !");
        }
        PERMISSIONS_MAP.put(key, target);
        return this;
    }

    @Override
    public EasyPermissions.PermissionCallbacks get(Integer key) {
        return PERMISSIONS_MAP.get(key);
    }


}
