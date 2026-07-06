// Toggle sidebar (mobile)
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
}

// Fecha sidebar ao clicar fora (mobile)
document.addEventListener('click', function(e) {
    const sidebar = document.getElementById('sidebar');
    const toggle = document.querySelector('.sidebar-toggle');
    if (window.innerWidth <= 768 &&
        sidebar.classList.contains('open') &&
        !sidebar.contains(e.target) &&
        !toggle.contains(e.target)) {
        sidebar.classList.remove('open');
    }
});

// Confirmação de saída
function confirmarSaida() {
    Swal.fire({
        title: 'Sair da conta?',
        text: 'Você será desconectado.',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sim, sair',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#2563eb',
        cancelButtonColor: '#94a3b8'
    }).then(result => {
        if (result.isConfirmed) {
            document.getElementById('formLogout').submit();
        }
    });
}

function abrirLaudo(el) {
    const id = el.getAttribute('data-laudo-id');

    fetch('/dashboard/consultas/laudo/' + id)
        .then(res => res.json())
        .then(data => {
            if (data.pendente) {
                Swal.fire({
                    icon: 'info',
                    title: 'Processando Laudo',
                    text: 'O PDF ainda está sendo gerado pelo nosso provedor. Por favor, tente novamente em alguns minutos.',
                    confirmButtonColor: '#3085d6',
                    confirmButtonText: 'Entendido'
                });
            } else {
                window.open(data.url, '_blank');
            }
        })
        .catch(() => {
            Swal.fire({
                icon: 'error',
                title: 'Erro ao buscar laudo',
                text: 'Não foi possível carregar o laudo. Tente novamente em instantes.',
                confirmButtonColor: '#3085d6'
            });
        });
}