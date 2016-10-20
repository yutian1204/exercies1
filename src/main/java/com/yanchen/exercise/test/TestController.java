package com.yanchen.exercise.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by yuyanchen on 16/10/20.
 */

@Controller
@RequestMapping(value = "/test")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Resource
    private TestService testService;

    @RequestMapping(value = "test")
    @ResponseBody
    public String test(HttpServletRequest request) {
        testService.test();
        return "Success";
    }
}
