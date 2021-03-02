package com.photon.photonchain.network.peer2;

import com.photon.photonchain.network.excutor.PeerServerExcutor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class BlockPServer {
    private Logger logger = LoggerFactory.getLogger(BlockPServer.class);

    @Value("${peer.port2}")
    private Integer port;

    @Autowired
    private PeerServerExcutor peerServerExcutor;

    @Autowired
    private BlockPServerInitializer peerServerInitializer;

    @PostConstruct
    public void init() {

        port = port != 0 ? port : 2906;
        peerServerExcutor.execute(() -> {
            ServerBootstrap bootstrap = new ServerBootstrap();
            EventLoopGroup bossLoopGroup = new NioEventLoopGroup();
            EventLoopGroup childLoopGroup = new NioEventLoopGroup();
            bootstrap.group(bossLoopGroup, childLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(peerServerInitializer)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);
            try {
                ChannelFuture channelFuture = bootstrap.bind(port).sync();
                channelFuture.channel().closeFuture().await();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                childLoopGroup.shutdownGracefully();
                bossLoopGroup.shutdownGracefully();
            }
        });
    }
}
