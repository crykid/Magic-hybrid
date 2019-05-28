# Magic Hybrid 魔法混合

## 核心思想：我们将Web(H5/js)--Native(原生/java/android）之间的交互简单的拆分为Web to Native, Native to Web 两种：

>1.Web to Native：
- 1.传递消息;
- 2.分析消息-消息转化为事件；
- 3.执行事件；

>2.Native to Web:
- 1.产生事件；
- 2.分析事件-事件转化为消息；
- 3.<strong>传递</strong>消息给Web

## 一.关于event

1.NativeEvent，web请求netive执行的事件。我们将web主动与native通信<strong>并
传递给Native的包含 请求native执行的事件的类型和执行该项事件必要的信息这两项内容
</strong>抽象为一个事件-NativeEvent；NativeEvent担负一个责任，就是需要将Web传
递给Native的数据-jsMsg,翻译成一个事件，其中必须准确的翻译出请求执行的事件的类型-OPERATION_TYPE，
以及事件必要的参数-params(params的格式很随意，可以是我们任何已有的类型）以便在
BaseNativeEventHandler执行的时候能够直接的拿到有效的数据；

```
public final class NativeEvent {

    private String OPERATION_TYPE = null;

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
                    this.OPERATION_TYPE = eventJsonObject.getString(OPERATION_TYPE);
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
        this.OPERATION_TYPE = requestOperationType;
        this.params = params;
    }

    /**
     * 当前事件的 操作符
     *
     * @return
     */
    public final String getRequestOperationType() {
        return OPERATION_TYPE;
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
```




>2.WebEvent,native主动请求web执行的事件。同NativeEvent，我们将Native主动与Web通信并<strong>传递给Web的包含请求Web执行的事件的类型以及执行该事件必要的信息这两项内容
</strong>抽象为一个事件-WebEvent；

```
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

```

## 二.IEventHandler ,事件执行者

### 1.BaseNativeEventHandler,NativeEvent 的执行者。BaseNativeEventHandler 是Native执行NativeEvent的核心，需要接触Native很多功能，所以包含以下相关功能

- 页面跳转回传startActivityForResult-IActivityResultHandler,

```

public abstract class BaseNativeEventHandler implements IEventHandler,  IActivityResultHandler {
  
  //...代码略...
  
  
   /**
     * 切记：如果想要使用onActivityResult则建议使用一下方式启动activity
     *
     * @param intent
     * @param requestCode
     */
    protected void startActivityForResult(Intent intent, int requestCode) {
        if (activity != null) {
            activity.startActivityForResult(intent, requestCode);
            //将当前IActivityResultHandler回调添加到管理器中
            ActivityResultHandlerManager.getInstance().add(requestCode, this);
        }
    }
    
}

```

```
/**
 * Created by : mr.lu
 * Created at : 2019-05-22 at 15:28
 * Description:webView承载activity
 */
public abstract class BaseWebViewActivity extends AppCompatActivity {


    //略。。。

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

```

```
/**
 * Created by : mr.lu
 * Created at : 2019-05-24 at 10:32
 * Description:
 */
public class ActivityResultHandlerManager implements IManager<Integer, IActivityResultHandler> {

    private final Map<Integer, IActivityResultHandler> RESULTHANDLER_MAP = new HashMap<>();
    
    //略。。。

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
        final IActivityResultHandler RESULTHANDLER = get(requestCode);
        if (RESULTHANDLER != null) {
            RESULTHANDLER.handleActivityResult(data);
        }
    }

}
```

>> 可以看到，我们将requestCode--IActivityResultHandler以n对1的方式在使用的时候存储起来，在使用的时候可以根据requestCode区分
使用哪个回调或区分回调处理那个事件；

- 权限相关，requestPermissions-EasyPermissions,我们在实际情况很可能会碰到权限相关的事件，所以我封装了EasyPermissions

```
public abstract class BaseNativeEventHandler implements IEventHandler, EasyPermissions.PermissionCallbacks{

    /**
     * 检查是否有权限并主动申请权限
     *
     * @param requestCode
     * @param permissions
     * @return
     */
    protected boolean hasPermissions(int requestCode, String[] permissions) {

        if (EasyPermissions.hasPermissions(getActivity(), permissions)) {
            return true;
        } else {
            EasyPermissions.requestPermissions(getFragment(), "为了您能正常使用,请开启响应权限!", requestCode, permissions);
            //将当前回调添加到管理器中
            PermissionsManager.getInstance().add(requestCode, this);
            return false;
        }
    }

    /**
     * EasyPermission.PermissionCallbacks
     *
     * @param i
     * @param strings
     * @param ints
     */
    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strings, @NonNull int[] ints) {

    }
    /**
     * EasyPermission.PermissionCallbacks
     * 可以看到这里我并没有做任何处理。在权限通过后有两种方式来执行后续的事情，第一重写该方法，第二使用{@link AfterPermissionGranted}
     * 注解来注解一个权限通过后将要执行的public方法。
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    /**
     * EasyPermission.PermissionCallbacks
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void handleActivityResult(Intent intent) {

    }

}

```

>> 在WebView承载类fragment中

```
public abstract class BaseWebViewFragment extends Fragment implements IWebViewInitializer {

    //...
 @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //EasyPermissions代理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, PermissionsManager.getInstance().get(requestCode));
    }
    
    //...

}

```

>>在PermissionsManager中

```

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

```

>> 具体类中配合EasyPermissions的注解请求权限，当然也可以重写父类的方法以requestCode来区分：

```
public class MediaCameraHandler extends BaseNativeEventHandler {

    private final String[] PERMISSIONS_CAMERA = {Manifest.permission.CAMERA};
    private final int PERMISSION_CAMERA = 123;

    public MediaCameraHandler() {

    }

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

```

>>可以看到，我们的EventHandler本身并不具有其承载类权限相关的功能，我只有通过回调的方式来实现，原理很简单，相信您一看就懂


>2.WebEventHandler，WebEvent的执行者。需要特别注意的是，WebEventHandler不同于NativeEventHandler，<strong>它并不执行事件，只是将事件传递给Web！</strong>

```

/**
 * Created by : mr.lu
 * Created at : 2019-05-23 at 12:44
 * Description:<h2>Native请求web执行的事件处理者；</h2><br>
 * <p>与其说这是一个Native请求web执行的事件的执行者，倒不如说这是一个<strong>中介</strong>，因为它其实并不会执行
 * 具体的事件，他只负责将我们Native请求Web执行的事件传递给Web；</p>
 */
public class WebEventHandler implements IEventHandler {
    private static final String TAG = "WebEventHandler";

    private static WebEventHandler webEventHandler = null;
    private final Handler HANDLER;

    private WebEventHandler() {
        HANDLER = MagicConfigurator.getInstance().getConfig(ConfigEnum.HANDLER);
    }

    /**
     * 因为WebEventHandler并不像{@link BaseNativeEventHandler}一样负责处理具体事情并且会有多种类型，所以我们
     * 不需要对每种事件做一个单独的封装，只需要传递一下事件而已，所以我不太希望这个它重复的创建多个实例；
     *
     * @return WebEventHandler： WebEventHandler的实例。
     */
    public static WebEventHandler create() {
        if (webEventHandler == null) {
            webEventHandler = new WebEventHandler();
        }
        return webEventHandler;
    }

    public String execute(BaseWebViewFragment fragment,WebEvent event){

        Log.d(TAG, "execute: <<< OPERATION_TYPE <<< : "+event.getOperationType());
        return execute(fragment, event.toString());
    }


    @Override
    public String execute(BaseWebViewFragment fragment, String params) {
        HANDLER.post(() -> {
            Logger.json(params);
            fragment.getWebView().loadUrl("javascript:webExecute(" + params + ")");
        });

        return null;
    }
}

```




## 三.执行事件

>1.Web请求Native--NativeEventHandler 执行 NativeEvent,在WebInterface中:

```
  @JavascriptInterface
    @SuppressWarnings("unused")
    public String nativeExecute(String jsMsg) {

        // TODO: 2019-05-24 注意这里可能有线程切换的问题 ，如果有需要，后续可以在具体handler中自己切换线程
        
        //1.获取NativeEventHandler；
        final BaseNativeEventHandler eventHandler = NativeEventManager.getInstance().getEventHandler();
        //2.将jsMsg包装秤一个NativeEvent；
        final NativeEvent nativeEvent = new NativeEvent(jsMsg);
        Log.d(TAG, "nativeExecute: >>> OPERATION_TYPE >>> : " + nativeEvent.getRequestOperationType());
        Logger.json(jsMsg);
        //3.执行该事件。
        return eventHandler.handleEvent(FRAGMENT, nativeEvent);
    }

```

>2.Native请求Web--WebEventHandler传递WebEvent：

```
public class TestHandler extends BaseNativeEventHandler {


    private void anyMethod(){
        AlbumEntity albumEntity = new AlbumEntity(path, path, path);
        WebEvent<AlbumEntity> webEvent = new WebEvent<>(MagicWebEvents.MEDIA_IMAGE, albumEntity);

        passEventToWeb(webEvent);
       }
    private void passEventToWeb(WebEvent webEvent){
        WebEventHandler.create().execute(fragment, event);
        release();
    }
    
    private void release(){
    
        activity = null;
        fragment = null;
    }
        
```

## 四.全局配置

### 在我们实际应用中，这个框架很可能是基于一个module来使用，所以我们的module不用一些其它方法是无法获取到主module的信息的，
为此我提供了以下一些基本的配置：

```
/**
 * Created by : mr.lu
 * Created at : 2019-05-21 at 23:23
 * Description: Hybrid配置管理，建议在app的Applicaiton或webview初始化之前添加以下配置
 */
public class MagicConfigurator {

    private final Map<ConfigEnum, Object> CONFIGS_MAP = new HashMap<>();


    private MagicConfigurator() {
        CONFIGS_MAP.put(ConfigEnum.DEBUG, false);
    }

    private static final class Holder {
        private final static MagicConfigurator INSTANCE = new MagicConfigurator();
    }

    public static MagicConfigurator getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 是否debug
     *
     * @param debug
     * @return
     */
    public MagicConfigurator debug(boolean debug) {
        CONFIGS_MAP.put(ConfigEnum.DEBUG, debug);
        return this;
    }

    /**
     * 主线程的handler
     *
     * @param handler
     * @return
     */
    public MagicConfigurator handler(Handler handler) {
        CONFIGS_MAP.put(ConfigEnum.HANDLER, handler);
        return this;
    }

    /**
     * ApplicationContext
     *
     * @param context
     * @return
     */
    public MagicConfigurator context(Context context) {
        CONFIGS_MAP.put(ConfigEnum.APPLICATION_CONTEXT, context);
        return this;
    }

    /**
     * 交互协议
     *
     * @param bridge
     * @return
     */
    public MagicConfigurator hybridBridge(@NonNull String bridge) {
        CONFIGS_MAP.put(ConfigEnum.APPLICATION_CONTEXT, bridge);
        return this;
    }

    /**
     * 如果访问外部H5，在此配置H5 Host
     *
     * @param webHost
     * @return
     */
    public MagicConfigurator webHost(@NonNull String webHost) {
        CONFIGS_MAP.put(ConfigEnum.WEB_API_HOST, webHost);
        return this;
    }


    /**
     * 获取配置
     *
     * @param configEnum
     * @param <T>
     * @return
     */
    public <T> T getConfig(ConfigEnum configEnum) {

        return (T) CONFIGS_MAP.get(configEnum);
    }

}

```

>> 我建议您在Application或者WebView初始化之前调用该配置类添加具体配置。
