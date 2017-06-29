package simplechatroom;



import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;


/**
 * Created by lee on 6/28/17.
 */
public class TextWebSocketFrameHandler
    extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final ChannelGroup group;

    public TextWebSocketFrameHandler(ChannelGroup group) {
        this.group = group;
    }
    // 重写userEventTriggered()以处理自定义事件
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //如果该事件表示握手成功，则移除HttpRequestHandler,因为不会再受到http消息
        // TODO: Enum 类型比较判断可以用==
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            ctx.pipeline().remove(HttpRequestHandler.class);    // 移除所有HttpRequestHandler的实例
            // 通知所有已经连接的websocket客户端，有新的客户端上线
            group.writeAndFlush(new TextWebSocketFrame(
                        "Client " + ctx.channel() + "joined")); // TODO: 这里修改为用户id
            // 添加新的websocket channel 到ChannelGroup
            group.add(ctx.channel());
        } else {
            // 如果握手升级没成功，转发该事件到下一个ChannelHandler
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 增加消息的引用计数，并将其写到ChannelGroup中的所有客户端
        group.writeAndFlush(msg.retain());
    }

}
