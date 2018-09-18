package micronaut.demo.beer;

import io.netty.bootstrap.Bootstrap;
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
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import micronaut.demo.beer.model.BeerItem;
import micronaut.demo.beer.model.Ticket;

import java.io.IOException;
import java.net.URI;

public class WebSocketClient {
    private final URI uri;
    private Channel ch;
    private static final EventLoopGroup group = new NioEventLoopGroup();
    WebSocketClientHandler handler;
    public WebSocketClient(final String uri) {
        this.uri = URI.create(uri);
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
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));
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

        // println "-- ${handler.handshakeFuture().awaitUninterruptibly().findResult {}}"
        /*
       BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (console.ready()) {
            String msg = console.readLine();
            if (msg == null) {
                break;
            } else if ("bye".equals(msg.toLowerCase())) {
                //ch.writeAndFlush(new CloseWebSocketFrame());
                //ch.closeFuture().sync();
                break;
            } else if ("ping".equals(msg.toLowerCase())) {
                //WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));
                //ch.writeAndFlush(frame);
            } else {
                WebSocketFrame frame = new TextWebSocketFrame(msg);
                println "-> ${frame.text()}"
                //ch.writeAndFlush(frame);
            }
        }
        */



    }

    public void close() throws InterruptedException {
        //System.out.println("WebSocket Client sending close");
        ch.writeAndFlush(new CloseWebSocketFrame());
        ch.closeFuture().sync();
        // group.shutdownGracefully();
    }

    public void eval(final String text, String name, String size) throws IOException {
        ch.writeAndFlush(new TextWebSocketFrame(text+":"+name+":"+size));
    }
}
