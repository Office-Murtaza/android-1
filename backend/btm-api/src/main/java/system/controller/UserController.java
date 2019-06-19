package system.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import system.model.POJO.ConfirmPhoneCodeInput;
import system.model.POJO.CreateUserInput;
import system.model.POJO.LoginInput;
import system.model.POJO.LogoutInput;
import system.model.User;
import system.service.CodeVerificationService;
import system.service.UserService;

@Controller
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    UserService userService;
    @Autowired
    CodeVerificationService codeVerificationService;

    @RequestMapping(value="/create", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<JsonNode> addEvent(@RequestBody CreateUserInput input) {
        System.out.println(input.phone + input.password);

        ObjectNode response = mapper.createObjectNode();
        response.put("code", 1);
        response.put("userId", 1);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value="/phone_confirmation", method = RequestMethod.POST)
    public @ResponseBody
    JSONObject confirmPhoneCode(@RequestBody ConfirmPhoneCodeInput input) {
        System.out.println(input.userId + "" + input.smsCode);

        JSONObject response = new JSONObject();
        response.put("code", 1);
        response.put("sessionId", "SESS");
        return response;
    }

    @RequestMapping(value="/login", method = RequestMethod.POST)
    public @ResponseBody
    JSONObject login(@RequestBody LoginInput input) {
        System.out.println(input.phone + input.password);

        JSONObject response = new JSONObject();
        response.put("userId", 1);
        response.put("sessionId", "SESS");
        return response;
    }

    @RequestMapping(value="/logout", method = RequestMethod.POST)
    public @ResponseBody
    JSONObject logout(@RequestBody LogoutInput input) {
        System.out.println(input.userId + input.sessionId);

        JSONObject response = new JSONObject();
        response.put("code", 0);
        return response;
    }
}
