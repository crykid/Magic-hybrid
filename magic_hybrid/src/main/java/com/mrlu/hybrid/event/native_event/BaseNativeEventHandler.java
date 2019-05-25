package com.mrlu.hybrid.event.native_event;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mrlu.hybrid.event.web_event.WebEvent;
import com.mrlu.hybrid.event.web_event.WebEventHandler;
import com.mrlu.hybrid.proxy.BaseWebViewFragment;


/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 00:09
 * Description: Web请求Native执行的一个事件(NativeEvent) 的执行者
 *
 * <h2>功能描述：</h2>
 * <p>1.我们通过{@link NativeEvent} 可知，我们将Web主动与Native通信的一次行为抽象为一个<strong>事件</strong>
 * 。事件包含类型-OPERATION_TYPE,参数-params .然后我们会有多个处理者，每个处理者依据
 * <strong>单一职责原则</strong>,只处理自己责任内的一件事情--那就是自己操作符所限定的职责。我们将自己的操作符与
 * 事件的操作符比对，一旦匹配，那么该事件就有自身来执行。</p>
 * <p>2.在1中多次提到“职责”，很明显，不同的事件--多个功能专一处理者，“责任链模式”就很符合这种情况。我们采用链式结构
 * 将多个处理者依次存储，使用的时候逐个比对落实责任。</p>
 */
public abstract class BaseNativeEventHandler implements IEventHandler {
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
    private static final String TAG = "BaseNativeEventHandler";
    /**
     * <p>在使用的时候，我们的handler会在{@link NativeEventManager}中保存，而NativeEventManager是以<strong>单例</strong>的形式
     * 存在的，这就可能造成<strong>内存泄漏！！</strong></p>，所以我强烈建议在execute()最后阶段，手动释放一下。
     */
    protected BaseWebViewFragment fragment;
    protected Activity activity;

    private BaseNativeEventHandler nextHandler;

    /**
     * 执行事件的模板.
     * <p>*<strong>注</strong>：在具体情况中，可能会需要进行线程的切换，请自行在具体的实现类中的execute()方法中
     * 进行线程切换，handler已经为你准备好了</p>
     *
     * @param fragment Fragmetn： webView所在的fragment
     * @param event    NativeEvent：需要执行的事件
     * @return Stirng: native执行完事件后返回的内容，这个在不同情况下可能为空可能会有返回值
     */
    public final String handleEvent(BaseWebViewFragment fragment, NativeEvent event) {
        //如果事件的类型正好是当前执行者执行的类型，那么由当前执行者执行
        if (event.getRequestOperationType().equals(getHandleOperationType())) {
            this.activity = fragment.getActivity();
            this.fragment = fragment;
            return execute(fragment, event.getParams());
        } else {
            //当前处理者无法执行该事件，则交由后续处理者执行
            if (nextHandler != null) {
                return nextHandler.handleEvent(fragment, event);
            } else {
                Log.e(TAG, "handleEvent: Operation can not be execute !");
                return null;
            }
        }
    }

    /**
     * Native请求Web执行事件。
     * <p>
     * 在具体的NativeEventHandler中需要主动与Web交互，推荐使用该方法
     * 因为在此方法中我们已经释放了activity和fragment，如果不使用的话，还是需要自己去手动释放。</p>
     * <p>你可能会好奇这个方法的名字，我们在{@link WebEventHandler}中说到，它并不是真正的处理WebEvent，而是将
     * WebEvent传递给Web</p>
     *
     * @param event WebEvent
     */
    protected void passEventToWeb(WebEvent event) {
        WebEventHandler.create().execute(fragment, event);
        release();
    }


    @NonNull
    public abstract String getHandleOperationType();

    /**
     * 添加链式结构的下一个事件处理者
     *
     * @param nextHandler
     */
    public void setNextHandler(BaseNativeEventHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    /**
     * 获取下一个事件处理者
     *
     * @return BaseNativeEventHandler：连式结构中，下一个事件处理者
     */
    public BaseNativeEventHandler nextHandler() {
        return nextHandler;
    }


    public BaseWebViewFragment getFragment() {
        return fragment;
    }

    public final Activity getActivity() {
        return activity;
    }

    /**
     * 在使用完fragment或者Activity之后主动release以防止内存泄漏
     */
    protected void release() {
        activity = null;
        fragment = null;
    }


}
