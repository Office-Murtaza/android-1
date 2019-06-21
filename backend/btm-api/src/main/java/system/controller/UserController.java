package system.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.context.request.RequestContextHolder;
import system.dao.CodeVerificationCode;
import system.dao.CodeVerificationResult;
import system.model.CodeVerification;
import system.model.POJO.ConfirmPhoneCodeInput;
import system.model.POJO.CreateUserInput;
import system.model.POJO.LoginInput;
import system.model.POJO.LogoutInput;
import system.model.User;
import system.service.CodeVerificationService;
import system.service.TwilioService;
import system.service.UserService;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    UserService userService;
    @Autowired
    CodeVerificationService codeVerificationService;
    @Autowired
    TwilioService twilioService;

    @RequestMapping(value="/", method = RequestMethod.GET)
    public @ResponseBody
    String getIndex() {
        return "hello";
    }

    @RequestMapping(value="/create", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<JsonNode> addEvent(@RequestBody CreateUserInput input) {
        System.out.println(input.phone + input.password);

        User u = new User();
        u.setPhone(input.phone);
        String hashedPass = org.apache.commons.codec.digest.DigestUtils.sha256Hex(input.password);
        u.setPassword(hashedPass);

        int resultCode;
        long userId = -1;
        Boolean ifUserExistsCheckResult = userService.isUserWithConfirmedPhoneExist(u);
        if(ifUserExistsCheckResult == null || ifUserExistsCheckResult) {
            resultCode = 1;
        } else {
            userId = userService.create(u).getUserId();
            resultCode = 0;

            String code = String.format("%04d%n", new Random().nextInt(10000));
            twilioService.sendCode(u.getPhone(), code);

            CodeVerification verification = new CodeVerification();
            verification.setPhone(u.getPhone());
            verification.setCode(code);
//            verification.setUserId(userId);
            verification.setUser(u);

            codeVerificationService.create(verification);
        }

        ObjectNode response = mapper.createObjectNode();
        response.put("code", resultCode);
        response.put("userId", userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value="/phone_confirmation", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<JsonNode> confirmPhoneCode(@RequestBody ConfirmPhoneCodeInput input) {
        System.out.println(input.userId + "" + input.smsCode);

        CodeVerificationResult verificationResult =
                codeVerificationService.isCodeTheSameAsTheLastCodeSentToUser(input.userId, input.smsCode);
        CodeVerificationCode verificationResultCode = verificationResult.getCode();

        if(verificationResultCode == CodeVerificationCode.OK) {
            CodeVerification latestVerification = verificationResult.getLatestVerification();
            latestVerification.setCodeConfirmed(1);
            codeVerificationService.update(latestVerification);

            User user = latestVerification.getUser();
            user.setPhoneConfirmed(1);
            userService.update(user);
        }

        ObjectNode response = mapper.createObjectNode();
        response.put("code", verificationResultCode != null? verificationResultCode.getCode() : 1);
        response.put("sessionId", "SESS");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value="/login", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<JsonNode> login(@RequestBody LoginInput input) {
        System.out.println(input.phone + input.password);

        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();

        ObjectNode response = mapper.createObjectNode();
        response.put("userId", 1);
        response.put("sessionId", sessionId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Resource(name="authenticationManager")
    private AuthenticationManager authManager;

    @RequestMapping(value = "/login2", method = RequestMethod.POST)
    public ResponseEntity<JsonNode> login2(@RequestBody LoginInput input, final HttpServletRequest request) {
        String username = input.phone;
        String password = input.password;
        UsernamePasswordAuthenticationToken authReq =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authManager.authenticate(authReq);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", sc);

        String sessionId = session.getId();

        ObjectNode response = mapper.createObjectNode();
        response.put("userId", 1);
        response.put("sessionId", sessionId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value="/logout", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<JsonNode> logout(@RequestBody LogoutInput input, final HttpServletRequest request) {
        System.out.println(input.userId + input.sessionId);

        request.getSession().invalidate();

        ObjectNode response = mapper.createObjectNode();
        response.put("code", 0);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
