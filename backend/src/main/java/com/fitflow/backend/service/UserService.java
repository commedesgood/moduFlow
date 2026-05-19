package com.fitflow.backend.service;

import com.fitflow.backend.dto.LoginRequest;
import com.fitflow.backend.dto.SignupRequest;
import com.fitflow.backend.entity.AuthProvider;
import com.fitflow.backend.entity.User;
import com.fitflow.backend.exception.CustomException;
import com.fitflow.backend.repository.UserRepository;
import com.fitflow.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public String signup(SignupRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException(HttpStatus.UNPROCESSABLE_ENTITY, "Passwords do not match.");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(HttpStatus.CONFLICT, "Email already exists.");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .name("User")
                .build();

        userRepository.save(user);
        return "Signup succeeded.";
    }

    public String login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        if (user.getPassword() == null
                || user.getPassword().isBlank()
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        return jwtUtil.generateToken(user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
