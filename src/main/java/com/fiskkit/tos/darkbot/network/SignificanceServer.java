package com.fiskkit.tos.darkbot.network;

import com.fiskkit.tos.darkbot.util.TimeTracker;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by Fabled on 9/9/2014.
 */
public class SignificanceServer {

    private int port;
    private TimeTracker timeTracker;
    public SignificanceServer(int port) {
        this.port = port;
    }

    public SignificanceServer(int port, TimeTracker timeTracker) {
        this.port = port;
        this.timeTracker = timeTracker;

    }



    public void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SignificanceServerInitializer(null, timeTracker));

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
