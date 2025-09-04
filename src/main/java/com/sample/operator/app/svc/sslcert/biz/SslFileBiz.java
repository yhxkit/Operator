package com.sample.operator.app.svc.sslcert.biz;

import com.sample.operator.app.common.exception.OperException;
import com.sample.operator.app.dto.sslCert.SslCertUploadDto;
import com.sample.operator.app.dto.sslCert.SslCertUploadList;
import com.sample.operator.app.jpa.sslCert.entity.SslCert;
import com.sample.operator.app.svc.fileBiz.ServerFileSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SslFileBiz
{

    @Value("${spring.servlet.multipart.location}") // 멀티파트 파일 서버 업로드 경로
    String path;

    private final ServerFileSvc serverFileSvc;

    // 서버에 요청파일 그대로 저장
    public List<SslCert> saveFileIndir(SslCertUploadList dtoList)
    {
        List<SslCert> rstCert = new ArrayList<>();
        File dir = new File(path);

        if(!dir.exists())
        {
            System.out.println("경로  확인 요망");
            return null;
        }

        try{
            for(SslCertUploadDto dto : dtoList.getList())
            {
                MultipartFile file =dto.getCertFile();

                if(file.isEmpty() || file.getOriginalFilename().isEmpty() || file.getOriginalFilename().isBlank())
                {
                    System.out.println("빈파일 이므로 패스");
                    continue;
                }

                String orgFileName =file.getOriginalFilename();
                System.out.println("업로드 파일 원본 명" + orgFileName);

                String fileNameOnle =orgFileName.substring(0, orgFileName.lastIndexOf("."));
                String fileExt = orgFileName.substring(orgFileName.lastIndexOf(".") + 1);
                
                String saveFilePath = path + File.separator + orgFileName;
                File saveFile = new File(saveFilePath); // 저장은 안되고 객체만 생성
                
                // 동일한 파일이 있으면 파일(1).확장자 형태로 생성
                if(saveFile.isFile())
                {
                    boolean isExistFile = true;
                    int idx = 0;

                    while(isExistFile)
                    {
                        idx++;
                        String newFileName =fileNameOnle +"("+ idx + ")." + fileExt;
                        String newFilePath = path + File.separator + newFileName;
                        isExistFile = new File(newFilePath).isFile();

                        if(!isExistFile)
                        {
                            saveFilePath = newFilePath;
                            saveFile = new File(newFilePath);
                        }
                    }
                }

                // 경로에 저장
                file.transferTo(saveFile);

                FileInputStream fis = new FileInputStream(saveFile);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate x509 = (X509Certificate) cf.generateCertificate(fis);

                SslCert cert = new SslCert(x509, dto.getCertType(), dto.getSvcName(), dto.getSubSvc());
                rstCert.add(cert);
            }
        }
        catch (Exception e)
        {
            System.out.println("저장 실패 " + e.getMessage());
        }
        return rstCert;
    }

    // 서버에 여러개의 인증서 union 하여저장
    // targetPath = 신규 생성 파일을 저장할 위치
    // mergerdFile = 신규 생성 파일명
    public void mergeCertsAnsSave(String targetpath, String mergedFile, String ... filePaths)
    {
        int cnt = filePaths.length;
        System.out.println("총 " + cnt + "개의 인증서를 머지합니다");

        String outFileName =targetpath + mergedFile;

        try( FileWriter fw = new FileWriter(outFileName);
             BufferedWriter bw = new BufferedWriter(fw))
        {
            for(String f : filePaths)
            {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);

                String line;
                while( (line = br.readLine()) != null)
                {
                    bw.write(line); // 라인 작성
                    bw.newLine(); // 줄바꿈
                    bw.flush();
                }
                
                fr.close();
                br.close();
            }
        }
        catch (Exception e)
        {
            System.out.println("머지 실패 " + e.getMessage());
            System.out.println(OperException.getStackTrace(e));
        }

        System.out.println("머지 종료");
    }

    // 서버에 union 인증서 개별 .cer로 분리하여 저장 
    public void splitCerts(String targetPath, String mergedFile)
    {
        String beginDelimiter = "-----BEGIN";;
        String endDelimiter = "-----END";;
        String ext = ".cer";

        System.out.println("분리 시작");
        HashMap<String, String> fileTempNameMap = new HashMap<>();
        
        int cnt = 0;
        String outFileName = cnt + ext;
        
        String oldFileName = null;
        String newFileName = null;
        
        try(   FileWriter fw = new FileWriter(targetPath + oldFileName);
               FileReader fr = new FileReader(mergedFile))
        {
            BufferedWriter bw = new BufferedWriter(fw);
            BufferedReader br = new BufferedReader(fr);
            
            String line;
            while( (line = br.readLine()) != null)
            {
                outFileName = cnt + ext;
                if(line.startsWith(beginDelimiter))
                {
                    System.out.println(cnt + "번째 인증서 시작");
                    bw.close(); // 여기서 닫지 않으면 프로세스 사용중 상태로 파일명 변경 불가

                    outFileName = cnt + ext;
                    bw = new BufferedWriter(new FileWriter(targetPath + outFileName));
                }
                bw.write(line); // 라인 작성
                bw.newLine(); // 줄바꿈
                bw.flush();
                
                if(line.startsWith(endDelimiter))
                {
                    X509Certificate splitedOne = extractCertInfo(targetPath + outFileName);
                    
                    if(splitedOne == null)
                    {
                        System.out.println("인증서 파일 추출 실패");
                    }
                    else {
                        Date expire = splitedOne.getNotAfter();
                        Date start = splitedOne.getNotBefore();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String period = sdf.format(start)  + " ~ " + sdf.format(expire);

                        String cnName = splitedOne.getSubjectX500Principal().getName();
                        newFileName = cnName.replace("\\.", "-")
                                + "(expire_"
                                + sdf.format(expire).split(" ")[0] + ")" + ext;

                        System.out.println("sub 로 인증서 추출 완료 -> cn " + cnName + " /  기간 : " + period + " / 파일 : " + outFileName + " , " + newFileName);
                    }

                    oldFileName = outFileName;
                    fileTempNameMap.put(oldFileName, newFileName);
                    cnt ++;
                }
            }
        } catch (Exception e)
        {
            System.out.println("인증서 분리 실패 " + OperException.getStackTrace(e));
        }

        serverFileSvc.renameFiles(targetPath, fileTempNameMap);
    }
    
    
    // 경로에 해당하는 x509 인증서 불러오기 
    private X509Certificate extractCertInfo(String path)
    {
        X509Certificate cert = null;
        
        try(FileInputStream fis = new FileInputStream(path))
        {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(fis);
        }
        catch (Exception e)
        {
            System.out.println(OperException.getStackTrace(e));
            System.out.println("인증서를 불러오지 못했습니다");
        }
        return cert;
    }
}
