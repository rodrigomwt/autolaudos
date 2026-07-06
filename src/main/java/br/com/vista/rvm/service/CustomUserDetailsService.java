package br.com.vista.rvm.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.vista.rvm.entity.UserCredential;
import br.com.vista.rvm.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserCredentialRepository credentialRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserCredential credential = credentialRepository.findByUserEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

		if (!credential.getActive()) {
			throw new DisabledException("Usuário inativo.");
		}

		return org.springframework.security.core.userdetails.User.builder().username(credential.getUser().getEmail())
				.password(credential.getPassword()).roles("USER").build();
	}
}