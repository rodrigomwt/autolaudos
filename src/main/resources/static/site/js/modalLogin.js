// ===== Controle de telas =====
function mostrarTela(id) {
    document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
    document.getElementById(id).classList.add('active');
}

function abrirModalLogin() {
    document.getElementById('modalLoginOverlay').classList.add('active');
    mostrarTela('screenLogin');
}

function fecharModalLogin() {
    document.getElementById('modalLoginOverlay').classList.remove('active');
}

function irParaPrimeiroAcesso() { mostrarTela('screenPrimeiroAcesso'); }
function irParaEsqueciSenha() { mostrarTela('screenEsqueciSenha'); }
function voltarParaLogin() { mostrarTela('screenLogin'); }
function irParaDefinirSenha() { mostrarTela('screenDefinirSenha'); }

// ===== Toggle senha =====
function toggleSenha(inputId, icon) {
    const input = document.getElementById(inputId);
    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
    icon.classList.toggle('fa-eye', !isPassword);
    icon.classList.toggle('fa-eye-slash', isPassword);
}

// ===== Força da senha =====
function verificarForca(senha) {
    const fill = document.getElementById('strengthFill');
    const label = document.getElementById('strengthLabel');
    let forca = 0;

    if (senha.length >= 8) forca++;
    if (/[A-Z]/.test(senha)) forca++;
    if (/[0-9]/.test(senha)) forca++;
    if (/[^A-Za-z0-9]/.test(senha)) forca++;

    const niveis = [
        { pct: '0%', cor: '#eee', txt: '' },
        { pct: '25%', cor: '#e74c3c', txt: 'Fraca' },
        { pct: '50%', cor: '#e67e22', txt: 'Razoável' },
        { pct: '75%', cor: '#f1c40f', txt: 'Boa' },
        { pct: '100%', cor: '#2ecc71', txt: 'Forte' },
    ];

    fill.style.width = niveis[forca].pct;
    fill.style.background = niveis[forca].cor;
    label.textContent = niveis[forca].txt;
    label.style.color = niveis[forca].cor;
}

// ===== Ações (integrar com backend) =====
async function efetuarLogin() {
    const email = document.getElementById('loginEmail').value.trim();
    const senha = document.getElementById('loginSenha').value;

    if (!email || !senha) {
        Swal.fire({ icon: 'warning', title: 'Atenção', text: 'Preencha e-mail e senha.', confirmButtonColor: '#3085d6' });
        return;
    }

    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    try {
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', [header]: token },
            body: JSON.stringify({ email, senha })
        });

        const result = await response.json();

        if (response.ok) {
            fecharModalLogin();
            Swal.fire({
                icon: 'success',
                title: 'Bem-vindo!',
                text: 'Login realizado com sucesso.',
                confirmButtonColor: '#3085d6',
                timer: 1500,
                showConfirmButton: false
            }).then(() => window.location.href = result.redirect || '/dashboard/consultas');
        } else {
            Swal.fire({ icon: 'error', title: 'Erro', text: result.message, confirmButtonColor: '#3085d6' });
        }
    } catch (e) {
        Swal.fire({ icon: 'error', title: 'Erro', text: 'Falha na comunicação com o servidor.', confirmButtonColor: '#3085d6' });
    }
}

async function cadastrarSenha() {
    const email = document.getElementById('primeiroEmail').value.trim();

    if (!email) {
        Swal.fire({ icon: 'warning', title: 'Atenção', text: 'Informe seu email.', confirmButtonColor: '#3085d6' });
        return;
    }

    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    try {
        const response = await fetch('/primeiro-acesso', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', [header]: token },
            body: JSON.stringify({ email })
        });

        const result = await response.json();

        if (response.ok) {
            Swal.fire({
                icon: 'success',
                title: 'Enviado com Sucesso!',
                text: 'Enviamos para o seu email um link para cadastrar sua senha.',
                confirmButtonText: 'Fechar',
                confirmButtonColor: '#3085d6',
                allowOutsideClick: false
            }).then(() => {
                fecharModalLogin();
            });
        } else {
            Swal.fire({ icon: 'error', title: 'Erro', text: result.message, confirmButtonColor: '#3085d6' });
        }
    } catch (e) {
        Swal.fire({ icon: 'error', title: 'Erro', text: 'Falha na comunicação com o servidor.', confirmButtonColor: '#3085d6' });
    }
}

async function enviarRecuperacao() {
    const email = document.getElementById('recuperarEmail').value.trim();

    if (!email) {
        Swal.fire({ icon: 'warning', title: 'Atenção', text: 'Informe seu e-mail.', confirmButtonColor: '#3085d6' });
        return;
    }

    // TODO: POST /auth/recuperar-senha
}

// ===== Definir senha (ativação via token) =====
async function salvarNovaSenha() {
    const activationToken = document.getElementById('activationToken').value;
    const senha = document.getElementById('novaSenha').value;
    const confirma = document.getElementById('confirmaSenha').value;

    if (senha.length < 8) {
        Swal.fire({ icon: 'warning', title: 'Atenção', text: 'A senha deve ter no mínimo 8 caracteres.', confirmButtonColor: '#3085d6' });
        return;
    }

    if (senha !== confirma) {
        Swal.fire({ icon: 'warning', title: 'Atenção', text: 'As senhas não coincidem.', confirmButtonColor: '#3085d6' });
        return;
    }

    const csrf = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    try {
        const response = await fetch('/primeiro-acesso/definir-senha', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', [csrfHeader]: csrf },
            body: JSON.stringify({ token: activationToken, senha })
        });

        if (response.ok) {
            mostrarTela('screenSenhaDefinida');
        } else {
            const msg = await response.text();
            Swal.fire({ icon: 'error', title: 'Erro', text: msg || 'Erro ao salvar senha.', confirmButtonColor: '#3085d6' });
        }
    } catch (e) {
        Swal.fire({ icon: 'error', title: 'Erro', text: 'Falha na comunicação com o servidor.', confirmButtonColor: '#3085d6' });
    }
}

// ===== Inicialização (aguarda DOM) =====
document.addEventListener('DOMContentLoaded', () => {

    // Fecha ao clicar fora do card
    document.getElementById('modalLoginOverlay').addEventListener('click', function(e) {
        if (e.target === this) fecharModalLogin();
    });

    const params = new URLSearchParams(window.location.search);

    // Abre modal automaticamente se ?login=true
    if (params.get('login') === 'true') {
        abrirModalLogin();
    }

    // Abre modal na tela de definir senha se ?token=xxx estiver na URL
    const token = params.get('token');
    if (token) {
        fetch(`/primeiro-acesso/validar-token?token=${token}`)
            .then(res => {
                if (res.ok) {
                    document.getElementById('activationToken').value = token;
                    document.getElementById('modalLoginOverlay').classList.add('active');
                    mostrarTela('screenDefinirSenha');
                } else {
                    document.getElementById('modalLoginOverlay').classList.add('active');
                    mostrarTela('screenLogin');
                    Swal.fire({ icon: 'error', title: 'Link inválido', text: 'Este link de ativação é inválido ou expirou. Solicite um novo.', confirmButtonColor: '#3085d6' });
                }
            })
            .catch(() => {
                document.getElementById('modalLoginOverlay').classList.add('active');
                mostrarTela('screenLogin');
            });
    }
});