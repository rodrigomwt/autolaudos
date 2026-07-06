package br.com.vista.rvm.service;

import br.com.vista.rvm.entity.ActivationToken;
import br.com.vista.rvm.entity.UserCredential;
import br.com.vista.rvm.repository.ActivationTokenRepository;
import br.com.vista.rvm.repository.UserCredentialRepository;
import br.com.vista.rvm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivationService {

	private final UserRepository userRepository;
	private final UserCredentialRepository userCredentialRepository;
	private final ActivationTokenRepository tokenRepository;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void requestActivation(String email) {
		var user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Email não encontrado"));

		String tokenValue = UUID.randomUUID().toString();

		ActivationToken token = new ActivationToken();
		token.setToken(tokenValue);
		token.setUser(user);
		token.setExpiration(LocalDateTime.now().plusHours(1));
		token.setUsed(false);

		tokenRepository.save(token);

		emailService.sendActivationEmail(user.getEmail(), tokenValue);
	}

	public ActivationToken validateToken(String tokenValue) {
		return tokenRepository.findByToken(tokenValue).filter(t -> !t.getUsed()).filter(t -> t.getExpiration().isAfter(LocalDateTime.now()))
				.orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));
	}

	@Transactional
	public void definePassword(String tokenValue, String newPassword) {
		ActivationToken token = validateToken(tokenValue);

		var credentialOpt = userCredentialRepository.findByUser(token.getUser());

		if (credentialOpt.isPresent()) {
			// Atualiza senha existente
			UserCredential credential = credentialOpt.get();
			credential.setPassword(passwordEncoder.encode(newPassword));
			userCredentialRepository.save(credential);
		} else {
			// Cria novo registro de credencial
			UserCredential credential = new UserCredential();
			credential.setUser(token.getUser());
			credential.setPassword(passwordEncoder.encode(newPassword));
			credential.setActive(true);
			userCredentialRepository.save(credential);
		}

		// Invalida o token
		token.setUsed(true);
		tokenRepository.save(token);
	}
}