package com.sample.operator.app.ctrl.common.mnt;

import com.sample.operator.app.common.mnt.MntBiz;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@RequiredArgsConstructor
@Controller
public class MntCtrl {

    private final MntBiz mntBiz;

    @RequestMapping("/mnt")
    public String mnt(Model model)
    {
        model.addAttribute("metrics", mntBiz.getMetrics());
        return "mnt/metrics";
    }
}
