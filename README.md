# RPC
## 什么是RPC

RPC（Remote Procedure Call Protocol）远程过程调用协议。一个通俗的描述是：客户端在不知道调用细节的情况下，调用存在于远程计算机上的某个对象，就像调用本地应用程序中的对象一样。

比较正式的描述是：一种通过网络从远程计算机程序上请求服务，而不需要了解底层网络技术的协议。

那么我们至少从这样的描述中挖掘出几个要点：

-  RPC是协议：既然是协议就只是一套规范，那么就需要有人遵循这套规范来进行实现。目前典型的RPC实现包括：Dubbo、Thrift、GRPC、Hetty等。 -  网络协议和网络IO模型对其透明：既然RPC的客户端认为自己是在调用本地对象。那么传输层使用的是TCP/UDP还是HTTP协议，又或者是一些其他的网络协议它就不需要关心了。 -  信息格式对其透明：我们知道在本地应用程序中，对于某个对象的调用需要传递一些参数，并且会返回一个调用结果。至于被调用的对象内部是如何使用这些参数，并计算出处理结果的，调用方是不需要关心的。那么对于远程调用来说，这些参数会以某种信息格式传递给网络上的另外一台计算机，这个信息格式是怎样构成的，调用方是不需要关心的。 -  应该有跨语言能力：为什么这样说呢？因为调用方实际上也不清楚远程服务器的应用程序是使用什么语言运行的。那么对于调用方来说，无论服务器方使用的是什么语言，本次调用都应该成功，并且返回值也应该按照调用方程序语言所能理解的形式进行描述。 
   <img alt="" src="https://img-blog.csdnimg.cn/img_convert/6ed1fcd28fe5b3661a00d248708de815.png">

## 为什么要用RPC

其实这是应用开发到一定的阶段的强烈需求驱动的。如果我们开发简单的单一应用，逻辑简单、用户不多、流量不大，那我们用不着。当我们的系统访问量增大、业务增多时，我们会发现一台单机运行此系统已经无法承受。此时，我们可以将业务拆分成几个互不关联的应用，分别部署在各自机器上，以划清逻辑并减小压力。此时，我们也可以不需要RPC，因为应用之间是互不关联的。

当我们的业务越来越多、应用也越来越多时，自然的，我们会发现有些功能已经不能简单划分开来或者划分不出来。此时，可以将公共业务逻辑抽离出来，将之组成独立的服务Service应用 。而原有的、新增的应用都可以与那些独立的Service应用 交互，以此来完成完整的业务功能。

所以此时，我们急需一种高效的应用程序之间的通讯手段来完成这种需求，所以你看，RPC大显身手的时候来了！

其实描述的场景也是服务化 、微服务和分布式系统架构的基础场景。即RPC框架就是实现以上结构的有力方式。

## 常用的RPC框架

-  Thrift：thrift是一个软件框架，用来进行可扩展且跨语言的服务的开发。它结合了功能强大的软件堆栈和代码生成引擎，以构建在 C++, Java, Python, PHP, Ruby, Erlang, Perl, Haskell, C#, Cocoa, JavaScript, Node.js, Smalltalk, and OCaml 这些编程语言间无缝结合的、高效的服务。 -  gRPC：一开始由 google 开发，是一款语言中立、平台中立、开源的远程过程调用(RPC)系统。 -  Dubbo：Dubbo是一个分布式服务框架，以及SOA治理方案。其功能主要包括：高性能NIO通讯及多协议集成，服务动态寻址与路由，软负载均衡与容错，依赖分析与降级等。Dubbo是阿里巴巴内部的SOA服务化治理方案的核心框架，Dubbo自2011年开源后，已被许多非阿里系公司使用。 -  Spring Cloud：Spring Cloud由众多子项目组成，如Spring Cloud Config、Spring Cloud Netflix、Spring Cloud Consul 等，提供了搭建分布式系统及微服务常用的工具，如配置管理、服务发现、断路器、智能路由、微代理、控制总线、一次性token、全局锁、选主、分布式会话和集群状态等，满足了构建微服务所需的所有解决方案。Spring Cloud基于Spring Boot, 使得开发部署极其简单。 

## RPC原理

### RPC调用流程

要让网络通信细节对使用者透明，我们需要对通信细节进行封装，我们先看下一个RPC调用的流程涉及到哪些通信细节：

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/2e72bc12bc9a4dcfda7f11f9b4044351.png">

