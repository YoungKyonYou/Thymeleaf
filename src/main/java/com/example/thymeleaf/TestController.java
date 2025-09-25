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

    @GetMapping("/test4")
    public String test4(){
        return "page/guide/guide";
    }

    @GetMapping("/test5")
    public String test5(){
        return "page/guide/modal";
    }

    @GetMapping("/test6")
    public String test6(){
        return "page/guide/page1";
    }


    @GetMapping("/test7")
    public String test7(){
        return "page/menu1/menu1";
    }
}
