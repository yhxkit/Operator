package com.sample.operator.app.ctrl.chat;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatMvcCtrl {

    // 챗 방 입장
    @GetMapping("/chat")
    public String chatRoom(HttpServletRequest request) {

        String addr = request.getRemoteAddr();
        String user = request.getRemoteUser();

        String name = request.getUserPrincipal() == null  ? "anonymous" : request.getUserPrincipal().getName();

        System.out.println(addr + " 접속 : " + user + "=" + name);

        return "chat/room";
    }


}