1.  服务消费方（client）调用以本地调用方式调用服务； 1.  client stub接收到调用后负责将方法、参数等组装成能够进行网络传输的消息体； 1.  client stub找到服务地址，并将消息发送到服务端； 1.  server stub收到消息后进行解码； 1.  server stub根据解码结果调用本地的服务； 1.  本地服务执行并将结果返回给server stub； 1.  server stub将返回结果打包成消息并发送至消费方； 1.  client stub接收到消息，并进行解码； 1.  服务消费方得到最终结果。 
    RPC的目标就是要2~8这些步骤都封装起来，让用户对这些细节透明。

下面是网上的另外一幅图，感觉一目了然：

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/0cbbf589ab3b391fa691f9914e7f0f14.png">

### 如何做到透明化远程服务调用

怎么封装通信细节才能让用户像以本地调用方式调用远程服务呢？对java来说就是使用代理！java代理有两种方式：1） jdk 动态代理；2）字节码生成。尽管字节码生成方式实现的代理更为强大和高效，但代码维护不易，大部分公司实现RPC框架时还是选择动态代理方式。

下面简单介绍下动态代理怎么实现我们的需求。我们需要实现RPCProxyClient代理类，代理类的invoke方法中封装了与远端服务通信的细节，消费方首先从RPCProxyClient获得服务提供方的接口，当执行helloWorldService.sayHello("test")方法时就会调用invoke方法。

```
public class RPCProxyClient implements java.lang.reflect.InvocationHandler{
    private Object obj;
    public RPCProxyClient(Object obj){
        this.obj=obj;
    }
    /**
     * 得到被代理对象;
     */
    public static Object getProxy(Object obj){
        return java.lang.reflect.Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(), new RPCProxyClient(obj));
    }
    /**
     * 调用此方法执行
     */
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        //结果参数;
        Object result = new Object();
        // ...执行通信相关逻辑
        // ...
        return result;
    }
}

```

```
public class Test {
     public static void main(String[] args) {         
     HelloWorldService helloWorldService = (HelloWorldService)RPCProxyClient.getProxy(HelloWorldService.class);
     helloWorldService.sayHello("test");
    }
}

```

其实就是通过动态代理模式，在执行该方法的前后对数据进行封装和解码等，让用于感觉就像是直接调用该方法一样，殊不知，我们对方法前后都经过了复杂的处理。

### 如何对消息进行编码和解码

确定消息数据结构

客户端的请求消息结构一般需要包括以下内容：

-  接口名称：在我们的例子里接口名是“HelloWorldService”，如果不传，服务端就不知道调用哪个接口了； -  方法名：一个接口内可能有很多方法，如果不传方法名服务端也就不知道调用哪个方法； -  参数类型&amp;参数值：参数类型有很多，比如有bool、int、long、double、string、map、list，甚至如struct等，以及相应的参数值； -  超时时间 + requestID（标识唯一请求id） 
   服务端返回的消息结构一般包括以下内容：
-  状态code + 返回值 -  requestID 
   序列化

一旦确定了消息的数据结构后，下一步就是要考虑序列化与反序列化了。

什么是序列化？序列化就是将数据结构或对象转换成二进制串的过程，也就是编码的过程。

什么是反序列化？将在序列化过程中所生成的二进制串转换成数据结构或者对象的过程。

为什么需要序列化？转换为二进制串后才好进行网络传输嘛！

为什么需要反序列化？将二进制转换为对象才好进行后续处理！

现如今序列化的方案越来越多，每种序列化方案都有优点和缺点，它们在设计之初有自己独特的应用场景，那到底选择哪种呢？从RPC的角度上看，主要看三点：

-  通用性：比如是否能支持Map等复杂的数据结构； -  性能：包括时间复杂度和空间复杂度，由于RPC框架将会被公司几乎所有服务使用，如果序列化上能节约一点时间，对整个公司的收益都将非常可观，同理如果序列化上能节约一点内存，网络带宽也能省下不少； -  可扩展性：对互联网公司而言，业务变化飞快，如果序列化协议具有良好的可扩展性，支持自动增加新的业务字段，而不影响老的服务，这将大大提供系统的灵活度。 
   目前互联网公司广泛使用Protobuf、Thrift、Avro等成熟的序列化解决方案来搭建RPC框架，这些都是久经考验的解决方案。

消息里为什么要有requestID？这个问题很简单，就不说明了，你能回答出来么？

### 如何发布自己的服务

