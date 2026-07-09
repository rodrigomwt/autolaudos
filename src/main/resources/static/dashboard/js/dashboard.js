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
    window.location.href = '/dashboard/consultas/laudo/' + id;
}