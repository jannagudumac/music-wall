package com.musicwall.service;

import com.musicwall.dto.AuthResponse;
import com.musicwall.dto.LoginRequest;
import com.musicwall.dto.RegisterRequest;
import com.musicwall.entity.UserEntity;
import com.musicwall.exception.BusinessException;
import com.musicwall.repository.UserRepository;
import com.musicwall.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// The service handles account creation and login.
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Spring supplies the dependencies through this constructor.
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        // Reject a username that is already registered.
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("This username is already used");
        }

        // Create the account with a role chosen by the server, not by the user.
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        // Hash the password with BCrypt so the plain password is never stored.
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        // Save the account; the database generates its id.
        userRepository.save(user);

        return createResponse(user);
    }

    // Ask Spring Security to check the username and password before issuing a JWT.
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        return createResponse(user);
    }

    // Return the JWT, username and role, but never the password.
    private AuthResponse createResponse(UserEntity user) {
        AuthResponse response = new AuthResponse();
        response.setToken(jwtUtil.generateToken(user.getUsername()));
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        return response;
    }
}
