package com.mrlu.hybrid.event.web_event;

import com.google.gson.Gson;

/**
 * Created by : mr.lu
 * Created at : 2019-05-24 at 10:09
 * Description:
 */
public class WebEvent<T> {

    /**
     * 操作符-事件类型
     */
    private String OPERATION_TYPE;

    /**
     * 需要给Web传递的参数
     */
    private T params;

    public WebEvent(Enum OPERATION_TYPE, T params) {
        this.OPERATION_TYPE = OPERATION_TYPE.name();
        this.params = params;
    }

    public WebEvent(Enum OPERATION_TYPE) {
        this.OPERATION_TYPE = OPERATION_TYPE.name();
    }

    public final String getOperationType() {
        return OPERATION_TYPE;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
