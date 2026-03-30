package com.orrs.controller;

import com.orrs.model.User;
import com.orrs.repository.UserRepository;
import com.orrs.security.JwtUtil;
import com.orrs.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req) {
        User user = new User(req.getFullName(), req.getEmail(), req.getPassword(), req.getRole());
        User saved = userService.registerUser(user);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Registered successfully");
        res.put("userId", saved.getId());
        res.put("role", saved.getRole());
        return res;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail());
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("uid", user.getId());

        String token = jwtUtil.generateToken(user.getEmail(), claims);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("token", token);
        res.put("userId", user.getId());
        res.put("role", user.getRole());
        res.put("fullName", user.getFullName());
        return res;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class RegisterRequest {
        private String fullName;
        private String email;
        private String password;
        private User.Role role;
    }
}
