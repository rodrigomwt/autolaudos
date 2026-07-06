package br.com.vista.rvm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.vista.rvm.entity.User;
import br.com.vista.rvm.entity.UserCredential;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
	Optional<UserCredential> findByUserEmail(String email);

	Optional<UserCredential> findByUser(User user);
}