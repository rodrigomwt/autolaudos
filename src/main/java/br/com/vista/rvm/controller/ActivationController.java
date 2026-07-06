package br.com.vista.rvm.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.vista.rvm.dto.DefinirSenhaRequest;
import br.com.vista.rvm.dto.FirstAccessDTO;
import br.com.vista.rvm.service.ActivationService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/primeiro-acesso")
@RequiredArgsConstructor
public class ActivationController {

	private final ActivationService activationService;

	@PostMapping
	public ResponseEntity<?> solicitarAtivacao(@RequestBody FirstAccessDTO dto) {
		try {
			activationService.requestActivation(dto.getEmail());
			return ResponseEntity.ok(Map.of("message", "Email enviado! Verifique sua caixa de entrada."));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao enviar email, tente novamente em alguns minutos."));
		}
	}

	@GetMapping("/validar-token")
	@ResponseBody
	public ResponseEntity<Void> validarToken(@RequestParam String token) {
	    try {
	        activationService.validateToken(token);
	        return ResponseEntity.ok().build();
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().build();
	    }
	}

	@PostMapping("/definir-senha")
	@ResponseBody
	public ResponseEntity<String> definirSenha(@RequestBody DefinirSenhaRequest request) {
	    try {
	        activationService.definePassword(request.getToken(), request.getSenha());
	        return ResponseEntity.ok().build();
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    }
	}
}