package com.sample.operator.app.ctrl.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.List;

@ControllerAdvice
public class CtrlAdvice {
    
    //mvc 뷰 페이지에서 현재 경로에 따른 aside 메뉴명 활성화를 위해 servletPath 설정
    // 현재 경로 servletPath 실어줌
    @ModelAttribute("servletPath")
    public String getServletPath(HttpServletRequest request) {
        return request.getServletPath();
    }

    // mvc 뷰에서 현재 경로에 따른 breadCrumb 노출을 위해 설정
    // 현재 경로를 /로 split 하여 breadCrumb에 실어줌
    @ModelAttribute("breadCrumb")
    public List<String> getBreadCrumb(HttpServletRequest request) {
        String[] pathStrArr = request.getServletPath().split("/");
        return Arrays.stream(pathStrArr).toList();
    }
}
