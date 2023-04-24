package nl.codegorilla.security.authentication.services;

import nl.codegorilla.security.authentication.dto.AuthenticationRequest;
import nl.codegorilla.security.authentication.dto.AuthenticationResponse;
import nl.codegorilla.security.authentication.dto.RegistrationRequest;
import nl.codegorilla.security.authentication.models.ApplicationUser;
import nl.codegorilla.security.authentication.models.Role;
import nl.codegorilla.security.authentication.repositories.UserRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    Logger logger = LoggerFactory.getLogger(AuthenticationService.class);


    public AuthenticationService(UserRepository userRepository, PasswordEncoder encoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public String register(RegistrationRequest request) {
        ApplicationUser user = ApplicationUser.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .username(request.getUsername())
                .password(encoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        try {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                throw new Exception("User " + user.getUsername() + " already exists");
            } else {
            userRepository.save(user);
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            return e.getMessage();
        }
        logger.info("User " + user.getUsername() + " registered");
        return "User " + user.getUsername() + " registered";
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        ApplicationUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        var jwtToken = jwtService.generateToken(user);
        logger.info("User " + user.getUsername() + " logged in");

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
