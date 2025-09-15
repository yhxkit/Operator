package com.sample.operator.app.ctrl.crypt;

import com.sample.operator.app.common.util.Telegram;
import com.sample.operator.app.dto.crypt.AesDto;
import com.sample.operator.app.dto.crypt.CryptUploadDto;
import com.sample.operator.app.svc.crypt.CryptorSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CryptRestCtrl {

    private final CryptorSvc cryptorSvc;

    // aes 256 : 기본 DB 설정 정보 암호화 / 카드 번호 DB 인서트 시 암복호화
    // RSA : 전문 요청 시 카드번호 암복호화
    // PGP : 요청 전문 및 응답 암복호화 / 서명 검증 n:n

    @PostMapping("/crypt/aes/getIvAndKey")
    public AesDto getAesDto()
    {
        return cryptorSvc.makeRandomAesDto();
    }

    @PostMapping("/crypt/aes/enc")
    public ResponseEntity<String> toAesEnc(CryptUploadDto dto)
    {
        String result =cryptorSvc.aesEnc(dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/crypt/aes/dec")
    public ResponseEntity<String> toAesDec(CryptUploadDto dto)
    {
        String result = cryptorSvc.aesDec(dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/crypt/rsa/enc")
    public ResponseEntity<String> toRsaEnc(CryptUploadDto dto)
    {
        String result =  cryptorSvc.rsaEnc(dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/crypt/rsa/dec")
    public ResponseEntity<String> toRsaDec(CryptUploadDto dto)
    {
        String result =  cryptorSvc.rsaDec(dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/crypt/pgp/enc")
    public ResponseEntity<String> toPgpEnc(CryptUploadDto dto)
    {
        String result =  cryptorSvc.pgpEnc(dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/crypt/pgp/dec")
    public ResponseEntity<String> toPgpDec(CryptUploadDto dto)
    {
        String result =  cryptorSvc.pgpDec(dto);
        return ResponseEntity.ok(result);
    }
}
