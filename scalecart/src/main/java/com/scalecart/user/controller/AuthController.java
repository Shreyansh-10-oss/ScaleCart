package com.scalecart.user.controller;
import com.scalecart.user.dto.RegisterRequest;
import com.scalecart.user.entity.User;
import com.scalecart.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.scalecart.user.dto.RegisterResponse;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = userService.register(request);

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
