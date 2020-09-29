package com.studyolle.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping("/sign-up")
    public String sighUpForm(Model model) {
        //model.addAttribute("signUpForm", new SignUpForm());
        model.addAttribute(new SignUpForm()); //클래스의 이름으로 값을 카멜이스로 사용
        return "account/sign-up";
    }
}
