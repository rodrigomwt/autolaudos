package br.com.vista.rvm.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.vista.rvm.dto.OrderDTO;
import br.com.vista.rvm.dto.UserDTO;
import br.com.vista.rvm.entity.Order;
import br.com.vista.rvm.entity.User;
import br.com.vista.rvm.service.OrderService;
import br.com.vista.rvm.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
	private final OrderService orderService;
	private final UserService userService;

	@GetMapping({ "/", "/consultas" })
	@Transactional(readOnly = true)
	public String consultas(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(required = false) String placa,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "false") boolean laudoPendente, Model model) {

		try {
			Long userId = userService.findByEmail(userDetails.getUsername()).getId();

			Pageable pageable = PageRequest.of(page, size, Sort.by("createDate").descending());

			Page<Order> pageOrders = (placa != null && !placa.isBlank()) ? orderService.findByUserIdAndLicensePlate(userId, placa, pageable)
					: orderService.findByUserId(userId, pageable);

			// Converte para DTO — regras de badge/label ficam fora da entidade
			Page<OrderDTO> pageDTO = pageOrders.map(OrderDTO::new);

			model.addAttribute("page", pageDTO);
			model.addAttribute("filtroPlaca", placa);
			model.addAttribute("activePage", "consultas");
			model.addAttribute("laudoPendente", laudoPendente);

			return "dashboard/consultas";
		} catch (Exception e) {
			throw new RuntimeException("ERRO NO SERVIDOR: " + e.getMessage() + " - CAUSA: " + e.getCause());
		}

	}
	
	@GetMapping("/consultas/laudo/{id}")
	public String abrirPaginaLaudo(@PathVariable Long id, Model model) {
	    model.addAttribute("laudoId", id);
	    model.addAttribute("activePage", "laudo");
	    return "dashboard/laudo";
	}
	
	@GetMapping("/consultas/laudo/{id}/dados")
	@ResponseBody
	public ResponseEntity<?> buscarDadosLaudo(@PathVariable Long id) {
	    try {
	        String laudo = orderService.gerarLaudoChecktudo(id);

	        if (laudo == null || laudo.isBlank()) {
	            return ResponseEntity.ok(Map.of(
	                "pendente", true
	            ));
	        }

	        return ResponseEntity.ok(Map.of(
	            "pendente", false,
	            "laudo", laudo
	        ));

	    } catch (Exception e) {
	        return ResponseEntity.internalServerError().body(Map.of(
	            "erro", true,
	            "mensagem", "Não foi possível carregar o laudo."
	        ));
	    }
	}
	
	@GetMapping("/profile")
    public String perfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("activePage", "meus-dados");
        return "dashboard/profile";
    }

	@PostMapping("/profile")
    public String salvar(@AuthenticationPrincipal UserDetails userDetails,
                         @ModelAttribute UserDTO dto,
                         RedirectAttributes redirectAttributes) {
        try {
            Long userId = userService.findByEmail(userDetails.getUsername()).getId();
            userService.update(userId, dto);
            redirectAttributes.addFlashAttribute("sucesso", "Seus dados foram atualizados com sucesso!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/dashboard/profile";
    }
}
