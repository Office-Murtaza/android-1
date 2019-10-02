package com.batm.rest;

import com.batm.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.batm.model.Response;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/sms/send")
    public Response sendSMS(@RequestParam String phone) {
        return Response.ok(messageService.sendMessage(phone, "Hey there, this is a test message!!!"));
    }
}