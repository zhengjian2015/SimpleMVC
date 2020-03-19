package com.zhengj.learnjava.controller;

import com.zhengj.learnjava.bean.User;
import com.zhengj.learnjava.framework.GetMapping;
import com.zhengj.learnjava.framework.ModelAndView;

import javax.servlet.http.HttpSession;

public class IndexController {

    @GetMapping("/")
    public ModelAndView index(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return new ModelAndView("/index.html", "user", user);
    }

    @GetMapping("/hello")
    public ModelAndView hello(String name) {
        if (name == null) {
            name = "World";
        }
        return new ModelAndView("/hello.html", "name", name);
    }
}
