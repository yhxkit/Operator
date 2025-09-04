package com.sample.operator.app.svc.sslcert;

import com.sample.operator.app.common.exception.OperException;
import com.sample.operator.app.dto.sslCert.*;
import com.sample.operator.app.jpa.sslCert.entity.CertSvcGroup;
import com.sample.operator.app.jpa.sslCert.entity.SslCert;
import com.sample.operator.app.jpa.sslCert.repository.SslCertRepository;
import com.sample.operator.app.jpa.sslCert.repository.SslCertSvcGroupRepository;
import com.sample.operator.app.svc.sslcert.biz.SslFileBiz;
import com.sample.operator.app.svc.sslcert.biz.SslOperationBiz;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SslCertSvc
{

    private final SslFileBiz sslFileBiz;
    private final SslOperationBiz sslOperBiz;
    private final SslCertRepository certRepository;
    private final SslCertSvcGroupRepository svcGroupRepository;


    // 머지 유형에 따른 인증서 머지
    public byte[] mergeCertsByMergeType(SslCertUploadList dtoList, String mergeType)
    {
        if(mergeType.equalsIgnoreCase(NamedSslCertInfo.simple))
        {
            return sslOperBiz.mergeCerts(dtoList.getList());
        }
        else if(mergeType.equalsIgnoreCase(NamedSslCertInfo.fullChain))
        {
            return mergeFullChainCerts(dtoList);
        }
        else {
            System.out.println("없는 머지 유형");
            throw new OperException("없는 머지 유형");
        }
    }

    // 인증서 풀체인으로 머지. 풀체인 검증 처리 포함
    private byte[] mergeFullChainCerts(SslCertUploadList dtoList)
    {
        List<SslCertUploadDto> list = dtoList.getList();
        byte[] fullChain = new byte[0];
        
        // 인증서끼리 풀체인 검증
        if(list.size() == 3)
        {
            System.out.println("인증서 3개 아님...");
            throw new OperException("인증서 3개 아님...");
        }

        List<X509Certificate> totalCerts = new ArrayList<>();

        list.stream().map(dto -> {
            MultipartFile file = dto.getCertFile();
            return  sslOperBiz.splitCerts(file);
        }).forEach(totalCerts::addAll);

        // 업로드된 파일이 개별 파일인지 확인
        if( list.size() != totalCerts.size() )
        {
            System.out.println("통합 파일은 업로드 할 수 없습니다");
            throw new OperException("통합 파일 업로드 불가");
        }

        // 풀체인 검증
        if(sslOperBiz.checkFullChain(totalCerts))
        {
            // 검증 성공 시 머지
            fullChain = sslOperBiz.mergeCerts(list);
        }

        return fullChain;
    }

    // 인증서 파일 쪼개서 구성 확인
    public List<SslCertInfoModel> splitAllCerts(SslCertUploadList dtoList)
    {
        List<SslCertUploadDto> list = dtoList.getList();
        List<SslCertInfoModel> allCert = new ArrayList<>();

        list.stream().map( dto -> {
            List<SslCertInfoModel> converted;

            try
            {
                MultipartFile c = dto.getCertFile();
                String purpose = dto.getCertType();
                String svcName = dto.getSvcName();
                String subSvc = dto.getSubSvc();

                // 리스트 내에 통합 인증서가 포함되어있을 경우 전부 분리
                List<X509Certificate> splited = sslOperBiz.splitCerts(c);

                //분리한 인증서를 정보 확인을 위한 DTO 객체로 변환
                converted = splited.stream().map( s -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String expire = sdf.format(s.getNotAfter());

                    String cn = Arrays.stream(s.getSubjectX500Principal().getName().split(","))
                            .filter(str -> str.startsWith("CN=")).map(d -> d.substring(3)).findFirst()
                            .orElseGet(() -> s.getSubjectX500Principal().getName());

                    String issuer = Arrays.stream(s.getIssuerX500Principal().getName().split(","))
                            .filter(str->str.startsWith("CN=")).map(d -> d.substring(3)).findFirst()
                            .orElseGet(() -> s.getIssuerX500Principal().getName());

                    return SslCertInfoModel.builder().certType(purpose).svcGroupName(svcName).subSvc(subSvc).cn(cn)
                            .issuer(issuer).serialNumber(s.getSerialNumber()).expire(expire).build();
                }).toList();
            }
            catch (Exception e)
            {
                System.out.println("리소스 확인 중 문제 발생 " + OperException.getStackTrace(e));
                return null;
            }
            System.out.println("요청 파일 갯수 : " + list.size() + " / 분리된 인증서 총 갯수 : " + converted.size() );
            return converted;
        }).forEach(allCert::addAll);

        if (allCert.contains(null))
        {
            System.out.println("리소스 확인 중 문제 발생 ");
            throw new OperException("리소스 문제 발생");
        }
        return allCert;
    }


    // 모든 인증서 가져오기
    public List<SslCertInfoModel> getAll() {
        //certIdx를 기준으로 desc 정렬
        Sort sort = Sort.by(Sort.Order.desc("certIdx"));
        List<SslCert> list = certRepository.findAll(sort);
        return list.stream().map(SslCert::convertToSslCertModel).toList();
    }
    
    
    // 인덱스에 해당하는 인증서 가져오기 
    public Optional<SslCert> getCertById(int id) {
        return certRepository.findById(id);
    }
    
    //list 내 인증서 전체 저장
    //list 내부에 통합 파일이 있다면 오류. 개별 인증서만 처리
    @Transactional
    public boolean saveAllCert(SslCertUploadList dtoList, String uploaderId)
    {
        List<SslCertUploadDto> reqList = dtoList.getList();
        List<SslCertInfoModel> splitedList = splitAllCerts(dtoList);

        if(reqList.size() == splitedList.size())
        {
            boolean result = reqList.stream().allMatch( req -> {
                try{
                    InputStream is = req.getCertFile().getInputStream();
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate file = (X509Certificate) cf.generateCertificate(is);

                    CertSvcGroup svcGroup = CertSvcGroup.builder().svcGroupName(req.getSvcName()).subSvcName(req.getSubSvc()).certPurpose(req.getCertType()).build();
                    SslCert cert = SslCert.builder().svcGroup(svcGroup).cert(file).uploaderId(uploaderId).build();

                    return saveCert(cert);
                }catch (Exception e)
                {
                    System.out.println("변환 오류");
                    return false;
                }
            });
            
            // 저장에 실패한 인증서가 있을 경우 트랜잭션 처리 
            if (result) 
            {
                return result;
            }
            else 
            {
                System.out.println("저장에 실패가 있어 트랜잭션 처리됩니다");
                throw new OperException("저장에 실패가 있어 트랜잭션 처리됩니다");
            }
        } else if (reqList.size() > splitedList.size())
        {
            // 리스트 내 통합 인증서 파일 존재
            System.out.println("개별 SSL 인증서가 아닌 파일은 저장 불가 ");
            throw new OperException("개별 SSL 인증서가 아닌 파일은 저장 불가 ");
        }else if( reqList.size() < splitedList.size() )
        {
            // list 내 인증서 파일이 아닌 파일 존재 
            System.out.println("SSL 인증서 파일만 올리세요");
            throw new OperException("SSL 인증서 파일만 올리세요");
        }
        return true;
    }


    // 개별 인증서 저장
    public boolean saveCert(SslCert cert) {
        CertSvcGroup csg = cert.getSvcGroup();
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("svcGroupIdx");

        Example<CertSvcGroup> example = Example.of(csg, matcher);
        CertSvcGroup group =svcGroupRepository.findOne(example).orElseGet(()-> svcGroupRepository.save(csg));
        cert.setSvcGroup(group);

        SslCert rst = certRepository.save(cert);
        return Optional.ofNullable(rst).isPresent();
    }

    //id  값으로 인증서 삭제
    public void deleteCertById(int idx)
    {
        certRepository.deleteById(idx);
    }
    
    // 서버내 파일을 저장
    public List<SslCert> saveFileInServer(SslCertUploadList dtoList)
    {
        return sslFileBiz.saveFileIndir(dtoList);
    }


    // 개인키 비밀번호해제
    public byte[] deletePasswordFromPrivateKey(PrivateKeyUploadDto dto)
    {
        return sslOperBiz.deletePasswordFromPrivateKey(dto.getKeyFile(), dto.getKeyPass());
    }

    // 서명용 CSR 과 개인키 생성
    public byte[] createCsrAndPrivatekey (String svc)
    {
        return sslOperBiz.createCsrAndPrivateKe(svc);
    }
}
