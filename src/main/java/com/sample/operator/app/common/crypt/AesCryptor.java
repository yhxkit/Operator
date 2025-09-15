package com.sample.operator.app.common.crypt;

import com.sample.operator.app.dto.crypt.AesDto;
import com.sample.operator.app.dto.crypt.spec.AesCryptSpec;
import com.sample.operator.app.common.exception.OperException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class AesCryptor implements BaseCryptor{

    private final AesCryptSpec aesCryptSpec;

    @Override
    public String encrypt(String plainText, String svc, String subType, Object... obj) {

        try
        {
            AesDto aes = null;

            if(obj == null || obj.length == 0)
            {
                aes = getAesDto(svc, subType);
            }
            if(obj.length == 2)
            {
                String iv = obj[0].toString();
                String key = obj[1].toString();

                aes = getAesDto(svc, subType, iv, key);
            }
            else
            {
                aes = getAesDto(plainText, svc, subType, obj);
            }

            return encrypt(plainText, aes);
        }
        catch (Exception e) {
            String errMsg = "AES 암호화 실패" + OperException.getStackTrace(e);
            System.out.println(errMsg);
            return errMsg;
        }
    }

    @Override
    public String decrypt(String cipherText, String svc, String subType, Object... obj) {
        try{
            AesDto aes = null;

            if(obj == null || obj.length == 0)
            {
                aes = getAesDto(svc, subType);
            }
            else if(obj.length == 2)
            {
                String iv = obj[0].toString();
                String key = obj[1].toString();

                aes = getAesDto(svc, subType, iv, key);
            }
            else
            {
                aes = getAesDto(svc, subType, obj);
            }

            return decrypt(cipherText, aes);
        }
        catch (Exception e)
        {
            String errMsg = "AES 복호화 실패 " + OperException.getStackTrace(e);
            System.out.println(errMsg);
            throw new OperException(errMsg);
        }
    }



    public String encrypt(String data, AesDto aesDto) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(aesDto.getAes256Key()), aesCryptSpec.getAlgorithm());
        Cipher cipher =Cipher.getInstance(aesCryptSpec.getCipherFormat());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(Base64.getDecoder().decode(aesDto.getAes256Iv())));
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return new String(Base64.getEncoder().encode(encrypted));
    }

    public String decrypt(String data, AesDto aesDto) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(aesDto.getAes256Key()), aesCryptSpec.getAlgorithm());
        Cipher cipher =Cipher.getInstance(aesCryptSpec.getCipherFormat());
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(Base64.getDecoder().decode(aesDto.getAes256Iv())));
        byte[] btArr = cipher.doFinal(Base64.getDecoder().decode(data));
        return new String(btArr);
    }


    private AesDto getAesDto(String svc, String subType, Object... obj)
    {
        // svc 와 subType 별로 다른 키 정보를 가져와야 할 경우에는 별도 로직 생성
        if(obj == null || obj.length == 0)
        {
            System.out.println("키 관리 서버 연동이 되면 가져오고 아니면 오류로 ");
            String iv = "3eNYMyjX4oKxTcpYmm/wxw==";
            String key = "8C2q0mJKO1ch75ZhwJdFKV/oU5IOZsme300894TGHKE=";
            return AesDto.builder().aes256Iv(iv).aes256Key(key).build();
        }

        List<Object> aesList = Arrays.asList(Objects.requireNonNull(obj));
        
        if(aesList.size() != 2)
        {
            System.out.println("aes 암복호화에 필요한 인자를 확인해주세요");
        }

        String iv = aesList.get(0).toString().trim();
        String key = aesList.get(1).toString().trim();

        return AesDto.builder().aes256Iv(iv).aes256Key(key).build();
    }
}
