# Magic Hybrid 神奇混合

## 一.关于event

>1.NativeEvent，web请求netive执行的事件。我们将web主动与native<strong>通信</strong>并请求native
<strong>执行</strong>或<strong>执行并返回内容</strong>的一个过程抽象为一个事件-NativeEvent,并将每个事件对应的执行者封装为一个请求处理者-NativeEventHandler；


>2.WebEvent,native主动请求web执行的事件。同NativeEvent，我们将native主动请求Web执行的一个事件的<strong>过程</strong>抽象为一个事件