package com.sample.operator.app.common;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AfterAppListener implements ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        System.out.println("시작! ");
    }


//    public static void main(String[] args) {
//
//        try {
//            // 1. 키 생성
//            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
//            keyGen.init(256); // or 128
//            SecretKey key = keyGen.generateKey();
//
//            // 2. IV 생성
//            byte[] iv = new byte[16];
//            SecureRandom random = new SecureRandom();
//            random.nextBytes(iv);
//            IvParameterSpec ivSpec = new IvParameterSpec(iv);
//
//
//            System.out.println("키정보 : " + Base64.getEncoder().encodeToString(key.getEncoded()) );
//            System.out.println("키 바이트는? : " + (key.getEncoded().length) );
//            // WPq0T9LAG1x448VuLcgHtRB8T38cRPHZ8wQuPhuie4=
//
//            System.out.println("ivSpec : " + Base64.getEncoder().encodeToString(ivSpec.getIV()) );
//            System.out.println(" iv 바이트는? " + ivSpec.getIV().length);
//            // fhl114yKtuBd5N4lY53K5w==
//
//        } catch (Exception e) {
//            System.out.println("익셉션 발생 ");
//        }
//    }
}
