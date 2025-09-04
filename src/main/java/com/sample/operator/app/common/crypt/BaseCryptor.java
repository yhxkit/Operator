package com.sample.operator.app.common.crypt;

public interface BaseCryptor {

    String encrypt(String plainText, String svc, String subType, Object ...obj);
    String decrypt(String cipherText, String svc, String subType, Object ...obj);
}
