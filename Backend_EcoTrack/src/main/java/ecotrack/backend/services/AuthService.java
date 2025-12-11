package ecotrack.backend.services;

import ecotrack.backend.dto.AuthResponse;
import ecotrack.backend.dto.LoginRequest;
import ecotrack.backend.dto.RegisterRequest;
import ecotrack.backend.models.entitys.User;
import ecotrack.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registrar nuevo usuario
     */
    public AuthResponse register(RegisterRequest request) {
        // Validar que el email no exista
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Crear usuario
        User user = User.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(request.getPassword()) // TODO: Hashear en producción
                .build();

        User savedUser = userRepository.save(user);

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .nombre(savedUser.getNombre())
                .email(savedUser.getEmail())
                .message("Usuario registrado exitosamente")
                .build();
    }

    /**
     * Login de usuario
     */
    public AuthResponse login(LoginRequest request) {
        // Buscar usuario por email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos"));

        // Validar contraseña
        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("Email o contraseña incorrectos");
        }

        return AuthResponse.builder()
                .userId(user.getId())
                .nombre(user.getNombre())
                .email(user.getEmail())
                .message("Login exitoso")
                .build();
    }
}