这个我前面的很多文章都提到过，Java常用zookeeper，Go常用ETCD，服务端进行注册和心跳，客户端获取机器列表，没啥高深的，比如zookeeper：

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/0da5dda8ec051cc1607adbf0ac6cebfc.png">

## gRPC &amp; Thrift

## gRPC

### gRPC 简介

gRPC是一个高性能、通用的开源RPC框架，其由Google 2015年主要面向移动应用开发并基于HTTP/2协议标准而设计，基于ProtoBuf序列化协议开发，且支持众多开发语言。

由于是开源框架，通信的双方可以进行二次开发，所以客户端和服务器端之间的通信会更加专注于业务层面的内容，减少了对由gRPC框架实现的底层通信的关注。

如下图，DATA部分即业务层面内容，下面所有的信息都由gRPC进行封装。

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/4300aa8813cadf864471b8512535ea7d.png">



### gRPC 特点

-  语言中立，支持多种语言； -  基于 IDL 文件定义服务，通过 proto3 工具生成指定语言的数据结构、服务端接口以及客户端 Stub； -  通信协议基于标准的 HTTP/2 设计，支持双向流、消息头压缩、单 TCP 的多路复用、服务端推送等特性，这些特性使得 gRPC 在移动端设备上更加省电和节省网络流量； -  序列化支持 PB（Protocol Buffer）和 JSON，PB 是一种语言无关的高性能序列化框架，基于 HTTP/2 + PB, 保障了 RPC 调用的高性能。 

### gRPC 交互过程

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/a35f5b2257cfdc10c1cdb0449d594643.png">

-  交换机在开启gRPC功能后充当gRPC客户端的角色，采集服务器充当gRPC服务器角色； -  交换机会根据订阅的事件构建对应数据的格式（GPB/JSON），通过Protocol Buffers进行编写proto文件，交换机与服务器建立gRPC通道，通过gRPC协议向服务器发送请求消息； -  服务器收到请求消息后，服务器会通过Protocol Buffers解译proto文件，还原出最先定义好格式的数据结构，进行业务处理； -  数据处理完后，服务器需要使用Protocol Buffers重编译应答数据，通过gRPC协议向交换机发送应答消息； -  交换机收到应答消息后，结束本次的gRPC交互。 
   简单地说，gRPC就是在客户端和服务器端开启gRPC功能后建立连接，将设备上配置的订阅数据推送给服务器端。我们可以看到整个过程是需要用到Protocol Buffers将所需要处理数据的结构化数据在proto文件中进行定义。

### 什么是Protocol Buffers?

你可以理解ProtoBuf是一种更加灵活、高效的数据格式，与XML、JSON类似，在一些高性能且对响应速度有要求的数据传输场景非常适用。ProtoBuf在gRPC的框架中主要有三个作用：

-  定义数据结构 -  定义服务接口 -  通过序列化和反序列化，提升传输效率 
   为什么ProtoBuf会提高传输效率呢？

我们知道使用XML、JSON进行数据编译时，数据文本格式更容易阅读，但进行数据交换时，设备就需要耗费大量的CPU在I/O动作上，自然会影响整个传输速率。Protocol Buffers不像前者，它会将字符串进行序列化后再进行传输，即二进制数据。

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/dabf033c07b1f054c795d5500884c0b2.png">



可以看到其实两者内容相差不大，并且内容非常直观，但是Protocol Buffers编码的内容只是提供给操作者阅读的，实际上传输的并不会以这种文本形式，而是序列化后的二进制数据。字节数会比JSON、XML的字节数少很多，速率更快。

如何支撑跨平台，多语言呢？

Protocol Buffers自带一个编译器也是一个优势点。前面提到的proto文件就是通过编译器进行编译的，proto文件需要编译生成一个类似库文件，基于库文件才能真正开发数据应用。具体用什么编程语言编译生成这个库文件呢？由于现网中负责网络设备和服务器设备的运维人员往往不是同一组人，运维人员可能会习惯使用不同的编程语言进行运维开发，那么Protocol Buffers其中一个优势就能发挥出来——跨语言。

从上面的介绍，我们得出在编码方面Protocol Buffers对比JSON、XML的优点：

-  简单，体积小，数据描述文件大小只有1/10至1/3； -  传输和解析的速率快，相比XML等，解析速度提升20倍甚至更高； -  可编译性强。 

### 基于HTTP 2.0标准设计

除了Protocol Buffers之外，从交互图中和分层框架可以看到， gRPC还有另外一个优势——它是基于HTTP 2.0协议的。

