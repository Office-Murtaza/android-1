package com.batm.controller;

import com.batm.entity.Response;
import com.batm.entity.User;
import com.batm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Date;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/register")
    public Response register(@RequestBody User user) {
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        user.setRole("USER");

        return Response.ok(userService.register(user));
    }

    @PostMapping(value = "/login")
    public Response login(@RequestBody User user) {
        return Response.ok(userService.login(user));
    }

    @GetMapping(value = "/test")
    public Response test() {
        return Response.ok(userService.test());
    }
}