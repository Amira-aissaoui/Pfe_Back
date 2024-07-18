package com.expo;

import java.util.Optional;

import org.apache.http.HttpHeaders;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.expo.security.config.JwtService;
import com.expo.security.model.AuthenticationRequest;
import com.expo.security.model.AuthenticationResponse;
import com.expo.security.model.RegisterRequest;
import com.expo.security.model.User;
import com.expo.security.repo.TokenRepository;
import com.expo.security.repo.UserRepository;
import com.expo.security.service.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;

class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_ShouldRegisterUser() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        // Set up properties for the request

        User user = User.builder()
                .email(request.getEmail())
                .build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(user)).thenReturn("jwtToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        // When
        AuthenticationResponse response = authenticationService.register(request);

        // Then
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(user);
        verify(jwtService, times(1)).generateRefreshToken(user);
    }

    @Test
    void register_ShouldThrowException_WhenUserAlreadyExists() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        // Set up properties for the request

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(Exception.class, () -> authenticationService.register(request));
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
        verify(jwtService, never()).generateRefreshToken(any(User.class));
    }

    @Test
    void authenticate_ShouldAuthenticateUser() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest();
        // Set up properties for the request

        User user = User.builder()
                .email(request.getEmail())
                .password("$2a$10$d8RsVnpxHZ3eH91m4AnXuODnF2LcKVJw8G1VgxFGBnFUU59ySd8ZS") // You can use an encoded password here for testing
                .build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwtToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        // When
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Then
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(jwtService, times(1)).generateToken(user);
        verify(jwtService, times(1)).generateRefreshToken(user);
        verify(tokenRepository, times(1)).findAllValidTokenByUser(user.getId());
    //    verify(tokenRepository, times(1)).saveAll(anyList());
    }

    @Test
    void refreshToken_ShouldRefreshToken() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse(); // Use MockHttpServletResponse
        String refreshToken = "validRefreshToken";
        User user = User.builder()
                .email("test@example.com")
                .build();
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtService.extractUsername(refreshToken)).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("newJwtToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("newRefreshToken");

        // When
        authenticationService.refreshToken(request, response);

        // Then
        verify(jwtService, times(1)).isTokenValid(refreshToken, user);
        verify(tokenRepository, times(1)).findAllValidTokenByUser(user.getId());
   //     verify(tokenRepository, times(1)).saveAll(anyList());

        // Use response.getContentAsString() to access the response content
        String responseBody = response.getContentAsString();
        assertNotNull(responseBody);
        // You can also verify the response content using assertions based on the expected behavior.
    }

}