由于gRPC基于HTTP 2.0标准设计，带来了更多强大功能，如多路复用、二进制帧、头部压缩、推送机制。这些功能给设备带来重大益处，如节省带宽、降低TCP连接次数、节省CPU使用等。gRPC既能够在客户端应用，也能够在服务器端应用，从而以透明的方式实现两端的通信和简化通信系统的构建。

HTTP 版本分为HTTP 1.X、 HTTP 2.0，其中HTTP 1.X是当前使用最广泛的HTTP协议，HTTP 2.0称为超文本传输协议第二代。HTTP 1.X定义了四种与服务器交互的方式，分别为：GET、POST、PUT、DELETE，这些在HTTP 2.0中均保留。HTTP 2.0的新特性：

-  双向流、多路复用 -  二进制帧 -  头部压缩 

## Thrift

### Thrift 简介

thrift是一种可伸缩的跨语言服务的RPC软件框架。它结合了功能强大的软件堆栈的代码生成引擎，以建设服务，高效、无缝地在多种语言间结合使用。2007年由facebook贡献到apache基金，是apache下的顶级项目，具备如下特点：

-  支持多语言：C、C++ 、C# 、D 、Delphi 、Erlang 、Go 、Haxe 、Haskell 、Java 、JavaScript、node.js 、OCaml 、Perl 、PHP 、Python 、Ruby 、SmallTalk -  消息定义文件支持注释，数据结构与传输表现的分离，支持多种消息格式 -  包含完整的客户端/服务端堆栈，可快速实现RPC，支持同步和异步通信 

### Thrift框架结构

Thrift是一套包含序列化功能和支持服务通信的RPC（远程服务调用）框架，也是一种微服务框架。其主要特点是可以跨语言使用，这也是这个框架最吸引人的地方。

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/f896bfedf2c4ae261cf9a31debe9faef.png">

图中code是用户实现的业务逻辑，接下来的 Service.Client和 write()/read()是thrift根据IDL生成的客户端和服务端的代码，对应于RPC中Client stub和Server stub。TProtocol 用来对数据进行序列化与反序列化，具体方法包括二进制，JSON 或者 Apache Thrift 定义的格式。TTransport 提供数据传输功能，使用 Apache Thrift 可以方便地定义一个服务并选择不同的传输协议。

### Thrift网络栈结构

thirft使用socket进行数据传输，数据以特定的格式发送，接收方进行解析。我们定义好thrift的IDL文件后，就可以使用thrift的编译器来生成双方语言的接口、model，在生成的model以及接口代码中会有解码编码的代码。thrift网络栈结构如下：

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/8c0050c0f1cc77a1fbf3a09d5ed8fdf8.png">

Transport层

代表Thrift的数据传输方式，Thrift定义了如下几种常用数据传输方式：

-  TSocket: 阻塞式socket； -  TFramedTransport: 以frame为单位进行传输，非阻塞式服务中使用； -  TFileTransport: 以文件形式进行传输。 
   TProtocol层

代表thrift客户端和服务端之间传输数据的协议，通俗来讲就是客户端和服务端之间传输数据的格式(例如json等)，thrift定义了如下几种常见的格式：

-  TBinaryProtocol: 二进制格式； -  TCompactProtocol: 压缩格式； -  TJSONProtocol: JSON格式； -  TSimpleJSONProtocol: 提供只写的JSON协议。 
   Server模型
-  TSimpleServer: 简单的单线程服务模型，常用于测试； -  TThreadPoolServer: 多线程服务模型，使用标准的阻塞式IO； -  TNonBlockingServer: 多线程服务模型，使用非阻塞式IO(需要使用TFramedTransport数据传输方式); -  THsHaServer: THsHa引入了线程池去处理，其模型读写任务放到线程池去处理，Half-sync/Half-async处理模式，Half-async是在处理IO事件上(accept/read/write io)，Half-sync用于handler对rpc的同步处理； 

## gRPC VS Thrift

### 功能比较

直接贴上网上的两幅截图：

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/fda738b757a4de1ff8b8db0c49f49dd7.png">

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/4378349cd1e121a0e259d5e5efb54d46.png">

### 性能比较

也是基于网上测试的结果，仅供参考：

