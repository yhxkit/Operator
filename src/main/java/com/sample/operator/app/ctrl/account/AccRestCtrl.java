package com.sample.operator.app.ctrl.account;

import com.sample.operator.app.jpa.account.entity.Account;
import com.sample.operator.app.svc.account.AccountSvc;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AccRestCtrl {
    
    private final AccountSvc accountSvc;

    // 임시 관리자 생성
    @PostMapping("/temp/createAdm")
    public boolean createAdm(@Valid @RequestBody Account account) {
        System.out.println("임시 관리자 계정을 생성합니다. adm / adm");
        return Optional.ofNullable(accountSvc.makeAdmin(account)).isPresent();
    }

    // 중복 확인 후 계정 생성
    @PostMapping("/sign_up")
    public boolean registerAccount(@Valid Account account)
    {
        System.out.println("계정 생성");
        return Optional.ofNullable(accountSvc.signUp(account)).isPresent();
    }

    // 계정 정보 변경. 권한 및 상태는 변경불가
    // 변경 가능 정보 = 이름, 사번, 패스워드
    @PostMapping("/member/profile/{accountName}/modify")
    public boolean modifyInfo (HttpServletRequest request, @PathVariable("accountName") String accountName, Account account)
    {
        // 세션 정보와 계정명 일치 여부 
        String principalName = request.getUserPrincipal().getName();
                
        if (principalName.equals(accountName) && accountName.equals(account.getAccountName())) {
            accountSvc.modAccount(account, false);
            return true;
        }
        else
        {
            System.out.println("계정명 불일치" + accountName + account.getAccountName() + principalName);
            return false;
        }
    }
}
