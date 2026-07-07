package com.exam.backend.controller;

import com.exam.backend.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("后端启动成功！");
    }
}
