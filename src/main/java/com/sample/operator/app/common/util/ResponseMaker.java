package com.sample.operator.app.common.util;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class ResponseMaker {

    // 다운로드할 리소스 생성하기
    public ResponseEntity<ByteArrayResource> makeDownloadableResource( String downloadName, byte[] resource) {
        if (resource == null) {
            return ResponseEntity.notFound().build();
        } else {
            ByteArrayResource res = new ByteArrayResource(resource);
            HttpHeaders headers = new HttpHeaders();

            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(downloadName, StandardCharsets.UTF_8));
            headers.add(HttpHeaders.CONTENT_TYPE, "application/x-download");

            return ResponseEntity.ok().headers(headers).body(res);
        }
    }
}
