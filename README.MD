# Magic Hybrid 混合开发框架

标签： Android架构师之路

---


>[Magic-Hybrid][1]架构是从我自己写的，历时一年多的优化更改，才呈现出现在的模样，一直以来都懒得写出发，现在有时间把它发出来。
我并没有对比市面上其它的架构，但是我猜原理都是一样的。我自认为Magic-Hybrid长处在于，Native功能、模块、业务处理简单、解耦。因为它充分利用设计模式，面向接口、单一职责、依赖倒置等原则，同时又不刻意；众多的业务功能模块彼此独立调用简单，优势展现的淋漓尽致。


---

# 用法

## 1.初始化

在Application的onCreate中调用MagicConfigurator初始化必要的参数：
```java
     //配置Hybrid
        MagicConfigurator.getInstance()
                .context(this)//ApplicationContext
                .handler(new Handler(getMainLooper()))
                .debug(BuildConfig.DEBUG)//是否debug
                .hybridBridge("MAGIC_HYBRID_BRIDGE");//交互协议
```

使用继承自**BaseHybridActivity**的Activity：

```java
public class MainActivity extends BaseHybridActivity {

    private final static String INTENT_TARGET_URL = "intent_target_url";

    /**
     * 跳转方法
     *
     * @param context
     * @param url
     */
    public static void start(Context context, String url) {
        context.startActivity(new Intent(context, MainActivity.class)
                .putExtra(INTENT_TARGET_URL, url));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //将我们的eventHandler添加进来
        NativeEventManager.getInstance()
                .addHandler(new MediaAlbumHandler())
                .addHandler(new MediaCameraHandler());

        //从别的页面跳转过来
//        Intent intent = getIntent();
//        String intent_url = intent.getStringExtra(INTENT_TARGET_URL);
//        loadPate(intent_url);

        //本地assets目录下的
        loadPate("magic_hybrid_page.html");
        //网页
//        loadPate("https://www.baidu.com");

    }
}
```
或者单独使用继承自BaseHybridFragment的**WebFragmentIml**,可以搭配FragmLayout或者ViewPager。

## 2. 执行事件

### 2.1. Web请求Native--NativeEventHandler 执行 NativeEvent:

在适当的时机初始化NativeEventHandler之后，还需要在WebInterface中:

```java
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

### 2.2 Native请求Web--WebEventHandler传递WebEvent：

```java
public class TestHandler extends BaseNativeEventHandler {


    private void anyMethod(){
        AlbumEntity albumEntity = new AlbumEntity(path, path, path);
        WebEvent<AlbumEntity> webEvent = new WebEvent<>(MagicWebEvents.MEDIA_IMAGE, albumEntity);

        passEventToWeb(webEvent);
       }
    private void passEventToWeb(WebEvent webEvent){
        WebEventHandler.create().execute(fragment, event);
        //如果持有了相关引用，使用完赶紧释放
        release();
    }
    
