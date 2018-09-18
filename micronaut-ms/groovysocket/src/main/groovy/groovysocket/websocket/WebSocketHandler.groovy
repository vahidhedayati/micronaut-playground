package groovysocket.websocket

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.handler.codec.http.websocketx.*
import io.netty.util.concurrent.GlobalEventExecutor

class WebSocketHandler  extends SimpleChannelInboundHandler<WebSocketFrame> {


    //private static final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {

        if (msg instanceof WebSocketFrame) {
            System.out.println("This is a WebSocket frame");
            System.out.println("Client Channel : " + ctx.channel());
            if (msg instanceof BinaryWebSocketFrame) {
                System.out.println("BinaryWebSocketFrame Received : ");
                System.out.println(((BinaryWebSocketFrame) msg).content());
            }  else if (msg instanceof TextWebSocketFrame) {
                //ctx.write(msg.retain());
                final String text = ((TextWebSocketFrame) msg).text();
                if (text) {
                    System.out.println("Received text frame : "+text);
                    //This resends to all connected billing apps
                    try {
                        HttpServerHandler.allChannels.stream()?.each { c->
                            c.writeAndFlush(new TextWebSocketFrame(text));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } else if (msg instanceof PingWebSocketFrame) {
                System.out.println("PingWebSocketFrame Received : ");
                System.out.println(((PingWebSocketFrame) msg).content());
            } else if (msg instanceof PongWebSocketFrame) {
                System.out.println("PongWebSocketFrame Received : ");
                System.out.println(((PongWebSocketFrame) msg).content());
            } else if (msg instanceof CloseWebSocketFrame) {
                System.out.println("Received close : ");
                ctx.close()
            } else if (msg instanceof CloseWebSocketFrame) {
                System.out.println("CloseWebSocketFrame Received : ");
                System.out.println("ReasonText :" + ((CloseWebSocketFrame) msg).reasonText());
                System.out.println("StatusCode : " + ((CloseWebSocketFrame) msg).statusCode());
            } else {
                System.out.println("Unsupported WebSocketFrame");
            }
        }

    }

    /*
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Adding new channel {} to list of channels nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"+ctx.channel().remoteAddress());
        allChannels.add(ctx.channel());
    }
    */
}