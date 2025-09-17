package com.sample.operator.app.ctrl.chat;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;
import java.util.TreeSet;

@Controller
public class LottoMvcCtrl
{
    @RequestMapping(value = "/lotto", method = {RequestMethod.GET})
    public String toPage()
    {
        return "chat/lottoToday";
    }


    @RequestMapping(value = "/lotto", method = {RequestMethod.POST})
    public String toPage(Model model)
    {
        // 자동 정렬을 위해 TreeSet 사용
        Set<Integer> randomSet = new TreeSet<>();

        while (randomSet.size() < 6)
        {
            //range 0.0 ~ 0.999..
            int ballNum = ((int) (Math.random() * 45)) + 1;
            randomSet.add(ballNum);
        }

        model.addAttribute("randomSet", randomSet);

        return "chat/lottoToday";
    }
}
