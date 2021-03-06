package com.photon.photonchain.network.peer2;

import com.photon.photonchain.network.proto.InesvMessage;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockPClientInitializer extends ChannelInitializer<NioSocketChannel> {

    @Autowired
    private BlockPClientHandler peerClientHandler;

    Logger logger = LoggerFactory.getLogger(BlockPClientInitializer.class);

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        logger.info("block 入口1：");
        ChannelPipeline pipeline = ch.pipeline();
        //将字节数组转换成Person对象和将Person对象转成字节数组,一共需要四个处理器
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new ProtobufDecoder(InesvMessage.Message.getDefaultInstance()));
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast(new ProtobufEncoder());
        //自定义处理器
        pipeline.addLast("handler", peerClientHandler);
    }
}
