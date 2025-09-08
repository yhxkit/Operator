package com.sample.operator.config;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

// 프로젝트 내에서 바운시캐슬 사용 시 매번 해줄 필요없이 빈으로 등록하기
@Configuration
public class BouncyCastleConf {
    @PostConstruct
    public void addBCProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }
}
