package com.batm.controller;

import com.batm.entity.Response;
import com.batm.entity.User;
import com.batm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/v1")
public class TestController {

    @Autowired
    private UserService userService;

    @GetMapping(value = "/test")
    public Response test() {
        return Response.ok(userService.test());
    }

    @PostMapping(value = "/test")
    public Response test(@RequestBody User user) {
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        user.setRole("USER");

        return Response.ok(userService.register(user));
    }
}