package com.sample.operator.app.ctrl.pgp;

import com.sample.operator.app.svc.pgp.PgpSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PgpMvcCtrl
{
    private final PgpSvc pgpSvc;

    @GetMapping("/pgp/list")
    public String toList(Model model, @RequestParam(value = "pgpIdx", required = false, defaultValue = "0") int pgpIdx)
    {
        if( pgpIdx !=0 )
        {
            model.addAttribute("currentPgpIdx", pgpIdx);
            model.addAttribute("pgpKeyList", pgpSvc.getPgpKeyRingById(pgpIdx));
        }else{
            model.addAttribute("pgpKeyList", pgpSvc.showKeyRingSetInfo(null));
        }
        return "pgp/list";
    }

    // 구성 확인
    @GetMapping("/pgp/split")
    public String toSplit(){
        return "pgp/split";
    }

    // 키링 추가
    @GetMapping("/pgp/merge")
    public String toMerge(){
        return "pgp/merge";
    }

    // 키링 제거
    @GetMapping("/pgp/remove")
    public String toRemove(){
        return "pgp/remove";
    }

    // 키링 Base64 문자열 관련 변환
    @GetMapping("/pgp/convert")
    public String toConvert(){
        return "pgp/convert";
    }

    // DB 저장된 키링 리스트
    @GetMapping("/pgp/dbList")
    public String toDbList(Model model){
        model.addAttribute("dblist", pgpSvc.getAllKeyRing());
        return "pgp/db_list";
    }

    // 키링 DB 저장
    @GetMapping("/pgp/dbSave")
    public String toDBSave(){
        return "pgp/db_save";
    }

}
