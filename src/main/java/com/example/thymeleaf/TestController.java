package com.example.thymeleaf;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    @GetMapping("/test1")
    public String test1(){
        return "index";
    }

    @GetMapping("/test2")
    public String test2(){
        return "page/dashboard/index";
    }

    @GetMapping("/test3")
    public String test3(){
        return "component/aside";
    }
}
