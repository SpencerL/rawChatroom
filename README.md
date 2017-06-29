## 基于Netty和WebSocket协议的简单聊天室
###  使用说明
#### 服务端
1. <code>mvn clean package</code>
2. <code>java -cp  dependencies/netty-all-4.1.11.Final.jar:target/simple-ws-chatroom-1.0-SNAPSHOT.jar  simplechatroom.ChatServer &lt;port&gt; </code>

#### 客户端
方式一：通过浏览器访问localhost:port，会出出现界面。
方式二：使用[Chrome websocket插件](https://chrome.google.com/webstore/detail/simple-websocket-client/pfdhoblngboilpfeibdedpjgfnlcodoo),
在URL栏输入ws://youraddress:yourport/ws
