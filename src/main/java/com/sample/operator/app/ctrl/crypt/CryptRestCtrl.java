package com.sample.operator.app.ctrl.crypt;

import com.sample.operator.app.dto.crypt.CryptUploadDto;
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
    public String toAesEnc(CryptUploadDto dto) {
        //String plainText, String svc, String subType, String aes256iv, String aes256key
        return cryptorSvc.aesEnc(dto);
    }

    @PostMapping("/crypt/aes/dec")
    public String toAesDec(CryptUploadDto dto) {
        return cryptorSvc.aesDec(dto);
    }

    @PostMapping("/crypt/rsa/enc")
    public String toRsaEnc(CryptUploadDto dto) {
        //String plainText, String svc, String subType, PrivateKey privateKey
        return cryptorSvc.rsaEnc(dto);
    }

    @PostMapping("/crypt/rsa/dec")
    public String toRsaDec(CryptUploadDto dto) {
        return cryptorSvc.rsaDec(dto);
    }

    @PostMapping("/crypt/pgp/enc")
    public String toPgpEnc(CryptUploadDto dto) {
        //String plainText, String svc, String subType, PGPPublicKeyRingCollection pub, PGPSecretKeyRingCollection sec
        return cryptorSvc.pgpEnc(dto);
    }

    @PostMapping("/crypt/pgp/dec")
    public String toPgpDec(CryptUploadDto dto) {
        return cryptorSvc.pgpDec(dto);
    }
}
