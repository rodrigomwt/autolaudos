package br.com.vista.rvm.repository;

import br.com.vista.rvm.entity.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    Optional<ActivationToken> findByToken(String token);
}