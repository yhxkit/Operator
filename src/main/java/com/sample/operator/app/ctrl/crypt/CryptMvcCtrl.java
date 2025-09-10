package com.sample.operator.app.ctrl.crypt;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CryptMvcCtrl {

    @GetMapping("/crypt/list")
    public String toList() {
        return "crypt/list";
    }


    @GetMapping("/crypt/aes")
    public String toAes() {
        return "crypt/aes/cryptor";
    }

    @GetMapping("/crypt/rsa")
    public String toRsa() {
        return "crypt/rsa/cryptor";
    }

    @GetMapping("/crypt/pgp")
    public String toPgp() {
        return "crypt/pgp/cryptor";
    }
}
