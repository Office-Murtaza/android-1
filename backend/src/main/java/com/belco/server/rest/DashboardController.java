package com.belco.server.rest;

import com.belco.server.dto.MessageDTO;
import com.belco.server.dto.PasswordDTO;
import com.belco.server.dto.PhoneDTO;
import com.belco.server.dto.VerificationDTO;
import com.belco.server.model.Response;
import com.belco.server.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final UserService userService;
    private final UserController userController;

    public DashboardController(UserService userService, UserController userController) {
        this.userService = userService;
        this.userController = userController;
    }

    @PostMapping("/message")
    public Response sendMessage(@RequestBody MessageDTO dto) {
        return Response.ok(true);
    }

    @PostMapping("/user/{userId}/phone")
    public Response updatePhone(@PathVariable Long userId, @RequestBody PhoneDTO dto) {
        return userController.updatePhone(userId, dto);
    }

    @PostMapping("/user/{userId}/password")
    public Response updatePassword(@PathVariable Long userId, @RequestBody PasswordDTO dto) {
        return userController.updatePassword(userId, dto);
    }

    @PostMapping("/user/{userId}/verification")
    public Response updateVerification(@PathVariable Long userId, @RequestBody VerificationDTO dto) {
        return userService.updateVerification(userId, dto);
    }
}