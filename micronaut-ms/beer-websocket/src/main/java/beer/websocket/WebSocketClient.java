package beer.websocket;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.websocket.WebSocketSession;
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

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;


public class WebSocketClient {
    private final URI uri;
    private Channel ch;

    private static final EventLoopGroup group = new NioEventLoopGroup();
    WebSocketClientHandler handler;
    EmbeddedServer embeddedServer;
    @Inject
    public WebSocketClient(final String uri ) {
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
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()),embeddedServer);
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

    /**
     * Used to distribute websocket sessions amongst all the other nodes
     * @param session
     * @throws IOException
     */
    public void sendSession(final WebSocketSession session) throws IOException {
        ch.writeAndFlush(session);
    }

    public void sendSessions(final ArrayList<WebSocketSession> sessions) throws IOException {
        ch.writeAndFlush(sessions);
    }

    public void eval(final String text) throws IOException {
        ch.writeAndFlush(new TextWebSocketFrame(text));
    }
}
