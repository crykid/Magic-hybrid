package com.mrlu.hybrid.event.native_event;

import android.support.annotation.NonNull;


import com.google.gson.Gson;
import com.mrlu.hybrid.webview.HybridAgreementEnum;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 00:13
 * Description:Web主动请求Native执行的一个行为/事件，我们抽象其为一个需要Native执行的一个事件；
 *
 * <h2>NativeEvent的职责:</h2>
 * <p>我们在WEB调用了Native的 jsEvent(String msg)方法后，将msg包装成一个事件Event，然后让专门的
 * EventHandler处理;它的工作只有一个内容两个步骤：1.分析得到OPERATION_TYPE；2.分析得到PARAMS。</p>
 * <h2>意义：</h2>
 * <p>
 * 我们把原本eventHandler中关于Event的OPERATION_TYPE、params的分析获取的工作放到这里，使eventHandler能够
 * 更加专注的处理它应该处理的事情————那就是根据event的OPERATION_TYPE执行相关的事情！而不需要分析OPERATION_TYPE和
 * params是什么
 * <h2>NativeEvent处理的消息格式:</h2>
 * {
 * "OPERATION_TYPE": "REQUEST_ENCRYPT",//操作符
 * "params": {
 * "phone": "13512345678",
 * "password": "qwer1234"
 * ...
 * }
 *  }
 */
public final class NativeEvent {


    private String requestOperationType = null;

    private String params = null;


    /**
     * jsMsg 格式
     * {
     * "OPERATION_TYPE": "REQUEST_ENCRYPT",//操作符
     * "params": {
     * "phone": "13512345678",
     * "password": "qwer1234"
     * ...
     * }
     *  }
     */

    /**
     * 在这个构造函数中，我们将Web传递给Native消息体转化为包含操作符和关键参数字段的一个<strong>事件</strong>
     *
     * @param jsMsg Stirng:Web传递给Native消息体
     */
    public NativeEvent(@NonNull String jsMsg) {
        if (jsMsg != null && jsMsg.length() > 0) {
            try {
                JSONObject eventJsonObject = new JSONObject(jsMsg);

                //1.得到requestOperationType
                final String OPERATION_TYPE = HybridAgreementEnum.OPERATION_TYPE.name();
                if (jsMsg.contains(OPERATION_TYPE)) {
                    requestOperationType = eventJsonObject.getString(OPERATION_TYPE);
                }
                //2.得到params

                final String PARAMS = HybridAgreementEnum.params.name();
                if (jsMsg.contains(PARAMS)) {
                    params = eventJsonObject.getString(PARAMS);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 在一些特殊的情况下，我们可能会需要自己创造一个web事件
     *
     * @param requestOperationType String ：必须是大写的操作符
     * @param params               json格式的参数
     */
    public NativeEvent(@NonNull String requestOperationType, @NonNull String params) {
        this.requestOperationType = requestOperationType;
        this.params = params;
    }

    /**
     * 当前事件的 操作符
     *
     * @return
     */
    public final String getRequestOperationType() {
        return requestOperationType;
    }

    /**
     * 当前事件的参数
     *
     * @return
     */
    public String getParams() {
        return params;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }


}
