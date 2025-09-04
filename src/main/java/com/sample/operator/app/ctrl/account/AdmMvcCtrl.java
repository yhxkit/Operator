package com.sample.operator.app.ctrl.account;

import com.sample.operator.app.jpa.account.entity.Account;
import com.sample.operator.app.svc.account.AccountSvc;
import com.sample.operator.app.svc.account.RoleSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AdmMvcCtrl {
    private final RoleSvc roleSvc;
    private final AccountSvc accountSvc;

    // Role 관리페이지
    @GetMapping("/adm/role")
    public String toRole(Model model)
    {
        model.addAttribute("roleList", roleSvc.getRoles());
        return "account/admin/role_list";
    }

    // 멤버 계정 목록 페이지
    @GetMapping("/adm/account")
    public String toAuth(Model model)
    {
        // pageable 필요
        model.addAttribute("accountList", accountSvc.getAllAccount());
        return "account/admin/account_list";
    }

    // 멤버 정보 상세 페이지
    @GetMapping("/adm/account/{accountName}")
    public String toModAccount(Model model, @PathVariable("accountName") String accountName)
    {
        Account account = accountSvc.getAccount(accountName);

        if(Optional.ofNullable(account).isEmpty())
        {
            model.addAttribute("errMsg", "정보를 찾을 수 없습니다");
            return "error/errorWithMsg";
        }

        model.addAttribute("accountInfo", account);
        model.addAttribute("authList", accountSvc.getAllAuthoritiesOfAccount(account));
        return "account/member/profile";
    }
}
