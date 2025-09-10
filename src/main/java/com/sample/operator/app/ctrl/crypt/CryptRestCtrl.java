package com.sample.operator.app.ctrl.crypt;

import com.sample.operator.app.svc.crypt.CryptorSvc;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PrivateKey;

@RestController
@RequiredArgsConstructor
public class CryptRestCtrl {

    private final CryptorSvc cryptorSvc;

    // aes 256 : 기본 DB 설정 정보 암호화 / 카드 번호 DB 인서트 시 암복호화
    // RSA : 전문 요청 시 카드번호 암복호화
    // PGP : 요청 전문 및 응답 암복호화 / 서명 검증 n:n

    @PostMapping("/crypt/aes/enc")
    public String toAesEnc(String plainText, String svc, String subType, String aes256iv, String aes256key) {
        return cryptorSvc.aesEnc(plainText, svc, subType, aes256iv, aes256key);
    }

    @PostMapping("/crypt/aes/dec")
    public String toAesDec(String cipherText, String svc, String subType, String aes256iv, String aes256key) {
        return cryptorSvc.aesDec(cipherText, svc, subType, aes256iv, aes256key);
    }

    @PostMapping("/crypt/rsa/enc")
    public String toRsaEnc(String plainText, String svc, String subType, PrivateKey privateKey) {
        return cryptorSvc.rsaEnc(plainText, svc, subType, privateKey);
    }

    @PostMapping("/crypt/rsa/dec")
    public String toRsaDec(String cipherText, String svc, String subType, PrivateKey privateKey) {
        return cryptorSvc.rsaDec(cipherText, svc, subType, privateKey);
    }

    @PostMapping("/crypt/pgp/enc")
    public String toPgpEnc(String plainText, String svc, String subType, PGPPublicKeyRingCollection pub, PGPSecretKeyRingCollection sec) {
        return cryptorSvc.pgpEnc(plainText, svc, subType, pub, sec);
    }

    @PostMapping("/crypt/pgp/dec")
    public String toPgpDec(String cipherText, String svc, String subType, PGPPublicKeyRingCollection pub, PGPSecretKeyRingCollection sec) {
        return cryptorSvc.pgpDec(cipherText, svc, subType, pub, sec);
    }
}
