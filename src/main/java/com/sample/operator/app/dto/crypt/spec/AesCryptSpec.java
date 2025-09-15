package com.sample.operator.app.dto.crypt.spec;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AesCryptSpec {

    String algorithm = "AES";
    String cipherFormat = "AES/GCM/NoPadding";

    // "AES/ECB/PKCS5Padding"; < IV 사용 불가
    //CBC는 전통적인 블록 암호화, GCM은 최신의 빠르고 안전한 인증 암호용으로 활용
    // "AES/GCM/NoPadding"; <  IV 사용
    // "AES/CBC/PKCS5Padding" < IV 사용

}
