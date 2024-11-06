package tech.zhiwei.frostmetal.portal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 修复访问knife4j时后台报错：NoResourceFoundException: No static resource favicon.ico
 */
@Controller
public class FaviconController {

    @GetMapping("/favicon.ico")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void returnNoContent() {
    }
}