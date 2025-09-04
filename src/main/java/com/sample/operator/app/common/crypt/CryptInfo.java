package com.sample.operator.app.common.crypt;

import lombok.Getter;

import java.util.HashMap;

@Getter
public enum CryptInfo
{
    AES_DB_CONF           ("AES", "OPER", "DB",                "DB 설정 암복호화용 AES"),
    AES_SOMETHING_PRIVATE ("AES", "OPER", "something private", "DB 설정 암복호화용 AES"),

    RSA_PARTNER_A         ("RSA", "TEST","PARTNER_A",          "OPER 서비스 A 파트너사 RSA"),
    RSA_PARTNER_B         ("RSA", "TEST","PARTNER_B",          "OPER 서비스 B 파트너사 RSA"),

    PGP_PARTNER_PAIR      ("PGP", "TEST","PGP",                "PGP 키링"),

    DEFAULT_INFO          ("AES", "OPER","DB",                 "디폴트 설정");

    String cryptType;
    String svc;
    String subType;
    String desc;

    CryptInfo(String cryptType, String svc, String subType, String desc)
    {
        this.cryptType = cryptType;
        this.svc = svc;
        this.subType = subType;
        this.desc = desc;
    }

    private static HashMap<String, CryptInfo> cryptTypeMap;
    private static HashMap<String, CryptInfo> svcMap;
    private static HashMap<String, CryptInfo> subTypeMap;



    public static CryptInfo getCryptInfo(String cryptType, String svc, String subType)
    {
        return getByMap(cryptType, svc, subType);
    }

    private static CryptInfo getByMap(String cryptType, String svc, String subType)
    {
        initMaps();

        CryptInfo byCryptType = cryptTypeMap.get(cryptType);
        CryptInfo bySvc = svcMap.get(svc);
        CryptInfo bySubType = subTypeMap.get(subType);

        if(byCryptType.equals(bySubType) && bySvc.equals(bySubType)) // 세개가 모두 같아야 함
        {
            return byCryptType;
        }
        else{
            return DEFAULT_INFO;
        }
    }

    private static void initMaps()
    {
        if( cryptTypeMap == null || cryptTypeMap.isEmpty() )
        {
            cryptTypeMap = new HashMap<>();

            for(CryptInfo ci : CryptInfo.values())
            {
                ci.cryptTypeMap.put(ci.cryptType, ci);
            }
        }


        if( svcMap == null || svcMap.isEmpty() )
        {
            svcMap = new HashMap<>();

            for(CryptInfo ci : CryptInfo.values())
            {
                ci.svcMap.put(ci.svc, ci);
            }
        }


        if( subTypeMap == null || subTypeMap.isEmpty() )
        {
            subTypeMap = new HashMap<>();

            for(CryptInfo ci : CryptInfo.values())
            {
                ci.subTypeMap.put(ci.subType, ci);
            }
        }


    }
}
