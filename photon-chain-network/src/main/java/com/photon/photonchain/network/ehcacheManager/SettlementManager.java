package com.photon.photonchain.network.ehcacheManager;

import com.alibaba.fastjson.JSON;
import com.photon.photonchain.network.core.MessageProcessor;
import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.entity.TotalTrans;
import com.photon.photonchain.storage.entity.Transaction;
import com.photon.photonchain.storage.repository.TotalTransRepository;
import com.photon.photonchain.storage.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SettlementManager {
    private static Logger logger = LoggerFactory.getLogger(SettlementManager.class);
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    TotalTransRepository totalTransRepository;

    @Autowired
    StatisticalAssetsManager statisticalAssetsManager;

    @Value("${active.time}")
    private long ACTIVE_TIME;

    public void toCalculation(Transaction t) {
        if (t == null || t.getTransValue() == 0 || t.getFee() == 0) return;
        //收入计算
        try {
            String tokenName = t.getTokenName();
            String transTo = t.getTransTo();
            String transFrom = t.getTransFrom();
            long transValue = t.getTransValue();
            long free = t.getFee();
            boolean initPit = false;
            // 手续费 gec
            if (!pubKeyExitsAndActive(transFrom, Constants.PTN)) {
                initTotalTrans(transFrom, Constants.PTN);
                initPit = true;
            } else {
                totalTransRepository.addFee(transFrom + "_" + Constants.PTN.toLowerCase(), free);
//                logger.info("增加手续费：free={}，pubkey={}", free, transFrom);
            }

            // 支出 other token
            if (!pubKeyExitsAndActive(transFrom, tokenName)) {
                initTotalTrans(transFrom, tokenName);
            } else if (!initPit || (initPit && !tokenName.equalsIgnoreCase(Constants.PTN))) {
                totalTransRepository.addExpenditure(transFrom + "_" + tokenName.toLowerCase(), transValue);
//                logger.info("增加支出：tValue={}，pubkey={}", transValue, transFrom);
            }
            // 收入 other token
            if (!pubKeyExitsAndActive(transTo, tokenName)) {
                initTotalTrans(transTo, tokenName);
            } else {
                totalTransRepository.addIncome(transTo + "_" + tokenName.toLowerCase(), transValue);
//                logger.info("增加收入：tValue={}，pubkey={}", transValue, transTo);
            }
            TotalTrans overFrom = getTotalTrans(transFrom, tokenName);
            TotalTrans overTo = getTotalTrans(transTo, tokenName);
//            logger.info("收入账户：{}", JSON.toJSONString(overTo));
//            logger.info("支出账户：{}", JSON.toJSONString(overFrom));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TotalTrans getTotalTrans(String pubKey, String tokenName) {
        return totalTransRepository.getTotalTransByPubKey(pubKey + "_" + tokenName.toLowerCase());
    }

    public TotalTrans getCalculation(String pubKey, String tokenName) {
        TotalTrans tt = totalTransRepository.getTotalTransByPubKey(pubKey + "_" + tokenName.toLowerCase());
        if (tt == null || System.currentTimeMillis() - tt.getCacheTime() > ACTIVE_TIME)
            return initTotalTrans(pubKey, tokenName);
        return tt;
    }

    private TotalTrans initTotalTrans(String pubKey, String tokenName) {
        try {
            Map<String, Map<String, Long>> accountAssets = statisticalAssetsManager.getStatisticalAssets();
            Long blockHeight = -1L;
            try {
                blockHeight = accountAssets.get(Constants.PTN).get(Constants.SYNC_BLOCK_HEIGHT);
            } catch (Exception e) {
            }
            // 总收
            long income = transactionRepository.findIncome(blockHeight, pubKey, tokenName);
            // 总支出
            long expenditure = transactionRepository.findExpenditureValue(blockHeight, pubKey, tokenName);
            // 总手续费
            long free = tokenName.equalsIgnoreCase(Constants.PTN) ? transactionRepository.findSumFee(blockHeight, pubKey) : 0;
            TotalTrans tt = new TotalTrans();
            tt.setPubKey(pubKey + "_" + tokenName.toLowerCase());
            tt.setExpenditure(expenditure);
            tt.setIncome(income);
            tt.setFee(free);
            tt.setCacheTime(System.currentTimeMillis());
            TotalTrans st = totalTransRepository.save(tt);
            return st;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 数据存在并且未过期
     */
    private boolean pubKeyExitsAndActive(String pubKey, String tokenName) {
        TotalTrans tt = totalTransRepository.getTotalTransByPubKey(pubKey + "_" + tokenName.toLowerCase());
        if (tt == null)
            return false;
        return System.currentTimeMillis() - tt.getCacheTime() < ACTIVE_TIME;
    }
}
