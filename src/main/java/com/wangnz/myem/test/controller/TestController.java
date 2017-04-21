package com.wangnz.myem.test.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Administrator on 2017/4/21.
 */
@Controller
public class TestController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/test1")
    @ResponseBody
    public String test1() {
        log.info("oks");
        return "ok";
    }
}
