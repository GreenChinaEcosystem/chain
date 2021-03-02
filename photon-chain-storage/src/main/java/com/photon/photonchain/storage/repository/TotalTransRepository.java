package com.photon.photonchain.storage.repository;

import com.photon.photonchain.storage.entity.TotalTrans;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface TotalTransRepository extends CrudRepository<TotalTrans, Long> {

    @Modifying
    @Query(value = "update TotalTrans tt set tt.income = CASE WHEN tt.income IS NULL THEN :#{#income} ELSE (tt.income + :#{#income}) END where tt.pubKey=:#{#pubKey}")
    void addIncome(@Param("pubKey") String pubKey, @Param("income") long income);

    @Modifying
    @Query(value = "update TotalTrans tt set tt.expenditure = CASE WHEN tt.expenditure IS NULL THEN :#{#expenditure} ELSE (tt.expenditure + :#{#expenditure}) END where tt.pubKey=:#{#pubKey}")
    void addExpenditure(@Param("pubKey") String pubKey, @Param("expenditure") long expenditure);

    @Modifying
    @Query(value = "update TotalTrans tt set tt.fee = CASE WHEN tt.fee IS NULL THEN :#{#fee} ELSE (tt.fee + :#{#fee}) END where tt.pubKey=:#{#pubKey}")
    void addFee(@Param("pubKey") String pubKey, @Param("fee") long fee);

    @Query(value = "select tt from TotalTrans tt where tt.pubKey=:#{#pubKey}")
    TotalTrans getTotalTransByPubKey(@Param("pubKey") String pubKey);

    @Query(value = "select tt.cacheTime from TotalTrans tt where tt.pubKey=:#{#pubKey}")
    Long getCacheTime(@Param("pubKey") String pubKey);
}
