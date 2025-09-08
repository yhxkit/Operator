package com.sample.operator.app.svc.pgp;

import com.sample.operator.app.common.crypt.spec.PgpKeySpec;
import com.sample.operator.app.common.exception.OperException;
import com.sample.operator.app.dto.pgp.*;
import com.sample.operator.app.jpa.pgp.entity.PgpKeyRing;
import com.sample.operator.app.jpa.pgp.repository.PgpRepository;
import com.sample.operator.app.svc.pgp.biz.PgpOperationBiz;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PgpSvc {

    private final PgpKeySpec pgpKeySpec;
    private final PgpOperationBiz operBiz;
    private final PgpRepository pgpRepository;

    // db 내 모든 키링
    public List<PgpKeyRing> getAllKeyRing()
    {
        return pgpRepository.findAll();
    }


    // idx 로 db내 키링 삭제
    public boolean delKeyRing(int idx){
        pgpRepository.deleteById(idx);
        return true;
    }

    // DB 내 특정 키링 정보를 다운로드하기
    public byte[] getResourceByPgpIdx(int idx) {
        Optional<PgpKeyRing> optkeyRing = pgpRepository.findById(idx);

        if (optkeyRing.isPresent()) {
            PgpKeyRing foundkeyRing = optkeyRing.get();

            try {
                byte[] pub = foundkeyRing.getPgpPubKeyRing();
                byte[] pri = foundkeyRing.getPgpPrivKeyRing();

                return operBiz.makeZipFile(pub, pri);
            }
            catch (Exception e) {
                System.out.println("DB내 키링 추출 실패");
                throw new OperException("DB 내 키링 추출 실패");
            }
        }else {
            System.out.println("No KeyRing found");
            return null;
        }
    }

    // DB 내 특정 키링 정보 읽어오기
    public List<PgpPubKeyDto> getPgpKeyRingById(int idx){
        Optional<PgpKeyRing> optkeyRing = pgpRepository.findById(idx);

        if(optkeyRing.isPresent()){
            PgpKeyRing foundkeyRing = optkeyRing.get();

            try{
                byte[] pub = foundkeyRing.getPgpPubKeyRing();
                byte[] pri = foundkeyRing.getPgpPrivKeyRing();

                PGPPublicKeyRingCollection pubColl = new PGPPublicKeyRingCollection(pub, pgpKeySpec.getCalculator());
                PGPSecretKeyRingCollection secColl = new PGPSecretKeyRingCollection(pri, pgpKeySpec.getCalculator());

                //pub 전체 정보
                List<PgpPubKeyDto> pubInfoList = operBiz.showPubKeyRingInfo(pubColl);
                // sec 전체 정보
                List<PgpPrivKeyDto> privInfoList = operBiz.showPrivKeyRingInfo(secColl);
                return matchPubkeyAndSecKey(pubInfoList, privInfoList);
            }catch (Exception e){
                System.out.println("DB 내 키링 추출 실패 pgpIdx = "   + idx + ", e : " + e.getMessage());
                throw new OperException(e.getMessage());
            }
        }else
        {
            System.out.println("DB내 해당 키링이 없습니다 "+idx);
            return null;
        }
    }

    // db에 키링 저장
    public boolean saveKeyRing(PgpKeyRingUploadList dtoList, String uploaderId)
    {
        try{
            String comment = dtoList.getComment();
            PGPPublicKeyRingCollection pubCol = extractPubFromDto(dtoList);
            PGPSecretKeyRingCollection secCol = extractSecFromDto(dtoList);

            PgpKeyRing entity = PgpKeyRing.builder().pgpPubKeyRing(Objects.requireNonNull(pubCol).getEncoded()).pgpPrivKeyRing(Objects.requireNonNull(secCol).getEncoded()).uploaderId(uploaderId).build();
            pgpRepository.save(entity);
            return true;
        }
        catch (Exception e)
        {
            System.out.println("키링 저장 실패" + e.getMessage());
            return false;
        }
    }

    // 키링에서 키 삭제
    public byte[] removeKeyFromKeyRing(PgpKeyRingUploadList dtoList, String masterId)
    {
        long masteridL = Long.parseLong(masterId);;

        try {
            PGPPublicKeyRingCollection pubcol = extractPubFromDto(dtoList);
            PGPSecretKeyRingCollection seccol = extractSecFromDto(dtoList);

            return operBiz.removeKeyPair(pubcol,seccol, masteridL);
        }
        catch (Exception e) {
            System.out.println("삭제 실패" + e.getMessage());
            throw new OperException(e.getMessage());

        }
    }

    // 키링에 키 추가
    public byte[] addKeyToKeyRing( PgpKeyRingUploadList dtoList, boolean isPublic)
    {
        if(dtoList.getList().size() != 2)
        {
            System.out.println("키링은 기존/신규 2개가 필요합니다");
            throw new OperException("키링 다시 확인해주세요");
        }

        try{
            MultipartFile keyRing = dtoList.getList().get(0).getKeyRingFile();
            MultipartFile key = dtoList.getList().get(1).getKeyRingFile();

            if(isPublic){
                return operBiz.addPubKeyRing(keyRing, key);
            }
            else{
                return operBiz.addSecKeyRing(keyRing, key);
            }
        } catch (Exception e) {
            System.out.println("키링 추가 실패 " + e.getMessage());
            throw new OperException(e.getMessage());
        }
    }

    public byte[] createNewKeySet()
    {
        try
        {
            System.out.println("생성 시작");
            return operBiz.createPgpKeySet();
        }
        catch (Exception e)
        {
            System.out.println("생성 실패");
            throw new OperException("생성 실패");
        }
    }


    // 넘어온 공개키링과 비밀키링의 매치여부 확인
    public List<PgpPubKeyDto> showKeyRingSetInfo(PgpKeyRingUploadList list)
    {
        List<PgpPubKeyDto> pub;
        
        try
        {
            PGPPublicKeyRingCollection pubcol=null;
            PGPSecretKeyRingCollection seccol=null;
            
            if( list == null )
            {
                // 오류
            }
            else 
            {
                pubcol = extractPubFromDto(list);
                seccol = extractSecFromDto(list);
            }
            
            // 공개키 전체 정보
            pub = operBiz.showPubKeyRingInfo(pubcol);
            if(pub == null || pub.isEmpty())
            {
                throw new OperException("PGP 추출 불가");
            }
            
            // 개인키 전체 정보
            List<PgpPrivKeyDto> pri = operBiz.showPrivKeyRingInfo(seccol);
            
            pub = matchPubkeyAndSecKey(pub, pri);
        }
        catch (Exception e)
        {
            System.out.println(OperException.getStackTrace(e));
            throw new OperException("PGP 키가 없거나 구성 확인에 실패");
        }
        return pub;
    }

    // 화면 노출용 pub 키링 DTO에 비밀키 여부 (mykey 여부) 세팅
    private List<PgpPubKeyDto> matchPubkeyAndSecKey(List<PgpPubKeyDto> pub, List<PgpPrivKeyDto> priv)
    {
        pub.forEach( pb -> {
            // 공개키와 개인키의 id가 일치할 경우만 true
            Optional<PgpPrivKeyDto> matched = priv.stream().filter( pr->pr.getKeyId().equals(pb.getKeyId())).findFirst();
            pb.setHasPrivateKey(matched.isPresent());
        });
        return pub;
    }

    // dto 에서 공개키링 추출
    private PGPPublicKeyRingCollection extractPubFromDto(PgpKeyRingUploadList list) throws IOException, PGPException {
        Optional<PgpKeyRingUploadDto> pubDto = list.getList().stream().filter(dto -> dto.getSvcType().equalsIgnoreCase(NamedPgpInfo.PGP) && dto.getKeyRingType().equalsIgnoreCase(NamedPgpInfo.PUBLIC)).findFirst();
    
        if(pubDto.isPresent())
        {
            return new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(new ArmoredInputStream(pubDto.get().getKeyRingFile().getInputStream())), pgpKeySpec.getCalculator());
        }
        else 
        {
            return null;
        }
    }

    // dto에서 비밀키링 추출
    private PGPSecretKeyRingCollection extractSecFromDto(PgpKeyRingUploadList list) throws IOException, PGPException {
        Optional<PgpKeyRingUploadDto> secDto = list.getList().stream().filter(dto -> dto.getSvcType().equalsIgnoreCase(NamedPgpInfo.PGP) && dto.getKeyRingType().equalsIgnoreCase(NamedPgpInfo.PRIVATE)).findFirst();

        if(secDto.isPresent())
        {
            return new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new ArmoredInputStream(secDto.get().getKeyRingFile().getInputStream())), pgpKeySpec.getCalculator());
        }
        else
        {
            return null;
        }
    }
    
    public byte[] convertPgpAndBase64Str(PgpKeyRingUploadList pgplist)
    {
        // pub sec 페어로 받아야함
        if(pgplist.getList().size() != 2)
        {
            return null;
        }

        MultipartFile pubFile = null;
        MultipartFile secFile = null;

        for(PgpKeyRingUploadDto dto : pgplist.getList())
        {
            switch (dto.getKeyRingType())
            {
                case NamedPgpInfo.PUBLIC :
                    pubFile = dto.getKeyRingFile();
                    break;
                case NamedPgpInfo.PRIVATE:
                    secFile = dto.getKeyRingFile();
                    break;
                default:
                    break;
            }

            // 키쌍 모두 할당되면 서비스타입에 따라 파일 변환 biz 후 zip 으로 리턴
            if( pubFile != null && secFile != null)
            {
                return switch (dto.getSvcType())
                {
                    case NamedPgpInfo.PGP -> operBiz.convertPgpToBase64Str(pubFile, secFile);
                    case NamedPgpInfo.Base64str -> operBiz.convertBase64StrToPgp(pubFile, secFile);
                    default -> null;
                };
            }
        }
        // 끝까지 오면 NULL
        return null;
    }
}
