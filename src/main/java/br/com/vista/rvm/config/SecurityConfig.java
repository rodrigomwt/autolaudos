package br.com.vista.rvm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import br.com.vista.rvm.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomUserDetailsService userDetailsService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.userDetailsService(userDetailsService).authorizeHttpRequests(auth -> auth
				.requestMatchers("/static/**", "/site/**", "/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
				.requestMatchers("/", "/home", "/new-session", "/selected-plan", "/step1", "/step2", "/stepPayment", "/status-payment/**",
						"/pagamento/gerar-pix", "/pagamento/notification", "/usuarios/registrar", "/usuarios/primeiro-acesso", "/primeiro-acesso/**",
						"/auth/login", "/fragments/**")
				.permitAll().requestMatchers("/dashboard/**").authenticated().anyRequest().authenticated())
				.formLogin(form -> form.loginPage("/home").loginProcessingUrl("/auth/login-form").defaultSuccessUrl("/dashboard/consultas", true)
						.permitAll())
				.logout(logout -> logout.logoutUrl("/auth/logout").logoutSuccessUrl("/home").permitAll())
				.csrf(csrf -> csrf.ignoringRequestMatchers("/usuarios/registrar", "/auth/login", "/pagamento/gerar-pix-dash",
						"/pagamento/notification", "/site/**"));

		return http.build();
	}

	@Bean
	public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/site/**", "/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico");
	}
}