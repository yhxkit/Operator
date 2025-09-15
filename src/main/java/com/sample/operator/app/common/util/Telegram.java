package com.sample.operator.app.common.util;

import com.sample.operator.app.common.connection.HyperTextTransferProtocol;
import com.sample.operator.app.common.crypt.AesCryptor;
import com.sample.operator.app.common.exception.OperException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Telegram
{
    private final AesCryptor aesCryptor;
    private final HyperTextTransferProtocol https;


    @Value("${connection.oper.telegram.domain}")
    String domain;

    @Value("${connection.oper.telegram.timeout}")
    String timeout;

    @Value("${connection.oper.telegram.room-id}")
    String chatRoomId;

    @Value("${connection.oper.telegram.bot-token}")
    String botTokenEnc;

    static String notiUrl;

    public Telegram (AesCryptor aesCryptor, HyperTextTransferProtocol https,
                     @Value("${connection.oper.telegram.domain}") String domain,
                     @Value("${connection.oper.telegram.timeout}") String timeout,
                     @Value("${connection.oper.telegram.room-id}") String chatRoomId,
                     @Value("${connection.oper.telegram.bot-token}") String botTokenEnc)
    {
        this.aesCryptor = aesCryptor;
        this.https = https;
        this.domain = domain;
        this.timeout = timeout;
        this.chatRoomId = chatRoomId;
        this.botTokenEnc = botTokenEnc;
    }

    @PostConstruct
    private void init()
    {
        try
        {
            // https://api.telegram.org/bot6290681410:AAG7BoCfKfITZDLAJEFyerzKVvk73Jbo14k/sendMessage?chat_id=1936935453&text=TestBotTest
            String botTokDec = aesCryptor.decrypt(botTokenEnc, null, null, null);
            this.notiUrl = domain + botTokDec + "/sendMessage?chat_id=" + chatRoomId + "&text=";
        }
        catch (Exception e)
        {
            System.out.println("노티 URL 복호화실패" + OperException.getStackTrace(e));
        }
    }


    public void noti(String msg)
    {
        if(this.notiUrl == null)
        {
            init();
        }

        String result = https.httpConn(notiUrl + msg , "", Integer.parseInt(timeout));
        System.out.println("result:" + result);
    }
}
