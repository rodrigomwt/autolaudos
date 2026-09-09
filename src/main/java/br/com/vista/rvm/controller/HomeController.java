package br.com.vista.rvm.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.vista.rvm.dto.UserDTO;
import br.com.vista.rvm.dto.enums.Plans;
import br.com.vista.rvm.service.OrderService;
import br.com.vista.rvm.service.PaymentService;
import br.com.vista.rvm.service.UserService;
import br.com.vista.rvm.supplier.checktudo.dto.CheckTudoAgregadosResponseDTO;
import br.com.vista.rvm.supplier.checktudo.dto.CheckTudoWebhookDTO;
import br.com.vista.rvm.supplier.checktudo.service.CheckTudoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

	//private final ConferiService conferiService;
	private final CheckTudoService checktudoService;
	private final PaymentService paymentService;
	private final UserService userService;
	private final OrderService orderService;

	private final AuthenticationManager authenticationManager;

	@GetMapping({ "/", "/home" })
	public String index() {
		return "site/Home";
	}

	@PostMapping("/new-session")
	@ResponseBody
	public ResponseEntity<Void> salvarPlaca(@RequestBody Map<String, String> body, HttpSession session) {
		session.setAttribute("veiculo", null);
		session.setAttribute("placa", body.get("placa"));
		return ResponseEntity.ok().build();
	}

	@PostMapping("/selected-plan")
	@ResponseBody
	public ResponseEntity<Void> salvarPlano(@RequestBody Map<String, String> body, HttpSession session) {
		session.setAttribute("plano", body.get("plano"));
		return ResponseEntity.ok().build();
	}

	@GetMapping("/step1")
	public String iniciarPasso1(HttpSession session, Model model) {
		
		var veiculoSession = (CheckTudoAgregadosResponseDTO) session.getAttribute("veiculo");
		if(veiculoSession != null) {
			model.addAttribute("veiculo", veiculoSession.getBody().getData());

			return "site/Step1";
		}

		String placa = (String) session.getAttribute("placa");

		try {
			var veiculo = checktudoService.requestAgregados(placa.replace("-", ""));

			session.setAttribute("veiculo", veiculo);
			model.addAttribute("veiculo", veiculo.getBody().getData());

			return "site/Step1";

		} catch (Exception e) {
			// se der erro, volta pra home com mensagem
			model.addAttribute("erro", "Não foi possível consultar a placa.");
			return "site/Home";
		}
	}

	@GetMapping("/step2")
	public String iniciarPasso2(HttpSession session, Model model) {
		String placa = (String) session.getAttribute("placa");
		model.addAttribute("placa", placa);
		model.addAttribute("planos", Plans.values());

		return "site/Step2";

	}

	@GetMapping("/stepPayment")
	public String stepPayment(HttpSession session, Model model) {
		String planId = (String) session.getAttribute("plano");
		Plans plan = Plans.findById(planId);

		var veiculo = (CheckTudoAgregadosResponseDTO) session.getAttribute("veiculo");
		model.addAttribute("veiculo", veiculo);
		
		String placa = (String) session.getAttribute("placa");
		model.addAttribute("placa", placa);
		model.addAttribute("plano", plan);
		model.addAttribute("planDescription", plan.getDescription());

		return "site/StepPayment";

	}

	@GetMapping("/status-payment/{paymentId}")
	@ResponseBody
	public ResponseEntity<Map<String, String>> getPaymentStatus(@PathVariable String paymentId) {
		try {
			var payment = paymentService.findByExternalId(paymentId);
			return ResponseEntity.ok(Map.of("status", payment.getStatus().name()));
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/usuarios/registrar")
	public ResponseEntity<?> registrar(@RequestBody UserDTO userDto) {
		try {
			userService.register(userDto.getFullName(), userDto.getFederalId(), userDto.getEmail(), userDto.getPhone(), userDto.getPassword());
			return ResponseEntity.ok(Map.of("message", "Cadastro realizado com sucesso!"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao realizar cadastro."));
		}
	}

	@PostMapping("/auth/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request, HttpServletResponse response) {
		try {
			String email = body.get("email");
			String senha = body.get("senha");

			Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, senha));

			SecurityContextHolder.getContext().setAuthentication(auth);

			// Salva a sessão manualmente
			HttpSession session = request.getSession(true);
			session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

			return ResponseEntity.ok(Map.of("message", "Login realizado com sucesso!", "redirect", "/dashboard/consultas" // 👈 adicionado
			));
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(401).body(Map.of("message", "E-mail ou senha incorretos."));
		} catch (DisabledException e) {
			return ResponseEntity.status(403).body(Map.of("message", "Usuário inativo."));
		}
	}
	
	@PostMapping("/order/notification")
	public ResponseEntity<Void> receberNotificacao(@RequestBody CheckTudoWebhookDTO payload) {
	    log.info("Webhook CheckTudo recebido: payload={}", payload);

	    try {
	    	
	    	if(payload.getOrder() == null || payload.getPayload().getData() == null) {
	    		return ResponseEntity.ok().build();
	    	}
	    	
	    	orderService.finishedOrderByExternalOrderId(payload.getOrder().getOrderId(), payload.getPayload().getData().toString());

	    } catch (Exception e) {
	        log.error("Erro ao processar webhook: {}", e.getMessage(), e);
	    }

	    return ResponseEntity.ok().build();
	}

}
