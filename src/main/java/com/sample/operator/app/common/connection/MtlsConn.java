package com.sample.operator.app.common.connection;


import com.sample.operator.app.common.exception.OperException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

@Component
public class MtlsConn
{

    @Value("${connection.oper.mtls.key_store.path}")
    String keyStorePath;

    @Value("${connection.oper.mtls.key_store.pw}")
    String keyStorePw;

    @Value("${connection.oper.mtls.trust_store.path}")
    String trustStorePath;

    @Value("${connection.oper.mtls.key_store.pw}")
    String trustStorePw;

    public String mtlsConn(String ip, String port, int timeout, String data)
    {

        try {
            TrustManagerFactory tmf = loadTrustManager();
            KeyManagerFactory kmf = loadKeyManager();

            // SSLContext (mTLS)
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            // SSLSocket 생성
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            try (SSLSocket socket = (SSLSocket) socketFactory.createSocket(ip, Integer.parseInt(port));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())))
            {
                socket.startHandshake();

                out.write(data + "\n");
                out.flush();

                // 응답 본문을 문자열로 수집
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                // 마지막 개행 제거하여 리턴
                return sb.toString().trim();
            }
        }
        catch (Exception e)
        {
            System.out.println("MTLS 통신 오류! " + OperException.getStackTrace(e));
            return null;
        }
    }


    private KeyManagerFactory loadKeyManager()
    {
        try
        {
            // 키스토어 (서버 개인키/인증서)
            KeyStore ks = getKeyStore(keyStorePath, keyStorePw);

            // 키 매니저
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keyStorePw.toCharArray());
            return kmf;
        }
        catch (Exception e)
        {
            System.out.println("Error loading key manager");
            System.out.println(OperException.getStackTrace(e));
            return null;
        }
    }


    private TrustManagerFactory loadTrustManager()
    {
        try
        {
            // 트러스트스토어 (클라이언트 인증서가 들어있음)
            KeyStore ts = getKeyStore(trustStorePath, trustStorePw);

            // 트러스트 매니저
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            return tmf;
        }
        catch (Exception e)
        {
            System.out.println("Error loading trust manager");
            System.out.println(OperException.getStackTrace(e));
            return null;
        }
    }


    private KeyStore getKeyStore(String path, String pw) {
        try
        {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream tsf = new FileInputStream(path))
            {
                trustStore.load(tsf, pw.toCharArray());
            }

            return trustStore;
        }
        catch (Exception e)
        {
            System.out.println("Error loading key store " + path);
            System.out.println(OperException.getStackTrace(e));
            return null;
        }
    }
}
