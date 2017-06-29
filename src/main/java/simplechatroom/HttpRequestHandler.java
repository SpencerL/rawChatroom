package simplechatroom;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.io.RandomAccessFile;

/**
 * Created by lee on 6/28/17.
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest>{
    private final String wsUri;
    private static final File INDEX;

    static {
        URL location = HttpRequestHandler.class
                .getProtectionDomain()
                .getCodeSource().getLocation();
        /*
        try {

            //String path = location.toURI() + "index.html";  // TODO: 这里待修改，将index的位置另外设定
            String path = location.toURI()+"";  // .../wschatroom/target/somename.jar
            path = !path.contains("file:") ? path : path.substring(5);

            int idx = path.lastIndexOf('/');
            idx = idx - 6;
            path = path.substring(0, idx);  // .../wschatroom/
            path += "index.html";

            //String path = System.getProperty("user.dir") + "index.html";
            INDEX = new File(path);

        } catch (URISyntaxException e) {
            throw new IllegalStateException(
                    "Unable to locate index.html", e);
        }
        */

        String path = System.getProperty("user.dir")+"/index.html";
        INDEX =  new File(path);
    }

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
        FullHttpRequest request) throws Exception {
        // 如果请求了websocket,则协议升级，增加引用计数，并将request传递给下一个ChannelInboundHandler
        if (wsUri.equalsIgnoreCase(request.getUri())) {
            ctx.fireChannelRead(request.retain());
        } else {
            // 处理100 continue请求
            if (HttpHeaders.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }
            // 读取index.html
            RandomAccessFile file = new RandomAccessFile(INDEX, "r");
            HttpResponse response = new DefaultHttpResponse(
                    request.getProtocolVersion(), HttpResponseStatus.OK);
            response.headers().set(
                    "content-type",
                    "text/html; charset=UTF-8");    // 这里是"text/html",不是"text/plain"，否则不会渲染index.html
            boolean keepAlive = HttpHeaders.isKeepAlive(request);
            // 如果请求了keep-alive,则添加所需要的HTTP头信息,　TODO: 如何在客户端设置keepalive?
            if (keepAlive) {
                response.headers().set(
                        "content-length", file.length());
                response.headers().set("connection",
                        "keep-alive");
            }
            // 将HttpResponse写到客户端
            ctx.write(response);
            // 将index.html写到客户端
            if (ctx.pipeline().get(SslHandler.class) == null) {
                ctx.write(new DefaultFileRegion(
                            file.getChannel(), 0, file.length()));
            } else {
                ctx.write(new ChunkedNioFile(file.getChannel()));
            }
            // 写入LastHttpContent到客户端，并冲刷残留字节
            ChannelFuture future = ctx.writeAndFlush(
                    LastHttpContent.EMPTY_LAST_CONTENT);    //内容结束标志
            // 若没有请求keep-alive,则在写操作完成后关闭channel
            // TODO:客户端如何控制请求是否alive
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE); // 参数为http version以及http状态
        ctx.writeAndFlush(response);
    }


}
