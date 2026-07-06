package br.com.vista.rvm.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.vista.rvm.dto.UserDTO;
import br.com.vista.rvm.entity.User;
import br.com.vista.rvm.entity.UserCredential;
import br.com.vista.rvm.repository.UserCredentialRepository;
import br.com.vista.rvm.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserCredentialRepository credentialRepository;
	private final PasswordEncoder passwordEncoder;

	public User create(String name, String federalId, String email, String phone) {
		User user = new User();

		if (!userRepository.existsByEmail(email)) {
			user.setName(name);
			user.setFederalId(federalId);
			user.setEmail(email);
			user.setPhone(phone);

			userRepository.save(user);
		} else {
			user = findByEmail(email);
		}

		return user;
	}

	public void register(String name, String federalId, String email, String phone, String password) {
		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("E-mail já cadastrado.");
		}

		User user = new User();
		user.setName(name);
		user.setFederalId(federalId);
		user.setEmail(email);
		user.setPhone(phone);
		userRepository.save(user);

		UserCredential credential = new UserCredential();
		credential.setUser(user);
		credential.setPassword(passwordEncoder.encode(password));
		credentialRepository.save(credential);
	}

	public User findById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found: " + id));
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: " + email));
	}

	public List<User> findAll() {
		return userRepository.findAll();
	}

	public User update(Long id, UserDTO dto) {
	    User user = userRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

	    if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
	        throw new RuntimeException("Este e-mail já está em uso.");
	    }

	    // Busca credencial ANTES de alterar o email
	    UserCredential credential = credentialRepository.findByUserEmail(user.getEmail())
	            .orElseThrow(() -> new RuntimeException("Credencial não encontrada"));

	    user.setName(dto.getFullName());
	    user.setEmail(dto.getEmail());
	    user.setPhone(dto.getPhone());
	    userRepository.save(user);

	    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
	        credential.setPassword(passwordEncoder.encode(dto.getPassword()));
	        credentialRepository.save(credential);
	    }

	    return user;
	}

	public void delete(Long id) {
		findById(id); // garante que existe antes de deletar
		userRepository.deleteById(id);
	}
}