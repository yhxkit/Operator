package com.sample.operator.app.ctrl.sslCert;

import com.sample.operator.app.svc.sslcert.SslCertSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequiredArgsConstructor
public class CertMvcCtrl
{

    private final SslCertSvc sslCertSvc;

    // DB 업로드 페이지
    @RequestMapping(value = "/ssl/upload", method = RequestMethod.GET)
    public String toMain()
    {
        return "sslCert/upload";
    }


    // 인증서 리스트 확인
    @RequestMapping(value = "/ssl/list", method = RequestMethod.GET)
    public String toList(Model model)
    {
        model.addAttribute("certList", sslCertSvc.getAll());
        return "sslCert/list";
    }


    // 인증서 머지
    @RequestMapping(value = "/ssl/merge", method = RequestMethod.GET)
    public String toMerge()
    {
        return "sslCert/merge";
    }


    // 인증서 구성 확인
    @RequestMapping(value = "/ssl/split", method = RequestMethod.GET)
    public String toSplit()
    {
        return "sslCert/split";
    }


    // privateKey 비번 해제
    @RequestMapping(value = "/ssl/delpass", method = RequestMethod.GET)
    public String toDelPw()
    {
        return "sslCert/delpass";
    }

    
    // csr / 개인키 생성
    @RequestMapping(value = "/ssl/create", method = RequestMethod.GET)
    public String toCreate()
    {
        return "sslCert/create";
    }

}
