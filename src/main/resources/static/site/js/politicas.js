// Modal de Política de Privacidade
document.addEventListener('DOMContentLoaded', function() {
    const modalPrivacidade = document.getElementById('modalPrivacidade');
    const linkPrivacidade = document.querySelector('a[href="#privacidade"]');
    const closeBtnPrivacidade = document.querySelector('.close-modal-privacidade');

    // Abrir modal ao clicar no link
    if (linkPrivacidade) {
        linkPrivacidade.addEventListener('click', function(e) {
            e.preventDefault();
            modalPrivacidade.style.display = 'block';
            document.body.style.overflow = 'hidden';
        });
    }

    // Fechar modal ao clicar no X
    if (closeBtnPrivacidade) {
        closeBtnPrivacidade.addEventListener('click', function() {
            modalPrivacidade.style.display = 'none';
            document.body.style.overflow = 'auto';
        });
    }

    // Fechar modal ao clicar fora dele
    window.addEventListener('click', function(event) {
        if (event.target === modalPrivacidade) {
            modalPrivacidade.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    });

    // Fechar modal com a tecla ESC
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape' && modalPrivacidade.style.display === 'block') {
            modalPrivacidade.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    });
});