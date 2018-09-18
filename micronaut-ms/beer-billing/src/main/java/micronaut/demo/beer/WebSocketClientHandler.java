package micronaut.demo.beer;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    //private static final ChannelGroup channels = new DefaultChannelGroup();
    public WebSocketClientHandler(final WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        //System.out.println("WebSocket Client disconnected!");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent
                .HANDSHAKE_COMPLETE) {
            //SubscribeRequest request = new SubscribeRequest(factory, products);
            //TextWebSocketFrame frame = request.encodeFrame();
            //System.out.println("new block-- ${evt.toString()}"
            ctx.channel().writeAndFlush(evt);
        } else {
            //println "old block"
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.out.println("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            //ctx.write(frame.retain());
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            //JsonParser jsonParser = new JsonFactory().createJsonParser(textFrame.text());
            //while(jsonParser.nextToken()!= JsonToken.END_OBJECT) {
              //  String name = jsonParser.getCurrentName();
               // System.out.println("==="+name);

            //}


            System.out.println("WebSocket Client received message: " + textFrame.text()+"\n\n\n\n\n\n\n\n\n\n");
            //ctx.channel().writeAndFlush(msg)
            ctx.channel().writeAndFlush(textFrame.text());

            //ch.write(frame);
            //ch.flush()

            /*Channel incoming = ctx.channel();
            for (Channel channel : channels) {
                println "got ${channel}"
                if (channel != incoming){
                    println "writing to channel ${channel}"
                    channel.write("[" + incoming.remoteAddress() + "]" + textFrame.text() );
                }
            }
            channels.remove(ctx.channel());
            */

            //  ctx.write(textFrame.text());
            //ctx.channel().flush();
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }

    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();

        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }

        ctx.close();
    }
}
