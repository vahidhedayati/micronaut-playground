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
                System.out.println("Received text frame : "+text);

                /**
                 *
                 * This is a custom method built into the demo
                 * which simply returns the user object as a map back to caller
                 * when lookup is triggered
                 */

                if (text.contains(':')) {
                    def actions = text.split(':')
                    String action = actions[0]
                    String  name = actions[1]
                    String  size = actions[2]

                  //              ctx.channel().writeAndFlush(
                    //                    new TextWebSocketFrame(text))


                            if (text) {
                                println "SENDING >${text}<"
                                ctx.channel().writeAndFlush(
                                        new TextWebSocketFrame(text));
                                /*allChannels.stream()
                                        .filter(c -> c != ctx.channel())
                                        .forEach(c -> c.writeAndFlush(frame));
                                        */
                                //HttpServerHandler.allChannels?.each { c->
                                 //   println "999999999999 working on ${c}"
                                  //  c.writeAndFlush(textFrame.text());
                                //}
                                HttpServerHandler.allChannels.stream()?.each { c->
                                    println "working on ${c}"
                                    c.writeAndFlush(new TextWebSocketFrame(text));
                                }
                               /* Channel incoming = ctx.channel();
                                for (Channel channel : allChannels.stream()) {
                                    System.out.println("got "+channel);
                                    if (channel != incoming){
                                        ///println "writing to channel ${channel}"
                                        channel.writeAndFlush(textFrame.text());
                                    }
                                }
                                */
                            }


                }




                //Write to own channel

                //System.out.println(((TextWebSocketFrame) msg).text());
                /*
                Java way -- above groovy way
                allChannels.stream()
                        .filter(c -> c != ctx.channel())
                        .forEach(c -> c.writeAndFlush(frame));
                        */

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