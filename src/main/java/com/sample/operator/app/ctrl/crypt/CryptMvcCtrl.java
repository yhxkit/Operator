package com.sample.operator.app.ctrl.crypt;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CryptMvcCtrl {

    @GetMapping("/crypt/aes/enc")
    public String toAesEnc() {
        return "crypt/aes/enc";
    }

    @GetMapping("/crypt/aes/dec")
    public String toAesDec() {
        return "crypt/aes/dec";
    }

    @GetMapping("/crypt/rsa/enc")
    public String toRsaEnc() {
        return "crypt/rsa/enc";
    }

    @GetMapping("/crypt/rsa/dec")
    public String toRsaDec() {
        return "crypt/rsa/dec";
    }

    @GetMapping("/crypt/pgp/enc")
    public String toPgpEnc() {
        return "crypt/pgp/enc";
    }

    @GetMapping("/crypt/pgp/dec")
    public String toPgpDec() {
        return "crypt/pgp/dec";
    }
}
