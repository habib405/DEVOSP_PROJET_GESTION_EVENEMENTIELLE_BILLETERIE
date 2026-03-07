package com.projet.gestion.evenementielle.service;

import com.projet.gestion.evenementielle.dto.AuthResponse;
import com.projet.gestion.evenementielle.dto.LoginRequest;
import com.projet.gestion.evenementielle.dto.RegisterRequest;
import com.projet.gestion.evenementielle.entity.User;
import com.projet.gestion.evenementielle.entity.UserRole;
import com.projet.gestion.evenementielle.repository.UserRepository;
import com.projet.gestion.evenementielle.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }
        // Empêcher l'auto-inscription avec des rôles privilégiés
        if (request.getRole() == UserRole.ADMIN || request.getRole() == UserRole.STAFF) {
            throw new IllegalArgumentException("Cannot self-register with role: " + request.getRole()
                    + ". Only ATTENDEE and ORGANIZER are allowed.");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}