-  整体上看，长连接性能优于短连接，性能差距在两倍以上； -  对比Go语言的两个RPC框架，Thrift性能明显优于gRPC，性能差距也在两倍以上； -  对比Thrift框架下的的两种语言，长连接下Go 与C++的RPC性能基本在同一个量级，在短连接下，Go性能大概是C++的二倍； -  对比Thrift&amp;C++下的TSimpleServer与TNonblockingServer，在单进程客户端长连接的场景下，TNonblockingServer因为存在线程管理开销，性能较TSimpleServer差一些；但在短连接时，主要开销在连接建立上，线程池管理开销可忽略； -  两套RPC框架，以及两大语言运行都非常稳定，5w次请求耗时约是1w次的5倍； 

### 如何选型

什么时候应该选择gRPC而不是Thrift：

-  需要良好的文档、示例 -  喜欢、习惯HTTP/2、ProtoBuf -  对网络传输带宽敏感 
   什么时候应该选择Thrift而不是gRPC：
-  需要在非常多的语言间进行数据交换 -  对CPU敏感 -  协议层、传输层有多种控制要求 -  需要稳定的版本 -  不需要良好的文档和示例 

## 小节

上面详细介绍gRPC和Thrift的特点和区别，小节如下：

-  GRPC主要就是搞了个ProtoBuf，然后采用HTTP协议，所以协议部分没有重复造轮子，重点就在ProtoBuf上。 -  Thrift的数据格式是用的现成的，没有单独搞一套，但是它在传输层和服务端全部是自己造轮子，所以可以对协议层、传输层有多种控制要求。 

## gRPC示例

除了理论，我们还需注重实践，gPRC的使用姿势看这篇文章 

## Dubbo &amp; Spring Cloud

## Dubbo

Dubbo 是一个分布式服务框架，致力于提供高性能和透明化的 RPC 远程服务调用方案，以及 SOA 服务治理方案。简单的说，Dubbo 就是个服务框架，说白了就是个远程服务调用的分布式框架。

Dubbo 总体架构：

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/309b995cd2d06792b7ad01b3ff7ccf62.png">

Dubbo特点：

-  远程通讯: 提供对多种基于长连接的 NIO 框架抽象封装（非阻塞 I/O 的通信方式，Mina/Netty/Grizzly），包括多种线程模型，序列化（Hessian2/ProtoBuf），以及“请求-响应”模式的信息交换方式。 -  集群容错: 提供基于接口方法的透明远程过程调用（RPC），包括多协议支持（自定义 RPC 协议），以及软负载均衡（Random/RoundRobin），失败容错（Failover/Failback），地址路由，动态配置等集群支持。 -  自动发现: 基于注册中心目录服务，使服务消费方能动态的查找服务提供方，使地址透明，使服务提供方可以平滑增加或减少机器。 

## Spring Cloud

Spring Cloud 基于 Spring Boot，为微服务体系开发中的架构问题，提供了一整套的解决方案——服务注册与发现，服务消费，服务保护与熔断，网关，分布式调用追踪，分布式配置管理等。

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/927f5cda37497d080cdb43659eccb36e.png">

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/0978444bec542d649335dd51bee72009.png">

## Dubbo vs Spring Cloud

<img alt="" src="https://img-blog.csdnimg.cn/img_convert/102f152c91b7a9acc8663ade44c8a596.png">

使用 Dubbo 构建的微服务架构就像组装电脑，各环节我们的选择自由度很高，但是最终结果很有可能因为一条内存质量不行就点不亮了，总是让人不怎么放心，但是如果你是一名高手，那这些都不是问题；而 Spring Cloud 就像品牌机，在 Spring Source 的整合下，做了大量的兼容性测试，保证了机器拥有更高的稳定性，但是如果要在使用非原装组件外的东西，就需要对其基础有足够的了解。

关于 Dubbo 和 Spring Cloud 的相关概念和对比，我个人比较倾向于 Spring Cloud，原因就是真正的微服务框架、提供整套的组件支持、使用简单方便、强大的社区支持等等，另外，因为考虑到 .NET/.NET Core 的兼容处理，RPC 并不能很好的实现跨语言（需要借助跨语言库，比如 gRPC、Thrift，但因为 Dubbo 本身就是“gRPC”，在 Dubbo 之上再包一层 gRPC，有点重复封装了），而 HTTP REST 本身就是支持跨语言实现，所以，Spring Cloud 这一点还是非常好的（Dubbox 也支持，但性能相比要差一些）。

但凡事无绝对，每件事物有好的地方也有不好的地方，总的来说，Dubbo 和 Spring Cloud 的主要不同体现在两个方面：服务调用方式不同和专注点不同（生态不同）。
