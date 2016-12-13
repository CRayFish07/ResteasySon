# ResteasySon
Resteasy 二次开发


- [目录](#目录)
    - [需求背景](#需求背景)
    - [实现方案](#实现方案)

## 需求背景

Resteasy原生的JSON反序列化，使用的是Jackson的ObjectReader，基于流式风格的JSON反序列化，且ObjectReader没有做定制化的反序列化配置。

这种会有什么问题呢？

* 系统之间进行交互，domain之间的属性约束比较弱，双方都可以随意加字段，就会出现domain的属性不对称，调用时就会报字段不识别的异常；
* JSON的属性值为空字符串，可能会报错；
* 不支持固定格式日期转换；
* 等等

这些特性，其实Jackson都支持，只需要对ObjectReader做一些属性的配置。

## 实现方案

#### Resteasy序列化组件加载原理

- ResteasyBootstrap.contextInitialized()
    - deployment = config.createDeployment()，创建ResteasyDeployment，此对象完成组件的初始化
        - deployment = super.createDeployment()
            - ConfigurationBootstrap.createDeployment()，完成ResteasyDeployment的属性定制化，也是方案2的实现原理
                - String providers = getParameter(ResteasyContextParameters.RESTEASY_PROVIDERS)
    - deployment.start()，此方法实现了组件的初始化
        - RegisterBuiltin.register(providerFactory)，加载项目中META-INF\services下，声明的默认扩展组件
            - registerProviders(factory)，实现组件初始化的核心逻辑，方案1的实现原理

#### 定制化方案实现原理

方案1、重写Resteasy的核心类ResteasyBootstrap、ResteasyDeployment、RegisterBuiltin，在RegisterBuiltin加载序列化组件时，替换成自定义的组件。

```xml
<listener>
    <listener-class>com.yollock.resteasy.boostrap.YolResteasyBootstrap</listener-class>
</listener>
```
使用此类，需要依赖ResteasySon包。替换组件的逻辑，写在YolRegisterBuiltin类。

说明：对Resteasy的核心类进行了重写，有一定的侵入性，但是定制化高，可以对多余的组件进行灵活控制，实现最大化的性能。

因为Resteasy默认会加载40-50个序列化组件，每次请求，都会对这些组件做两次检索。例如【application/json】，第一次检索，会留下13个组件，第二次检索，最终选择RestEasyJackson2Provider组件。
每一个组件的过滤，都会进行比较复杂的逻辑判断，比较浪费CPU。其实我们的项目中，貌似只用到了RestEasyJackson2Provider这一个组件。
 
方案2、利用ResteasyDeployment的创建时的属性扩展机制，在web.xml中添加属性resteasy.providers，此属性的值，就是自定义组件的完整类名。

```xml
<context-param>
    <param-name>resteasy.providers</param-name>
    <param-value>com.yollock.resteasy.provider.YolRestEasyJackson2Provider</param-value>
</context-param>
<listener>
    <listener-class>org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap</listener-class>
</listener>
```
 
同样依赖ResteasySon包。

说明：没有侵入性，使用的是原生的Resteasy代码，以及其扩展机制。


