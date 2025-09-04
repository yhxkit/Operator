package com.sample.operator.app.ctrl.common;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultErrorCtrl implements ErrorController {


    // 커스텀 에러페이지
    @RequestMapping(value = "/errorPage")
    public String errorPage(HttpServletRequest request, HttpServletResponse response) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        String errorPageDir = "error/";
        String errorCode = "common";

        if(status != null)
        {
            int statusCode = Integer.parseInt(status.toString());


            if( HttpStatus.NOT_FOUND.value() == statusCode || // 404
                    HttpStatus.FORBIDDEN.value() == statusCode || //403
                    HttpStatus.INTERNAL_SERVER_ERROR.value() == statusCode  || // 500
                    HttpStatus.SERVICE_UNAVAILABLE.value() == statusCode )  // 503
            {
                errorCode = String.valueOf(statusCode);
            }
        }

        return errorPageDir + errorCode;
    }
}
