package groovysocket.websocket

import groovysocket.domain.User
import groovysocket.service.NonUserService
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.util.concurrent.GlobalEventExecutor
import users.service.UserService

import javax.inject.Inject


class WebSocketHandler  extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Inject
    final UserService userService

    private static final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {

        if (msg instanceof WebSocketFrame) {
            System.out.println("This is a WebSocket frame");
            System.out.println("Client Channel : " + ctx.channel());
            if (msg instanceof BinaryWebSocketFrame) {
                System.out.println("BinaryWebSocketFrame Received : ");
                System.out.println(((BinaryWebSocketFrame) msg).content());

            }  else if (msg instanceof TextWebSocketFrame) {

                final String text = ((TextWebSocketFrame) msg).text();
                System.out.println("Received text frame {}"+text);

                /**
                 *
                 * This is a custom method built into the demo
                 * which simply returns the user object as a map back to caller
                 * when lookup is triggered
                 */

                if (text.contains(':')) {
                    def actions = text.split(':')
                    String action = actions[0]
                    String  trigger = actions[1]
                    println "-- ${action} vs ${trigger} vs ${trigger.getClass()}"
                    //TODO Why does this not inject
                    NonUserService userService1 = new NonUserService()
                    switch(action) {
                        case 'lookup':
                            User user = userService1.lookup(trigger)
                            if (user) {
                                /*
                                allChannels.stream()?.each { c->
                                    System.out.println("Sending ${user.loadValues()} to ${c.id()}");
                                    if (c!=ctx.channel()) {
                                        System.out.println("Sending ${user.loadValues()} to ${c.id()}");
                                        c.writeAndFlush(user.loadValues() as String)
                                    }
                                }
                                */

                                ctx.channel().writeAndFlush(
                                        new TextWebSocketFrame(user.loadValues() as String))


                            }

                            break
                        default:
                            /*
                            allChannels.stream()?.each { c->
                                if (c!=ctx.channel()) {
                                    c.writeAndFlush(text)
                                }
                            }
                            */
                            ctx.channel().writeAndFlush(
                                    new TextWebSocketFrame("Message recieved : " + ((TextWebSocketFrame) msg).text()));
                            break
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

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Adding new channel {} to list of channels "+ctx.channel().remoteAddress());
        allChannels.add(ctx.channel());
    }
}