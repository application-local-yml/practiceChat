package com.ll.chat.domain.home;

import com.ll.chat.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class HomeController {
    private final Rq rq;

    @GetMapping("/")
    public String showMain() {
        if (rq.isLogout()) return "redirect:/usr/main/home";

        return "redirect:/usr/main/home";
    }

    @GetMapping("/usr/main/home")
    public String showHome(Model model) {

        return "usr/main/home";
    }
}
