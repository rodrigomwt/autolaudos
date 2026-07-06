// Modal de Termos e Condições
document.addEventListener('DOMContentLoaded', function() {
	const modal = document.getElementById('modalTermos');
	const linkTermos = document.querySelector('a[href="#termos"]');
	const closeBtn = document.querySelector('.close-modal');

	// Abrir modal ao clicar no link
	if (linkTermos) {
		linkTermos.addEventListener('click', function(e) {
			e.preventDefault();
			modal.style.display = 'block';
			document.body.style.overflow = 'hidden'; // Previne scroll da página
		});
	}

	// Fechar modal ao clicar no X
	if (closeBtn) {
		closeBtn.addEventListener('click', function() {
			modal.style.display = 'none';
			document.body.style.overflow = 'auto';
		});
	}

	// Fechar modal ao clicar fora dele
	window.addEventListener('click', function(event) {
		if (event.target === modal) {
			modal.style.display = 'none';
			document.body.style.overflow = 'auto';
		}
	});

	// Fechar modal com a tecla ESC
	document.addEventListener('keydown', function(event) {
		if (event.key === 'Escape' && modal.style.display === 'block') {
			modal.style.display = 'none';
			document.body.style.overflow = 'auto';
		}
	});
});