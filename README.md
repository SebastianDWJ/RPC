# RPC项目流程梳理



## 启动Zookeeper服务端



## 启动服务端

1. 通过Sping容器得到NettyRpcServer实例。

2. **自动注册服务**：在得到NettyRpcServer实例化之前，通过SpringBeanPostProcessor的postProcessBeforeInitialization。如果有@RpcService修饰的类会被发布服务到zookeeper上（即去创建永久节点），并将相关信息通过rpcConfig对象保存到ConcurrentHashMap中。

3. 启动netty服务端，对EventLoopGroup和ChannelPipeline等 进行初始化等操作（多线程Reactor），ChannelHandler中加入IdleStateHandler（心跳）、RpcMessage编码/解码器和NettyRpcServerHandler。

4. 绑定端口

   ```java
   // 绑定端口，同步等待绑定成功
   ChannelFuture f = b.bind(host, PORT).sync();
   // 等待服务端监听端口关闭
   f.channel().closeFuture().sync();
   ```

   

## 启动客户端

1. 与服务端类似，不过是单线程Reactor。

2. 通过在controller中的成员变量（即要远程调用的类）加上@RpcReference注解，使得在此成员变量实例化后，通过SpringBeanPostProcessor的postProcessAfterInitialization，赋予此成员变量代理后的对象，以屏蔽rpc的细节。也即在客户端无感知地调用远程的服务。（客户端stub）

3. 客户端调用需要的函数，生成对应的rpcRequest, 通过代理类invoke()去进行sendRpcRequest(rpcRequest)。

4. 进行客户端的sendRpcRequest() :

   1. 通过serviceDiscovery.lookupService(rpcRequest) 查找并选择使用的远程服务（其中包括负载均衡-一致性hash/随机），连接后得到用来通信的Channel。

   2. 把此次请求 id 与结果 作为k-v 放到unprocessedRequests的ConcurrentHashMap中

   3. 把rpcRequest包装成RpcMessage后刷入channel

      ``` java
      channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
          if (future.isSuccess()) {
              log.info("client send message: [{}]", rpcMessage);
          } else {
              future.channel().close();
              resultFuture.completeExceptionally(future.cause());
              log.error("Send failed:", future.cause());
          }
      });
      ```

   4. 通过RpcMessageEncoder进行编码（因为他是OutboundHandler），写到netty的ByteBuf中。即向ByteBuf中写入自定义结构的数据，并进行指定的序列化和压缩。

5. rpcResponse = completableFuture.get();等待服务端返回结果。



## 传输到服务端

1. 服务端收到信息后，将ByteBuf中的内容 通过RpcMessageDecoder进行解码（InboundHandler）。解码时，先检查魔数和版本，再进行解压缩和反序列化，最后还原成rpcMessage。

2. 然后进入NettyRpcServerHandler（InboundHandler）进行处理，然后通过Object result = rpcRequestHandler.handle (rpcRequest) 得到真正的函数输出结果，并以此来构建rpcMessage。

   > 调用handle()时，要通过服务发现 从map中得到服务（即 在postProcessBeforeInitialization中, 发布服务时，加入带有@RpcService 的bean对应的rpcServiceConfig），然后通过反射得到结果。

3. 将运行结果放入rpcResponse，再把rpcResponse放入rpcMessage。

4. ```java
   ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
   ```

5. 通过RpcMessageEncoder（OutBoundHandler）进行编码，按照自定义格式写入。



## 回传给客户端

1. 客户端收到信息后，通过RpcMessageDecoder进行解码（InboundHandler）。

2. 然后进入NettyRpcClientHandler（InboundHandler）的channelRead：如果此信息是rpcResponse，则将unprocessedRequests中的请求去除。

   ```java
   unprocessedRequests.complete(rpcResponse);
   ```

3. 最后返回到动态代理处返回结果。

   ```java
   return rpcResponse.getData();
   ```





> 1、ctx.**writeAndFlush**只会从当前的**handler**位置开始，往前找**outbound**执行
>
> **2**、ctx.pipeline().writeAndFlush与ctx.channel().writeAndFlush会从tail的位置开始，往前找**outbound**执行
