package com.photon.photonchain.storage.utils;

import com.photon.photonchain.storage.constants.Constants;
import org.spongycastle.util.encoders.Hex;
import org.springframework.util.DigestUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class MD5Util {

    public static String getMD5String(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(64);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getRandomString(int length) {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            if ("char".equalsIgnoreCase(charOrNum)) {
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                int random1 = random.nextInt(26);
                val += (char) (random1 + temp);
            } else if ("num".equalsIgnoreCase(charOrNum)) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }

    public static String signMd5(byte[] bytes) {
        String signString = DigestUtils.md5DigestAsHex(bytes);
        if (signString.length() < Constants.TRANS_SIGN_LENGTH) {
            int num1 = Constants.TRANS_SIGN_LENGTH / signString.length();
            int num2 = Constants.TRANS_SIGN_LENGTH % signString.length();
            String signTemp = signString;
            if (num1 > 1) {
                for (int i = 1; i < num1; i++) {
                    signString += DigestUtils.md5DigestAsHex(Hex.decode(signTemp));
                }
            }
            if (num2 > 0) {
                signString+= DigestUtils.md5DigestAsHex(Hex.decode(signTemp)).substring(0,num2);
            }
        }
        return signString;
    }

//    public static final String TRANS_SIGNATURE = "7b2272223a32343539333230353535343838323538353638323837383333303938383933333130333236373538373439323534373634353434383134303236373032303732363133323537333138383037302c2273223a31363430393338313230353235373830323732363538373533353631323130373033323238343836333035343333393238323132343535303735313336373531323936353332393633323530312c2276223a32377d";
//    public static final Integer HASH_LENGTH = 66;
//
//    public static void main(String[] args) {
//        String str = signMd5(Hex.decode(TRANS_SIGNATURE));
//        System.out.println(str);
//        System.out.println(str.length());
//    }
}
