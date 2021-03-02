package com.photon.photonchain.storage.entity;

import javax.persistence.*;

@Entity
@Table(name = "TotalTrans", indexes = {
        @Index(name = "idx_pubKey", columnList = "pubKey", unique = true)})
public class TotalTrans {
    @Id
    String pubKey;//pubkey + _ + tokenName
    long income; // 收入
    long expenditure; // 支出
    long fee; // 手续 (支出)
    long cacheTime;

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public long getIncome() {
        return income;
    }

    public void setIncome(long income) {
        this.income = income;
    }

    public long getExpenditure() {
        return expenditure;
    }

    public void setExpenditure(long expenditure) {
        this.expenditure = expenditure;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }
}
