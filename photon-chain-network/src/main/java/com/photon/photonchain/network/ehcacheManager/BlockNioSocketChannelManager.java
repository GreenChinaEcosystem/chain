package com.photon.photonchain.network.ehcacheManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BlockNioSocketChannelManager {

    private static Logger logger = LoggerFactory.getLogger(BlockNioSocketChannelManager.class);

    private Cache nioSocketChannelCache = EhCacheManager.getCache("blockNioSocketChannelCache");

    public void addNioSocketChannel(String mac, ChannelHandlerContext ctx) {
        if (!EhCacheManager.existKey(nioSocketChannelCache, mac)) {
            EhCacheManager.put(nioSocketChannelCache, mac, ctx);
            logger.info("记录通道1：mac={} address={}", mac, ctx.channel().remoteAddress());
        } else {
            EhCacheManager.remove(nioSocketChannelCache, mac.toString());
            EhCacheManager.put(nioSocketChannelCache, mac, ctx);
            logger.info("记录通道1：mac={} address={}", mac, ctx.channel().remoteAddress());
        }
    }

    public void closeNioSocketChannelByMac(String mac) {
        if (EhCacheManager.existKey(nioSocketChannelCache, mac)) {
            ChannelHandlerContext channelHandlerContext = EhCacheManager.getCacheValue(nioSocketChannelCache, mac, ChannelHandlerContext.class);
            channelHandlerContext.channel().closeFuture();
            logger.info("移除通道：" + channelHandlerContext.channel().remoteAddress());
        }
    }

    public int getActiveNioSocketChannelCount() {
        removeInvalidChannel();
        List<ChannelHandlerContext> nioSocketChannelList = EhCacheManager.getAllCacheValue(nioSocketChannelCache, ChannelHandlerContext.class);
        return nioSocketChannelList.size();
    }

    public List<String> getChannelHostList() {
        List<String> hostList = new ArrayList<>();
        removeInvalidChannel();
        nioSocketChannelCache.getKeys().forEach(mac -> {
            hostList.add(mac.toString());
        });
        return hostList;
    }

    public void removeInvalidChannel() {
        try {
            nioSocketChannelCache.getKeys().forEach(mac -> {
                Channel ctx = ((ChannelHandlerContext) nioSocketChannelCache.get(mac).getObjectValue()).channel();
                boolean isActive = ctx.isActive();
                if (!isActive) {
                    EhCacheManager.remove(nioSocketChannelCache, mac.toString());
                    logger.info("移出通道1：mac={} address={}", mac, ctx.remoteAddress());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 移除指定的mac地址
     */
    public void removeTheMac(String mac) {
        EhCacheManager.remove(nioSocketChannelCache, mac);
        logger.info("移出通道1：mac={}", mac);
        this.closeNioSocketChannelByMac(mac);
    }

    public void write(Object o) {
        removeInvalidChannel();
        List<ChannelHandlerContext> nioSocketChannelList = EhCacheManager.getAllCacheValue(nioSocketChannelCache, ChannelHandlerContext.class);
        for (ChannelHandlerContext ctx : nioSocketChannelList) {
//            logger.info("①写出地址：" + ctx.channel().remoteAddress().toString());
            ctx.writeAndFlush(o);
        }
    }

    public void writeWithOutCtxList(Object o, List<String> hostList) {
        nioSocketChannelCache.getKeys().forEach(mac -> {
            try {
                ChannelHandlerContext ctx = (ChannelHandlerContext) nioSocketChannelCache.get(mac).getObjectValue();
                boolean isActive = ctx.channel().isActive();
                if (!isActive) {
                    EhCacheManager.remove(nioSocketChannelCache, mac.toString());
                } else {
                    if (!hostList.contains(mac)) {
                        logger.info("②block 回写地址：" + ctx.channel().remoteAddress().toString());
                        ctx.writeAndFlush(o);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("获取地址信息失败：{}", mac);
                logger.info("获取地址信息失败：{}", nioSocketChannelCache.getKeys().toArray().toString());
                logger.info("获取地址信息失败：{}", nioSocketChannelCache.get(mac));
            }
        });
    }
}