    private void release(){
    
        activity = null;
        fragment = null;
    }
        
```

---

#原理分析

---


## 1. 两个端 

1.Web：不管是JS、H5、网页前端，我们统称为Web；

2.Native：不管是Android、IOS、app、还是移动端，我们统称为Native。


--- 

## 2. 两个方向 —— 一个核心

我们将Web <——> Native之间的交互拆分为两个方向：**Web to Native，Native to Web**。然而不论是那种，其**核心思想都是消息的传递**，我认为都应该细分为以下几个步骤：


- 1.产生事件：将事件类型和必要参数信息组合产生一个事件；
- 2.转化消息：将事件转化为可以传递的消息；
- 3.传递消息：将消息从一端传递到另一端；
- 4.分析事件：接收端将消息转化为可以执行的事件；
- 5.执行事件：接收端根据事件类型和必要的参数信息执行事件。


--- 

## 3. 两个方法


可以看出来，不论是哪一端到哪一端，交互核心都是消息的传递。因此，我认为在Hybrid交互过程中，
交互方式的重点应该是消息的传递和事件的类型，而不是方法;

另一方面，Web和Native两端应该避免
过多的耦合，以适应需求的快速变更和迭代，因此两端应该对彼此知道的越少越好，最终彼此只需要暴露一个方法供对方调用来传递消息。因此，最终只需要一个方法:

- 1.String nativeExecute(String jsMsg):Native需要实现的方法，Web给Native传递消息时调用的方法；
- 2.webExecute(String nativeMsg):Web需要实现的方法，Native给Web传递消息时调用的方法；

---

## 4. 两个事件


由上述第二条可知，交互分为两部分，二者对应的事件为：


- NativeEvent——Web to Native；
- WebEvent——Native to Web 。

```text
接下来介绍详情：
```


### 4.1. NativeEvent:web请求netive执行的事件

我们将web主动与native通信并传递给Native的包含 请求native执行的<strong>事件的类型和执行该项事件必要的信息</strong>这两项内容合并抽象为一个事件-NativeEvent；
NativeEvent担负一个责任，就是需要将Web传递给Native的数据-jsMsg,翻译成一个事件，其中必须准确的翻译出请求执行的事件的类型-OPERATION_TYPE，以及事件必要的参数-params(params的格式很随意，可以是我们任何已有的类型）以便在BaseNativeEventHandler执行的时候能够直接的拿到有效的数据；

```java
public final class NativeEvent {

    //事件类型
    private String OPERATION_TYPE = null;
    //必要的参数信息
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




### 4.2. WebEvent,native主动请求web执行的事件。
同NativeEvent，我们将Native主动与Web通信并传递给Web的包含请求Web执行的<strong>事件的类型以及执行该事件必要的信息</strong>这两项内容合并
抽象为一个事件-WebEvent；

```java
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




## 5.  两个事件的执行者 -- IEventHandler

同上述-3-，事件执行的重点应该是消息的事件的类型和必要，而不是方法;
因此，最终只需要一个方法:

```java
public interface IEventHandler {

    String execute(BaseWebViewFragment fragment, String params);
}
```
### 5.1. BaseNativeEventHandler,NativeEvent 的执行者。

此情此景，感觉适合用责任链模式，于是就用责任链模式吧

```java
public class NativeEventManager {

    private final Map<String, BaseNativeEventHandler> EVENT_HANDLER_MAP = new HashMap<>(1);

    private final String EVENT_HANDLER = "EVENT_HANDLER";

    private EventManager() {
    }

    private static class Holder {
        private static final NativeEventManager INSTANCE = new NativeEventManager();
    }

    public static NativeEventManager getInstance() {

        return Holder.INSTANCE;
    }

    /**
     * 添加Hybrid事件处理者；<br>
     * <p>我们的EventManger是一个喜新厌旧却不贪心的管理器：可以放心的是，尽管调用当前方法的时候，我们对
     * BaseNativeEventHandler{@link BaseNativeEventHandler}
     * 创造新的实例并没有做限制，这意味着前后可能有多个相同EventHandler的实例会被传递过来，但是我们的内心始终
     * 只有一个位置，尽管来来往往的handler不断，我们内心只允许一个handler停留--后来的取代早先来的。
     * <p>
     *
     * @param eventHandler BaseHybridEventHandler的子类实例。
     * @return
     */
    public NativeEventManager addEvent(BaseNativeEventHandler eventHandler) {

        final BaseNativeEventHandler existHandler = existHandler(eventHandler);
        //如果已有就替换
        if (existHandler != null) {
            replace(existHandler, eventHandler);
        } else {
            laterComersSurpassTheFormers(eventHandler);
        }
        return this;
    }


    /**
     * 检查并取出与即将要添加的处理类型相同的Handler
     *
     * @param eventHandler 即将要添加的handler
     * @return 与即将要添加的处理类型相同的Handler
     */
    private BaseNativeEventHandler existHandler(BaseNativeEventHandler eventHandler) {

        BaseNativeEventHandler handler = EVENT_HANDLER_MAP.get(EVENT_HANDLER);
        if (handler != null) {
            while (handler.nextHandler() != null) {
                if (handler.getHandleOperationType().equals(eventHandler.getHandleOperationType())) {
                    return handler;
                }
                handler = handler.nextHandler();
            }

        }
        return null;
    }

