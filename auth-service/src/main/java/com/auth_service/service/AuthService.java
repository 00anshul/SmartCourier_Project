package com.auth_service.service;

import com.auth_service.dto.AuthResponse;
import com.auth_service.dto.LoginRequest;
import com.auth_service.dto.RegisterRequest;
import com.auth_service.entity.RefreshToken;
import com.auth_service.entity.User;
import com.auth_service.repository.RefreshTokenRepository;
import com.auth_service.repository.UserRepository;
import com.auth_service.security.JwtUtil;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.auth_service.config.RabbitMQConfig;

@Service
public class AuthService {

	// 1. Initialize the Logger for Security Auditing
	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

	private RedisTokenService redisTokenService;
	private RabbitTemplate rabbitTemplate;
	private UserRepository userRepository;
	private RefreshTokenRepository refreshTokenRepository;
	private PasswordEncoder passwordEncoder;
	private JwtUtil jwtUtil;
	private AuthenticationManager authenticationManager;
	
	public AuthService(UserRepository userRepository,
	                   RefreshTokenRepository refreshTokenRepository,
	                   PasswordEncoder passwordEncoder,
	                   JwtUtil jwtUtil,
	                   AuthenticationManager authenticationManager,
	                   RabbitTemplate rabbitTemplate,
	                   RedisTokenService redisTokenService,
	                   @Value("${jwt.refresh-expiration}") Long refreshExpiration) {
	    this.userRepository = userRepository;
	    this.refreshTokenRepository = refreshTokenRepository;
	    this.passwordEncoder = passwordEncoder;
	    this.jwtUtil = jwtUtil;
	    this.authenticationManager = authenticationManager;
	    this.rabbitTemplate = rabbitTemplate;
	    this.redisTokenService = redisTokenService;
	    this.refreshExpiration = refreshExpiration;
	}


	@Value("${jwt.refresh-expiration}")
	private Long refreshExpiration;

	public User register(RegisterRequest request) {
		logger.info("Registration attempt for email: {}", request.getEmail());

		if (userRepository.existsByEmail(request.getEmail())) {
			logger.warn("Registration failed: Email already exists - {}", request.getEmail());
			throw new RuntimeException("Email already registered");
		}

		User user = new User();
		user.setFullName(request.getFullName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setPhone(request.getPhone());
		user.setRole("CUSTOMER");

		User savedUser = userRepository.save(user);
		logger.info("User successfully registered with ID: {}", savedUser.getId());

		rabbitTemplate.convertAndSend(RabbitMQConfig.USER_REGISTERED_EXCHANGE, RabbitMQConfig.USER_REGISTERED_KEY,
				user.getEmail());
		logger.info("Published registration event to RabbitMQ for email: {}", user.getEmail());

		return savedUser;
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		logger.info("Login attempt for email: {}", request.getEmail());

		// Note: If this fails, Spring Security automatically throws an exception
		// (BadCredentialsException)
		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> {
			logger.error("Login critical error: Authenticated user not found in database - {}", request.getEmail());
			return new RuntimeException("User not found");
		});

		String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
		logger.info("JWT Access Token generated successfully for User ID: {}", user.getId());
		 // Store in Redis
	    redisTokenService.storeToken(user.getId(), accessToken);

		createRefreshToken(user);

		return new AuthResponse(accessToken, user.getRole(), user.getId(), user.getFullName());
	}

	private RefreshToken createRefreshToken(User user) {
		logger.debug("Deleting old refresh tokens for User ID: {}", user.getId());
		refreshTokenRepository.deleteByUser_Id(user.getId());

		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(user);
		refreshToken.setToken(UUID.randomUUID().toString());
		refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));

		RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
		logger.info("New refresh token generated for User ID: {}", user.getId());

		return savedToken;
	}

	@Transactional
	public void logout(Long userId) {
		logger.info("Logout requested for User ID: {}", userId);
		refreshTokenRepository.deleteByUser_Id(userId);
		redisTokenService.deleteToken(userId);
		logger.info("Refresh tokens successfully cleared for User ID: {}", userId);
	}

	public User getUserById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> {
			logger.warn("User lookup failed: No user found with ID: {}", id);
			return new RuntimeException("User not found");
		});
	}
}