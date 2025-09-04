package com.sample.operator.app.ctrl.account;


import com.sample.operator.app.jpa.account.entity.Account;
import com.sample.operator.app.svc.account.AccountSvc;
import com.sample.operator.app.svc.account.RoleSvc;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AccMvcCrtl {

    private final AccountSvc accountSvc;
    private final RoleSvc roleSvc;

    //대시보드
    @RequestMapping(value = "/", method = {RequestMethod.POST, RequestMethod.GET})
    public String toDashboard()
    {
        return "account/dashboard";
    }

    // 접근 거절
    @RequestMapping(value = "/denied", method = {RequestMethod.POST, RequestMethod.GET})
    public String toDeny()
    {
        return "error/denied";
    }

    // 세션 만료

    @RequestMapping(value = "/expired", method = {RequestMethod.POST, RequestMethod.GET})
    public String toExpired()
    {
        return "error/expired";
    }

    // 회원가입
    @GetMapping("/sign_up")
    public String toRegister()
    {
        return "account/sign_up";
    }

    // 로그인
    @GetMapping("/sign_in")
    public String toLogin(Model model, @RequestParam(value = "alertMsg", required = false) String alertMsg)
    {
        if (alertMsg != null)
        {
            model.addAttribute("alertMsg", alertMsg);
        }
        return "account/sign_in";
    }

    // 개인회원 본인 정보 페이지
    @GetMapping("/member/profile/{accountName}")
    public String toMyPage(Model model, @PathVariable("accountName") String accountName, HttpServletRequest request)
    {
        // 세션 내 정보 & 계정명 동일 여부 검증
        if(request.getUserPrincipal().getName().equals(accountName))
        {
            Account account = accountSvc.getAccount(accountName);
            if (Optional.ofNullable(account).isPresent())
            {
                model.addAttribute("accountInfo", account);
                model.addAttribute("authList", accountSvc.getAllAuthoritiesOfAccount(account));
                return "account/member/profile";
            }
            else
            {
                model.addAttribute("errMsg", "정보를 찾을 수 없습니다");
                return "error/errorWithMsg";
            }
        }
        else 
        {
            model.addAttribute("errMsg", "권한이 없습니다");
            return "error/errorWithMsg";
        }
    }
}
