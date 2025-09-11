package com.sample.operator.app.dto.sslCert;

import java.util.regex.Pattern;

public class NamedPemInfo
{
    // 개행문자 표현식
    public static final String rnRegex = "\\r?\\n";

    //pem 형식으로 개행문자가 포함된 경우
    public static final String beginDelimiter = "-----BEGIN";
    public static final String endDelimiter = "-----END";

    //pem 형식으로 개행문자 없이 한줄로 붙어있을 경우
    public static final String regexForPem = "(?s)"+beginDelimiter+".*?"+endDelimiter+".*?-----";
    public static final Pattern pattern = Pattern.compile(regexForPem);
}
