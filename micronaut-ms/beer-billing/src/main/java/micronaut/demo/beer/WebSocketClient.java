package micronaut.demo.beer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import micronaut.demo.beer.service.BillService;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;


public class WebSocketClient {
    private final URI uri;
    private Channel ch;
    final BillService billService;

    private static final EventLoopGroup group = new NioEventLoopGroup();
    WebSocketClientHandler handler;

    @Inject
    public WebSocketClient(final String uri, BillService billService ) {
        this.uri = URI.create(uri);
        this.billService=billService;
    }

    public void open() throws Exception {
        //
        String protocol = uri.getScheme();
        if (!"ws".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        Bootstrap b = new Bootstrap();
        handler =
                new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()), billService);
        // uri, WebSocketVersion.V13, null, false, HttpHeaders.EMPTY_HEADERS, 1280000));

        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        //if (sslCtx != null) {
                        //  p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        //}
                        p.addLast(
                                new HttpClientCodec(),
                                new HttpObjectAggregator(8192),
                                WebSocketClientCompressionHandler.INSTANCE,
                                handler);
                    }

            /*@Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("http-codec", new HttpClientCodec());
                pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                pipeline.addLast("ws-handler", handler);
            }
            */
                });
        System.out.println("WebSocket Client connecting");
        ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        handler.handshakeFuture().sync();
    }

    public void close() throws InterruptedException {
        //System.out.println("WebSocket Client sending close");
        ch.writeAndFlush(new CloseWebSocketFrame());
        ch.closeFuture().sync();
        // group.shutdownGracefully();
    }

    public void eval(final String text) throws IOException {
        ch.writeAndFlush(new TextWebSocketFrame(text));
    }
    public void send(final String text) throws IOException {
        ch.writeAndFlush(text);
    }
    public void ping() throws IOException {
        ch.writeAndFlush(new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4})));
    }
}
