package com.sample.operator.app.ctrl.sslCert;

import com.sample.operator.app.common.exception.OperException;
import com.sample.operator.app.common.util.ResponseMaker;
import com.sample.operator.app.dto.sslCert.PrivateKeyUploadDto;
import com.sample.operator.app.dto.sslCert.SslCertInfoModel;
import com.sample.operator.app.dto.sslCert.SslCertUploadList;
import com.sample.operator.app.jpa.sslCert.entity.SslCert;
import com.sample.operator.app.svc.sslcert.SslCertSvc;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class CertRestCtrl
{

    private final ResponseMaker responseMaker;
    private final SslCertSvc sslCertSvc;

    // DB 저장
    @RequestMapping(value = "/ssl/save", method = RequestMethod.POST)
    public ResponseEntity<String> ok(HttpServletRequest req, SslCertUploadList dtoList)
    {
        try
        {
            String uploaderId = req.getUserPrincipal().getName();
            return sslCertSvc.saveAllCert(dtoList, uploaderId) ? ResponseEntity.ok().body("성공") : ResponseEntity.badRequest().build();
        }
        catch (OperException e)
        {
            System.out.println("잘못된 인증서");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        catch (Exception e)
        {
            System.out.println(OperException.getStackTrace(e));
            return ResponseEntity.internalServerError().build();
        }
    }

    // 통합 인증서 쪼개서 정보 보여주기
    @RequestMapping(value = "/ssl/certInfo", method = RequestMethod.POST)
    public ResponseEntity<?> splitCerts( SslCertUploadList dtoList)
    {
        List<SslCertInfoModel> list = sslCertSvc.splitAllCerts(dtoList);
        
        if (list.isEmpty())
        {
            System.out.println("잘못된 인증서 파일");
            return ResponseEntity.badRequest().build();
        }
        else
        {
            return ResponseEntity.ok(list);
        }
    }

    // 인증서 머지하기
    @RequestMapping(value = "/ssl/unionSslCert", method = RequestMethod.POST)
    public ResponseEntity<ByteArrayResource> unionCert(SslCertUploadList dtoList, @RequestParam("merge_type") String mergeType)
    {
        if( dtoList.getList().isEmpty())
        {
            System.out.println("머지할 파일이 없습니다");
            return ResponseEntity.badRequest().build();
        }

        try
        {
            byte[] union =sslCertSvc.mergeCertsByMergeType(dtoList, mergeType);
            return responseMaker.makeDownloadableResource("unionSslCert.cer", union);
        }
        catch (OperException e)
        {
            return ResponseEntity.badRequest().build();
        }
        catch (Exception e)
        {
            System.out.println(OperException.getStackTrace(e));
            return ResponseEntity.internalServerError().build();
        }
    }


    // 개별 인증서 파일 다운
    @RequestMapping(value = "/ssl/download/{certIdx}", method = RequestMethod.POST)
    public ResponseEntity<ByteArrayResource> downloadCert(@PathVariable("certIdx") int certIdx)
    {
        Optional<SslCert> c =sslCertSvc.getCertById(certIdx);

        if(c.isPresent())
        {
            SslCert cc = c.get();
            X509Certificate x509 = cc.getCert();

            try {
              byte[] rst = x509.getEncoded();
              String fileName =cc.getSvcGroup().getSvcGroupName() + "_" + cc.getSvcGroup().getSubSvcName() + cc.getSvcGroup().getCertPurpose() + ".cer";
                return responseMaker.makeDownloadableResource(fileName, rst);
            }
            catch (Exception e)
            {
                System.out.println("인증서 다운로드 실패" + OperException.getStackTrace(e));
                return ResponseEntity.internalServerError().build();
            }
        }
        else
        {
            System.out.println("다운로드 하려는 인증서의 정보가 없습니다");
            return ResponseEntity.notFound().build();
        }
    }

    // db내 개별 인증서 파이 삭제
    @RequestMapping(value = "/ssl/delete/{certIdx}", method = RequestMethod.POST)
    public boolean delcert(@PathVariable("certIdx") int certIdx)
    {
        try
        {
            sslCertSvc.deleteCertById(certIdx);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }


    // 개인키 비밀번호 해제 : openssl rsa -in 암호화개인키.key -out newkey.pem
    // 개인키 파일 읽어 암호화 해제하고 pkcs1형식으로 내려받기
    @RequestMapping(value = "/ssl/deletePwFromPri", method = RequestMethod.POST)
    public ResponseEntity<ByteArrayResource> delPwFromPri(PrivateKeyUploadDto dto)
    {
        try{
            byte[] newKey = sslCertSvc.deletePasswordFromPrivateKey(dto);
            String fileName = "newkey.pem";
            return responseMaker.makeDownloadableResource(fileName, newKey);
        }
        catch (Exception e) {
            System.out.println("개인키 비밀번호 해제 실패 " + OperException.getStackTrace(e));
            return ResponseEntity.internalServerError().build();
        }
    }

    // 신규 csr & 개인키 쌍 생성
    // rsa 2048 기준으로 키페어 생성
    // dn 정보 설정 필요  = CN / O / C
    // 서비스 설정하지 않을 경우 * 인증서
    @RequestMapping(value = "/ssl/create", method = RequestMethod.POST)
    public ResponseEntity<ByteArrayResource> createCsrAndPri(@RequestParam(value="svcName", required = false) String svcName)
    {
        try{
            byte[] pairZip = sslCertSvc.createCsrAndPrivatekey(svcName);
            String fileName = svcName == null ? "default-ssl.zip" : svcName + "-ssl.zip";
            return responseMaker.makeDownloadableResource(fileName, pairZip);
        }
        catch (Exception e) {
            System.out.println(svcName + " 서비스의 csr 및 개인키 생성 실패 " + OperException.getStackTrace(e));
            return ResponseEntity.internalServerError().build();
        }
    }
}
