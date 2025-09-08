package com.sample.operator.app.svc.sslcert.biz;

import com.sample.operator.app.common.exception.OperException;
import com.sample.operator.app.dto.sslCert.SslCertUploadDto;
import com.sample.operator.app.svc.fileBiz.ServerFileSvc;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
public class SslOperationBiz {

    private final ServerFileSvc serverFileSvc;

    // List 내 모든 인증서 통합
    public byte[] mergeCerts(List<SslCertUploadDto> list)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        list.stream().forEach(dto -> {
            try{
                MultipartFile file =dto.getCertFile();
                byte[] btArr = file.getResource().getContentAsByteArray();
                baos.write(btArr);
                baos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            }
            catch(Exception e)
            {
                System.out.println("리소스 오류 " + e.getMessage());
            }
        });
        return baos.toByteArray();
    }


    // 3개 인증서 풀체인 검증
    // leaf- inter -root 순서
    public boolean checkFullChain (List<X509Certificate> list)
    {
        boolean rst = false;

        try
        {
            // 체인 검증을 위한 certPath
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            CertPath certPath = cf.generateCertPath(list);

            // 키스토어 설정 : 신뢰할 수 있는 루트 인증서 서정
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null); // 빈 키스토어 로드 > 루트 인증서만 사용할 것이므로

            //루트 인증서 추가
            ks.setCertificateEntry("root", list.get(list.size()-1)); // 제일 마지막 인증서가 루트 인증서

            // 인증서 검증을 위한 PKIX paremeter 설정
            PKIXParameters params = new PKIXParameters(ks);
            params.setRevocationEnabled(false); // 인증서 폐기목록 OCSP 사용하지 않도록 설정

            // 인증서 체인 검증
            CertPathValidator val = CertPathValidator.getInstance("PKIX");
            val.validate(certPath, params);

            rst = true;
        }
        catch (CertPathValidatorException e)
        {
            System.out.println("인증서 체인 검증 실패" + e.getMessage());
        }
        catch (Exception e)
        {
            System.out.println("기타 실패" + e.getMessage());
        }
        return rst;
    }

    public List<X509Certificate> splitCerts (MultipartFile mergedFile)
    {
        try{
            InputStream is =mergedFile.getInputStream();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            Collection<? extends Certificate> colCert = cf.generateCertificates(is);
            return colCert.stream().map( c -> (X509Certificate)c).toList();
        }
        catch (Exception e)
        {
            System.out.println("인증서 분리 실패 " + e.getMessage());
            throw new OperException("인증서 분리 실패 " + e.getMessage());
        }
    }

    // 서명용 csr & privateKey 생성
    // rsa 2048 기준 키페어 생성
