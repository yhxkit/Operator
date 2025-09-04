package com.sample.operator.app.ctrl.account;


import com.sample.operator.app.jpa.account.entity.Account;
import com.sample.operator.app.svc.account.AccountSvc;
import com.sample.operator.app.svc.account.RoleSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AdmRestCtrl {

    private final RoleSvc roleSvc;
    private final AccountSvc accountSvc;

    // 권한 추가 생성
    @PostMapping("/adm/role/add")
    public boolean addRole(String roleName)
    {
        return roleSvc.addRole(roleName);
    }

    // 계정 정보 변경
    //ADMIN 권한을 가진 경우 계정 정보 및 권한 수정 가능
    @PostMapping("/adm/account/{accountName}/modify")
    public boolean modAccount(Account account, @PathVariable("accountName") String accountName)
    {
        if(account.getAccountName().equals(accountName))
        {
            return Optional.ofNullable(accountSvc.modAccount(account, true)).isPresent();
        }
        else
        {
            System.out.println("계정명 불일치" + accountName + account.getAccountName());
            return false;
        }
    }
    
    // 계정 권한 변경
    //ADMIN 권한을 가진 경우 계정 정보 및 권한 수정 가능
    @PostMapping("/adm/account/{accountName}/modifyRole")
    public boolean modAccountRole( @PathVariable("accountName") String accountName, @RequestBody List<String> list)
    {
        return accountSvc.modAccountAuth(accountName, list);
    }
}
