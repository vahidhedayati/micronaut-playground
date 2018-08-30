package groovysocket.init


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovysocket.websocket.HTTPInitializer
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import users.service.UserService

import javax.inject.Singleton

@Slf4j
@CompileStatic
@Singleton

class DataLoader implements ApplicationEventListener<ServerStartupEvent> {

    final UserService userService
    private static final int PORT = 9000;

    DataLoader(UserService userService) {
        this.userService = userService
    }

    @Override
    void onApplicationEvent(ServerStartupEvent event) {
        if (!userService.count()) {
            log.debug "Loading sample data"
            userService.save("jsmith","John","Smith", "password")
            userService.save("cjones","Casey","Jones", "password1")
            userService.save("mbutler","Mike","Butler", "password2")
            userService.save("jhanes","Jim","Hanes", "password3")
            userService.save("ksmith","Kevin","Smith", "password4")

            /**
             * This was actually put within users project - but had to be separated.
             *
             *
             * At this point I don't really understand enough about how you are actually supposed to initiate a websocket
             * channel on a micronaut app.
             *
             * It seems when this is enabled - the app name no longer communicates properly via consul - so hence
             * consul definition removed from this app application.yml as well.
             *
             *
             */

            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.option(ChannelOption.SO_BACKLOG, 1024);
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new HTTPInitializer());

                Channel ch = b.bind(PORT).sync().channel();

                ch.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }

        }

    }
}
