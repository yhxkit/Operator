package com.sample.operator.app.ctrl.pgp;

import com.sample.operator.app.common.util.ResponseMaker;
import com.sample.operator.app.dto.pgp.NamedPgpInfo;
import com.sample.operator.app.dto.pgp.PgpKeyRingUploadList;
import com.sample.operator.app.dto.pgp.PgpPubKeyDto;
import com.sample.operator.app.svc.pgp.PgpSvc;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PgpRestCtrl
{
    private final ResponseMaker responseMaker;
    private final PgpSvc pgpSvc;


    // DB에 키링 저장
    @PostMapping("/pgp/save")
    public boolean saveKeyRing (HttpServletRequest req, PgpKeyRingUploadList dtoList)
    {
        String uploaderId = req.getUserPrincipal().getName();
        return pgpSvc.saveKeyRing(dtoList, uploaderId);
    }


    // PGP 키링 정보 분석
    @PostMapping("/pgp/keyInfo")
    public ResponseEntity showKeyInfo( PgpKeyRingUploadList dtoList)
    {
        try{
            List<PgpPubKeyDto> list = pgpSvc.showKeyRingSetInfo(dtoList);
            return ResponseEntity.ok(list);
        }
        catch (Exception e)
        {
            System.out.println("키링 구성을 분석할 수 없습니다");
            return ResponseEntity.badRequest().build();
        }
    }

    // Db내 키링 삭제
    @PostMapping("/pgp/delete/{pgpIdx}")
    public ResponseEntity del(@PathVariable("pgpIdx") int pgpIdx)
    {
        try{
            pgpSvc.delKeyRing(pgpIdx);;
            return ResponseEntity.ok().build();
        }
        catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }

    //키링 다운로드
    @PostMapping("/pgp/download")
    public ResponseEntity<ByteArrayResource> downloadKeyRing (@RequestParam(value ="pgpIdx",required = false, defaultValue = "0") int pgpIdx)
    {
        // 디폴트값에 대해서 고민하기
//        if( pgpIdx != 0 )
//        {
            byte[] zip = pgpSvc.getResourceByPgpIdx(pgpIdx);
            return responseMaker.makeDownloadableResource("keyring.zip", zip);
//        }          
    }


    // 신규 키 생성
    @PostMapping("/pgp/create")
    public ResponseEntity<ByteArrayResource> newKeySet()
    {
        byte[] newKey = pgpSvc.createNewKeySet();
        return responseMaker.makeDownloadableResource("keyring.zip", newKey);
    }

    // 공개키링과 비밀키링에서 키 삭제
    @PostMapping("/pgp/removeKeyPair")
    public ResponseEntity<ByteArrayResource> removeKeyPair( PgpKeyRingUploadList dtoList, @RequestParam("master_id") String masterId)
    {
        byte[] keyring = pgpSvc.removeKeyFromKeyRing(dtoList, masterId);
        return responseMaker.makeDownloadableResource("removed.zip", keyring);
    }


    // 키링에 키 추가
    @PostMapping("/pgp/merge")
    public ResponseEntity<ByteArrayResource> addKey(PgpKeyRingUploadList dtoList, @RequestParam("merge_type") String keyType)
    {
        boolean isPub;
        String downloadName;

        if(keyType.equalsIgnoreCase(NamedPgpInfo.PUBLIC))
        {
            isPub = true;
            downloadName = "pubKeyRing";
        }
        else if(keyType.equalsIgnoreCase(NamedPgpInfo.PRIVATE))
        {
            isPub = false;
            downloadName = "privKeyRing";
        }
        else
        {
            System.out.println("머지 불가 타입");
            return ResponseEntity.badRequest().build();
        }
        byte[] keyRing = pgpSvc.addKeyToKeyRing(dtoList, isPub);
        return responseMaker.makeDownloadableResource(downloadName, keyRing);
    }


    // PGP Base64 간 변환
    @PostMapping("/pgp/convert")
    public ResponseEntity<ByteArrayResource> convertKey( PgpKeyRingUploadList dtoList)
    {
        String downloadName = "converted.zip";

        byte[] convertedData = pgpSvc.convertPgpAndBase64Str(dtoList);
        return responseMaker.makeDownloadableResource(downloadName, convertedData);
    }

}