//    dn 정보 설정 필요
//    CN = ?.sample.com
//    O = yhxkit
//    L = some-gu
//    ST = Some-city
//    C = KR
    public byte[] createCsrAndPrivateKe(String svc)
    {
        String fileName = svc;

        if( svc == null || svc.isBlank() || svc.equals("*"))
        {
            svc = "*";
            fileName = "default";
        }

        fileName = serverFileSvc.isValidFileName(fileName) ? fileName : serverFileSvc.sanitizeDownloadableFileName(fileName);



        String cn = ".sample.com";
        String o = "yhxkit";
        String l = "Some-Gu";
        String st = "Some-City";
        String c = "KR";

        try{
            // 1. key pair 생성 - rsa 2048
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // 2. dn 정보 주체 정보 설정
            String dnInfo = "CN=" + svc + cn + ", O=" + o + ", L=" + l + ", ST=" + st + ", C=" + c;
            X500Name subject = new X500Name(dnInfo);

            // 3. 셀프 서명 인증서 생성
            X509Certificate cert = createSelfSignedCert(publicKey, privateKey, subject);

            // 4. csr 생성
            PKCS10CertificationRequest csr = createCsr(publicKey, privateKey, subject);

            // 5. csr 형식 변환
            byte[] pkcs10csr = convertCsrToPemFormat(csr);

            // 6. 인증서 pem형식으로 변환
            byte[] pemCert = convertX509ToPemStr(cert);

            // 7. 키 pkcs1 로 형식 변환
            byte[] pkcs1key = convertPkcs8ToPkcs1(privateKey);

            // 8. zip 으로 묶어서 반환
            return makeZipFile(fileName, pkcs10csr, pemCert, pkcs1key);

        }
        catch (Exception e)
        {
            System.out.println(OperException.getStackTrace(e));
            throw new OperException(e.getMessage());
        }
    }

    // 개인키 비밀번호 해제
    public byte[] deletePasswordFromPrivateKey (MultipartFile file, String password)
    {

        try(Reader reader = new InputStreamReader(file.getInputStream());
            PEMParser pemParser = new PEMParser(reader))
        {
            Object obj = pemParser.readObject();
            PEMKeyPair pemKeyPair;
            
            if(obj instanceof PEMEncryptedKeyPair)
            {
                PEMDecryptorProvider decryptor = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
                pemKeyPair = ((PEMEncryptedKeyPair) obj).decryptKeyPair(decryptor);
            }
            else if (obj instanceof PEMKeyPair) 
            {
                pemKeyPair = (PEMKeyPair) obj;
            }
            else 
            {
                throw new OperException("파일이 개인키 형식이 아닙니다");    
            }
            
            //PKCS8 형식에서 PKCS1형식으로 변환하여 리턴
            PrivateKey pkcs8key = new JcaPEMKeyConverter().getKeyPair(pemKeyPair).getPrivate();
            return convertPkcs8ToPkcs1(pkcs8key);
        } catch (Exception e) {
            throw new OperException(e.getMessage());
        }
    }


    // csr 생성
    private PKCS10CertificationRequest createCsr(PublicKey publicKey, PrivateKey privateKey, X500Name subject)
    {
        try {
            //  csr 빌드
            JcaPKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(subject, publicKey);

            // 서명자 설정
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);

            // csr 설정
            return csrBuilder.build(signer);
        } catch (Exception e) {
            System.out.println("CSR 생성 실패" + OperException.getStackTrace(e));
            return null;
        }
    }


    // 셀프서명 인증서 생성하기
    private X509Certificate createSelfSignedCert(PublicKey publicKey, PrivateKey privateKey, X500Name subject)
    {
        try
        {
            // 인증서 유효 기간 설정
            Date notBefore = new Date(); // 시작 날짜: 현재
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(notBefore);
            calendar.add(Calendar.YEAR, 1); // 1년 유효
            Date notAfter = calendar.getTime();

            // 인증서 시리얼 넘버
            BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

            // X.509 인증서 생성자
            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    subject,        // 발행자 = 주체 (Self-signed니까 동일)
                    serial,
                    notBefore,
                    notAfter,
                    subject,        // 주체 (자기 자신에게 발급)
                    publicKey
            );

            // 서명자
            ContentSigner certSigner = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);

            // 인증서 빌드
            X509CertificateHolder certHolder = certBuilder.build(certSigner);
            return new JcaX509CertificateConverter()
                    .setProvider(new BouncyCastleProvider())
                    .getCertificate(certHolder);
        }
        catch (Exception e)
        {
            System.out.println("셀프 서명 인증서 생성 실패 " + OperException.getStackTrace(e));
            return null;
        }
    }


    // zip 으로 묶기
    private byte[] makeZipFile(String svc, byte[] pksc10csr, byte[]  selfSignedCert, byte[] pkcs1key)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(ZipOutputStream zos = new ZipOutputStream(baos)) // zos.close 먼저 닫아야 파일 완성
        {
            serverFileSvc.addFileToZip(zos, selfSignedCert, svc + ".pem");
            serverFileSvc.addFileToZip(zos, pksc10csr, svc+".csr");
            serverFileSvc.addFileToZip(zos, pkcs1key, svc+"_private.key");

            zos.finish();
        } catch (Exception e)
        {
            throw new OperException(e.getMessage());
        }
        return baos.toByteArray();
    }




    // 개인키 형식 변환
    // BEGIN PRIVATE KEY -> BEGIN RSA PRIVATE KEY
    private byte[] convertPkcs8ToPkcs1(PrivateKey pkcs8key) throws IOException
    {
        RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey) pkcs8key;
        RSAPrivateKey rsaPk = new RSAPrivateKey(rsaKey.getModulus(), rsaKey.getPublicExponent(), rsaKey.getPrivateExponent(), rsaKey.getPrimeP(), rsaKey.getPrimeQ(), rsaKey.getPrimeExponentP(), rsaKey.getPrimeExponentQ(), rsaKey.getCrtCoefficient());

        // pem
        StringWriter sw = new StringWriter();
        try (PemWriter writer = new PemWriter(sw))
        {
            writer.writeObject(new PemObject("RSA PRIAVTE KEY", rsaKey.getEncoded()));
        }

        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    //csr -> pem 변환 / PKCS10 표준
    private byte[] convertCsrToPemFormat(PKCS10CertificationRequest csr) throws IOException
    {
        String pkcs10Header = "-----BEGIN CERTIFICATE REQUEST-----";
        String pkcs10Footer = "-----END CERTIFICATE REQUEST-----";

        String lineSeparator = "\n";

        String pem =pkcs10Header + lineSeparator +
                Base64.getMimeEncoder(64, lineSeparator.getBytes(StandardCharsets.UTF_8)).encodeToString(csr.getEncoded()) +
                lineSeparator + pkcs10Footer ;

        return pem.getBytes(StandardCharsets.UTF_8);
    }


    // x509 인증서 pem 형식 문자열로 변환
    private byte[] convertX509ToPemStr(X509Certificate cert)
    {
        StringWriter writer = new StringWriter();

        try (JcaPEMWriter pemWriter = new JcaPEMWriter(writer))
        {
            pemWriter.writeObject(cert);
        }
        catch (Exception e)
        {
            System.out.println("x509 Pem 형식 변환 실패 " + OperException.getStackTrace(e));
        }
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    // pem 문자열 x509로 변환
    private X509Certificate convertStrToX509 (String str) throws CertificateException
    {
        byte[] decoded = str.getBytes(StandardCharsets.UTF_8);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is = new ByteArrayInputStream(decoded);
        return (X509Certificate)cf.generateCertificate(is);
    }


    //Multipart 라인별로 읽어들여 쪼개기
    private List<X509Certificate> multipartfileToX509ByStrLine(MultipartFile mergedFile)
    {
        List<X509Certificate> certList = new ArrayList<>();

        // 개행문자 표현식
        String rnRegex = "\\r?\\n";

        //pem 형식으로 개행문자가 포함된 경우
        String beginDelimiter = "-----BEGIN";
        String endDelimiter = "-----END";

        //pem 형식으로 개행문자 없이 한줄로 붙어있을 경우
        String regexForCert = "(?s)-----BAGING .*?-----END .*?-----";
        Pattern pattern = Pattern.compile(regexForCert);

        try
        {
            byte[] btArr = mergedFile.getResource().getContentAsByteArray();
            String strContent = new String (btArr);
            strContent = strContent.replaceAll(rnRegex, System.lineSeparator()); // 개행문자 일괄 치환
            String[] lines = strContent.split(System.lineSeparator());

            StringBuffer strCertContent = new StringBuffer();

            for(String line : lines)
            {
                // 개행없는 1라인인 경우
                if(pattern.matcher(line.trim()).matches())
                {
                    X509Certificate cert = convertStrToX509(strCertContent.toString());
                    certList.add(cert);
                    strCertContent = new StringBuffer();
                }
                else // 개행있는 복수 라인인 경우
                {
                    strCertContent.append(line + System.lineSeparator());
                    
                    // 인증서 종료
                    if(line.trim().startsWith(endDelimiter))
                    {
                        X509Certificate cert = convertStrToX509(strCertContent.toString());
                        certList.add(cert);
                        strCertContent = new StringBuffer();
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("인증서 추출 실패");
        }
        return certList;
    }
}
