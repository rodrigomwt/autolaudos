package br.com.vista.rvm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import br.com.vista.rvm.entity.Plan;
import br.com.vista.rvm.entity.User;
import br.com.vista.rvm.repository.PlanRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

	private final PlanRepository planRepository;
	private final UserService userService;

	public Plan create(Long userId, String planName, String description, BigDecimal planValue) {
		User user = userService.findById(userId);

		Plan plan = new Plan();
		plan.setPlan(planName);
		plan.setDescription(description);
		plan.setPlanValue(planValue);
		plan.setUser(user);
		
		return planRepository.save(plan);
	}

	public Plan findById(Long id) {
		return planRepository.findById(id).orElseThrow(() -> new RuntimeException("Plan not found: " + id));
	}

	public List<Plan> findByUserId(Long userId) {
		userService.findById(userId); // valida se o usuário existe
		return planRepository.findByUserId(userId);
	}

	public List<Plan> findAll() {
		return planRepository.findAll();
	}

	public Plan update(Long id, Plan updated) {
		Plan existing = findById(id);
		existing.setPlan(updated.getPlan());
		existing.setDescription(updated.getDescription());
		existing.setPlanValue(updated.getPlanValue());
		return planRepository.save(existing);
	}

	public void delete(Long id) {
		findById(id);
		planRepository.deleteById(id);
	}
}
