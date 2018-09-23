package micronaut.demo.beer;

import io.micronaut.tracing.annotation.ContinueSpan;
import io.micronaut.tracing.annotation.SpanTag;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private final WebSocketClientHandshaker handshaker;

    final BillService billService;
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
    public WebSocketClientHandler(final WebSocketClientHandshaker handshaker, BillService billService) {
        this.handshaker = handshaker;
        this.billService=billService;
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

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
             //System.out.println("WebSocket Client received message: " + textFrame.text()+"\n\n\n\n\n\n\n\n\n\n");
            //ctx.channel().writeAndFlush();
            String content = textFrame.text();
            //System.out.println("Websocket Content "+content);//+
            if (content.indexOf(':')>-1) {
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
