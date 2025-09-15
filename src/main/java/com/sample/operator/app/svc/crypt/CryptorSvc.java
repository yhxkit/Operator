package com.sample.operator.app.svc.crypt;

import com.sample.operator.app.common.crypt.AesCryptor;
import com.sample.operator.app.common.crypt.PgpCryptor;
import com.sample.operator.app.common.crypt.RsaCryptor;
import com.sample.operator.app.dto.crypt.AesDto;
import com.sample.operator.app.dto.crypt.CryptUploadDto;
import com.sample.operator.app.svc.pgp.biz.PgpOperationBiz;
import com.sample.operator.app.svc.sslcert.biz.SslOperationBiz;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

@RequiredArgsConstructor
@Service
public class CryptorSvc
{

    private final SslOperationBiz certBiz;
    private final PgpOperationBiz pgpBiz;

    private final AesCryptor aesCryptor;
    private final PgpCryptor pgpCryptor;
    private final RsaCryptor rsaCryptor;

    // aes 256 : 기본 DB 설정 정보 암호화 / 카드 번호 DB 인서트 시 암복호화
    // RSA : 전문 요청 시 카드번호 암복호화
    // PGP : 요청 전문 및 응답 암복호화 / 서명 검증 n:n

    public AesDto makeRandomAesDto()
    {
        try
        {
            // 1. 키 생성
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // or 128
            SecretKey key = keyGen.generateKey();

            // 2. IV 생성
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);


            String ivVal = Base64.getEncoder().encodeToString(ivSpec.getIV());
            String keyVal = Base64.getEncoder().encodeToString(key.getEncoded());

            System.out.println("키정보 : " + ivVal );
            System.out.println("키 바이트는? : " + (key.getEncoded().length) );
            // WPq0T9LAG1x448VuLcgHtRB8T38cRPHZ8wQuPhuie4=

            System.out.println("ivSpec : " + keyVal );
            System.out.println(" iv 바이트는? " + ivSpec.getIV().length);
            // fhl114yKtuBd5N4lY53K5w==

            return AesDto.builder().aes256Iv( ivVal ).aes256Key( keyVal ).build();

        }
        catch (Exception e)
        {
            System.out.println("익셉션 발생 ");
            return null;
        }
    }

    public String aesEnc(CryptUploadDto dto)
    {
        String plainText = dto.getTargetData();
        String aes256iv = dto.getOptionData1();
        String aes256key = dto.getOptionData2();

        return aesCryptor.encrypt(plainText, null, null, aes256iv, aes256key);
    }


    public String aesDec(CryptUploadDto dto)
    {
        String cipherText = dto.getTargetData();
        String aes256iv = dto.getOptionData1();
        String aes256key = dto.getOptionData2();

        return aesCryptor.decrypt(cipherText, null, null, aes256iv, aes256key);
    }


    public String rsaEnc(CryptUploadDto dto)
    {
        String plainText = dto.getTargetData();
        PublicKey key = certBiz.multipartfileToPublicKeyByStrLine(dto.getFile1());
        return rsaCryptor.encrypt(plainText, null, null, key);
    }


    public String rsaDec(CryptUploadDto dto)
    {
        String cipherText = dto.getTargetData();
        PrivateKey key = certBiz.multipartfileToPrivateKeyByStrLine(dto.getFile1());
        return rsaCryptor.decrypt(cipherText, null, null, key);
    }


    public String pgpEnc(CryptUploadDto dto)
    {
        String plainText = dto.getTargetData();
        PGPPublicKeyRingCollection pub = pgpBiz.convertMultipartFileToPgpPub(dto.getFile1());
        PGPSecretKeyRingCollection sec = pgpBiz.convertMultipartFileToPgpSec(dto.getFile2());

        return pgpCryptor.encrypt(plainText, null, null, pub, sec);
    }


    public String pgpDec(CryptUploadDto dto)
    {
        String cipherText = dto.getTargetData();
        PGPPublicKeyRingCollection pub = pgpBiz.convertMultipartFileToPgpPub(dto.getFile1());
        PGPSecretKeyRingCollection sec = pgpBiz.convertMultipartFileToPgpSec(dto.getFile2());
        return pgpCryptor.decrypt(cipherText, null, null, pub, sec);
    }
}
