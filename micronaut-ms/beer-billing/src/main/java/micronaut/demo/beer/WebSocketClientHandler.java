package micronaut.demo.beer;

import io.micronaut.tracing.annotation.ContinueSpan;
import io.micronaut.tracing.annotation.SpanTag;
import io.micronaut.websocket.WebSocketSession;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import micronaut.demo.beer.model.BeerItem;
import micronaut.demo.beer.model.Ticket;
import micronaut.demo.beer.service.BillService;
import micronaut.demo.beer.service.BootService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Map;
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private final WebSocketClientHandshaker handshaker;

    final BillService billService;
   final BootService bootService;

    private ChannelPromise handshakeFuture;
    private static final ChannelGroup channels = new DefaultChannelGroup(
            GlobalEventExecutor.INSTANCE);

    /*
    public WebSocketClientHandler(final WebSocketClientHandshaker handshaker , BillService billService) {
        this.handshaker = handshaker;
        this.billService=billService;
    }
    */

    @Inject
    public WebSocketClientHandler(final WebSocketClientHandshaker handshaker, BillService billService, BootService bootService) {
        this.handshaker = handshaker;
        this.billService=billService;
        this.bootService=bootService;
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
            ctx.channel().writeAndFlush(evt);
        } else {
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
       // System.out.println("WG "+msg);
        /*if (msg instanceof byte[]) {
            String msg1=new String((byte[])msg);
            System.out.println("WG1 "+msg1);

        }
        */
        //System.out.println("WG1 "+msg);
        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {

            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;

             //System.out.println("WebSocket Client received message: " + textFrame.text()+"\n\n\n\n\n\n\n\n\n\n");
            //ctx.channel().writeAndFlush();
            String content = textFrame.text();
            //System.out.println("WG2 "+content+getClass()+" "+content);
            //System.out.println("Websocket Content "+content);//+
            if (content.indexOf(':')>-1 && !content.contains("__PING__")) {
                String[] parts = content.split(":");
                String username=parts[0];
                String beerName=parts[1];
               // BeerItem.Size beerSize=BeerItem.Size.MEDIUM;
                if (parts.length>2) {
                    String beerSize=parts[2];



                /**
                 * This is the logic now from TicketController.java - being executed by
                 * each WebsocketClientHander on each running instance of the beer-billing application
                 *
                 *
                 */

              //  System.out.println("Billing "+username+" beerName "+beerName);//
                    Optional<Ticket> t = getTicketForUser(username);
                    BeerItem beer = new BeerItem(beerName,BeerItem.Size.valueOf(beerSize));// );
                    Ticket ticket = t.isPresent() ?  t.get() : new Ticket();
                    ticket.add(beer);
                    System.out.println("Billing "+username+" ticket "+ticket+ " size:"+beerSize);
                    billService.createBillForCostumer(username, ticket);
                }

            } else {
                /**
                 * When a ping is sent the other side responds back with the same message
                 * a ping in effect this end receives ping from a specific node and updates the
                 * map of available sockets with last time stamp
                 */

                if (content.startsWith("__PING__")) {
                    if (content.contains(">")) {
                        String[] parts = new String(content).split(">");
                        if (parts!=null && parts.length>=1) {
                            String hostPort=parts[1];
                            if (hostPort!=null) {
                                ///String hostPort=new String(content).substring(content.indexOf('|')+1,content.length());
                                //System.out.print("Updating connection ------------------------------------------------>"+hostPort);
                                bootService.updateConnection(hostPort);
                                //System.out.println("Got a ping back");
                                // ch.writeAndFlush(new PongWebSocketFrame(((PingWebSocketFrame) msg).content()));
                            }
                        }

                    }
                }
            }

        } else if (frame instanceof PingWebSocketFrame) {
            System.out.println("WebSocket Client received pi ng");

        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");

        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }

    }

    @ContinueSpan
    private Optional<Ticket> getTicketForUser(@SpanTag("getTicketForUser") String customerName) {
        return Optional.ofNullable(billService.getBillForCostumer(customerName));
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