    /**
     * 长江后浪推前浪--将新添加进来重复的handler替换原有的
     *
     * @param origin 原来的handler
     * @param target 新的handler
     */
    private void replace(BaseNativeEventHandler origin, BaseNativeEventHandler target) {
        //1.取出重复的 原来的handler的nextHandler，
        final BaseNativeEventHandler next = origin.nextHandler();
        //2.然后赋给当前handler的nextHandler
        target.setNextHandler(next);
        //3.将原来存储链中的替换掉
        origin = target;
    }

    /**
     * 后来居上
     *
     * @param eventHandler
     */
    private void laterComersSurpassTheFormers(BaseNativeEventHandler eventHandler) {
        //取出上一个handler，将其设置为新的handler的nextHandler；
        //将之前存储的handler存储到新的handler中，然后将新的handler放入map中
        BaseNativeEventHandler beh = EVENT_HANDLER_MAP.get(EVENT_HANDLER);
        if (beh != null) {
            eventHandler.setNextHandler(beh);
        }
        EVENT_HANDLER_MAP.put(EVENT_HANDLER, eventHandler);

    }

    /**
     * 获取所有的HybridEventHandler
     *
     * @return 所有的HybridEventHandler。
     */
    public final BaseNativeEventHandler getEventHandler() {
        return EVENT_HANDLER_MAP.get(EVENT_HANDLER);
    }
}

```


##### BaseNativeEventHandler 是Native执行NativeEvent的核心。应包含以下功能：

- 判断是否是自己执行的事件类型
- 设置和获取下个NavtiveEventHandler

```java

public abstract class BaseNativeEventHandler implements IEventHandler {
 
    private static final String TAG = "BaseNativeEventHandler";

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
        final String name = getHandleOperationType().name();
        if (event.getRequestOperationType().equals(name)) {
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
    /**
    *获取当前NativeEventHandler执行的事件类型，采用Enum的方式定义。
    */
    @NonNull
    public abstract Enum getHandleOperationType();
    }

    /**
     * 设置写一个处理者
     *
     * @param nextHandler 下一个处理者
     */
    public void setNextHandler(BaseHybridEventHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
    public BaseHybridEventHandler nextHandler() {
        return nextHandler;
    }
``` 

##### 同时，BaseNativeEventHandler 还需要接触Native很多功能，所以包含以下相关功能：

- 页面跳转回传startActivityForResult-IActivityResultHandler,

```java

public abstract class BaseNativeEventHandler implements IEventHandler,  IActivityResultHandler {
  
  //...代码略...
  
  
   /**
     * 切记：如果想要使用onActivityResult则建议使用这种方式启动activity
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

```java
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

```java
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

> 可以看到，我们将requestCode--IActivityResultHandler以n对1的方式在使用的时候存储起来，在使用的时候可以根据requestCode区分
使用哪个回调或区分回调处理那个事件；

- 权限相关，requestPermissions-EasyPermissions,我们在实际情况很可能会碰到权限相关的事件，所以我封装了EasyPermissions

```java
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

```java
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

```java

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

```java
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


### 5.2. WebEventHandler，WebEvent的执行者。
需要特别注意的是，WebEventHandler不同于NativeEventHandler，<strong>它并不执行事件，只是将事件传递给Web！</strong>

```java

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






## 6. 全局配置

 在我们实际应用中，这个框架很可能是基于一个module来使用，所以我们的module不用一些其它方法是无法获取到主module的信息的，
为此我提供了以下一些基本的配置：

```java
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

我建议您在Application或者WebView初始化之前调用该配置类添加具体配置。


---

[CSDN : Magic-Hybrid][1]

[1]:https://blog.csdn.net/blankmargin/article/details/106625803





