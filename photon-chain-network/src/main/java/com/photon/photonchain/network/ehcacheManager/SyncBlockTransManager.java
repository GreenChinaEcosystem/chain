package com.photon.photonchain.network.ehcacheManager;

import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 同步块交易管理
 */
@Component
public class SyncBlockTransManager {
    private static final String KEY = "syncBlocking";
    private static Logger logger = LoggerFactory.getLogger(SyncBlockTransManager.class);

    private Cache syncBlockTransCache = EhCacheManager.getCache("syncBlockTransCache");

    public void setBlockSync(boolean sync) {
        EhCacheManager.put(syncBlockTransCache, KEY, sync);
    }

    public Boolean isBlockSync() {
        if (EhCacheManager.existKey(syncBlockTransCache, KEY)) {
            return (Boolean) syncBlockTransCache.get(KEY).getObjectValue();
        }
        return false;
    }
}
