package mandarin.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import mandarin.auth.UserType;
import mandarin.dao.UserRepository;
import mandarin.entities.User;
import mandarin.utils.CryptoUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthenticationController {
    @Resource
    UserRepository userRepository;

    class Response {
        @JsonProperty
        boolean success;
        @JsonProperty
        String message;

        public Response(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping(value = "/register", produces = "application/json")
    @ResponseBody
    public ResponseEntity register(@RequestParam String username,
                                   @RequestParam String password,
                                   @RequestParam String type) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setType(Enum.valueOf(UserType.class,type));
        userRepository.save(user);
        return ResponseEntity.ok().body(new Response(true, "registered"));
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping(value = "/login", produces = "application/json")
    @ResponseBody
    public ResponseEntity login(@RequestParam String username,
                                @RequestParam String password,
                                HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return ResponseEntity.badRequest().body(new Response(false, "already logged in"));
        } else {
            User user = userRepository.findByUsername(username);
            if (user == null || !CryptoUtils.verifyPassword(password, user.getPasswordHash())) {
                return ResponseEntity.badRequest().body(new Response(false, "username or password incorrect"));
            }
            session.setAttribute("userId", user.getId());
            session.setAttribute("userType", user.getType());
            return ResponseEntity.ok().body(new Response(true, "logged in"));
        }
    }

    @PostMapping(value = "/logout", produces = "application/json")
    @ResponseBody
    public ResponseEntity logout(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(false, "not logged in"));
        } else {
            session.removeAttribute("userId");
            session.removeAttribute("userType");
            return ResponseEntity.ok().body(new Response(true, "logged out"));
        }
    }
}
