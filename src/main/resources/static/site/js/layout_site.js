function toggleMenu() {
    const menu = document.getElementById('mobileMenu');
    menu.classList.toggle('open');
}

function fecharMenu() {
    const menu = document.getElementById('mobileMenu');
    menu.classList.remove('open');
}

// Fecha o menu ao clicar fora
document.addEventListener('click', function(e) {
    const menu = document.getElementById('mobileMenu');
    const toggle = document.querySelector('.menu-toggle');
    if (menu && toggle && !menu.contains(e.target) && !toggle.contains(e.target)) {
        menu.classList.remove('open');
    }
});