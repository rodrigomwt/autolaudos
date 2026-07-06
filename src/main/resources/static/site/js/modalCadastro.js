function abrirModalCadastro() {
    document.getElementById('modalCadastroOverlay').classList.add('active');
}

function fecharModalCadastro() {
    document.getElementById('modalCadastroOverlay').classList.remove('active');
}

function irParaLogin() {
    fecharModalCadastro();
    abrirModalLogin();
}

// Fecha ao clicar fora do card
document.getElementById('modalCadastroOverlay').addEventListener('click', function(e) {
    if (e.target === this) fecharModalCadastro();
});

// Força da senha (independente do login)
function verificarForcaCadastro(senha) {
    const fill = document.getElementById('strengthFillCad');
    const label = document.getElementById('strengthLabelCad');
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

async function realizarCadastro() {
    const fullName = document.getElementById('cadNome').value.trim();
    const federalId = document.getElementById('cpf').value.trim();
    const email = document.getElementById('cadEmail').value.trim();
    const phone = document.getElementById('cadTelefone').value.trim();
    const password = document.getElementById('cadSenha').value;
    const confirmPassword = document.getElementById('cadSenhaConfirma').value;

    if (!fullName || !email || !phone || !password || !confirmPassword) {
        Swal.fire({ icon: 'warning', title: 'Atenção', text: 'Preencha todos os campos.', confirmButtonColor: '#3085d6' });
        return;
    }
    if (password.length < 8) {
        Swal.fire({ icon: 'warning', title: 'Senha fraca', text: 'A senha deve ter no mínimo 8 caracteres.', confirmButtonColor: '#3085d6' });
        return;
    }
    if (password !== confirmPassword) {
        Swal.fire({ icon: 'error', title: 'Senhas diferentes', text: 'As senhas não coincidem.', confirmButtonColor: '#3085d6' });
        return;
    }

    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    try {
        const response = await fetch('/usuarios/registrar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token
            },
            body: JSON.stringify({ fullName, federalId, email, phone, password })
        });

        const result = await response.json();

        if (response.ok) {
            fecharModalCadastro();
            Swal.fire({
                icon: 'success',
                title: 'Cadastro realizado!',
                text: 'Sua conta foi criada com sucesso.',
                confirmButtonText: 'Fazer Login',
                confirmButtonColor: '#3085d6',
                showCancelButton: true,
                cancelButtonText: 'Fechar'
            }).then(res => {
                if (res.isConfirmed) abrirModalLogin();
            });
        } else {
            Swal.fire({ icon: 'error', title: 'Erro', text: result.message || 'Erro ao realizar cadastro.', confirmButtonColor: '#3085d6' });
        }
    } catch (e) {
        Swal.fire({ icon: 'error', title: 'Erro', text: 'Falha na comunicação com o servidor.', confirmButtonColor: '#3085d6' });
    }
}


function mascaraCPF(input) {
        let valor = input.value.replace(/\D/g, '').slice(0, 11);

        if (valor.length <= 3) {
            valor = valor;
        } else if (valor.length <= 6) {
            valor = valor.replace(/(\d{3})(\d+)/, '$1.$2');
        } else if (valor.length <= 9) {
            valor = valor.replace(/(\d{3})(\d{3})(\d+)/, '$1.$2.$3');
        } else {
            valor = valor.replace(/(\d{3})(\d{3})(\d{3})(\d+)/, '$1.$2.$3-$4');
        }

        input.value = valor;
    }


function validarCPF(cpf) {
    cpf = cpf.replace(/\D/g, '');

    if (cpf.length !== 11) return false;

    // Rejeita sequências iguais: 000.000.000-00, 111.111.111-11, etc.
    if (/^(\d)\1{10}$/.test(cpf)) return false;

    // Valida 1º dígito verificador
    let soma = 0;
    for (let i = 0;i < 9;i++) {
        soma += parseInt(cpf[i]) * (10 - i);
    }
    let resto = (soma * 10) % 11;
    if (resto === 10 || resto === 11) resto = 0;
    if (resto !== parseInt(cpf[9])) return false;

    // Valida 2º dígito verificador
    soma = 0;
    for (let i = 0;i < 10;i++) {
        soma += parseInt(cpf[i]) * (11 - i);
    }
    resto = (soma * 10) % 11;
    if (resto === 10 || resto === 11) resto = 0;
    if (resto !== parseInt(cpf[10])) return false;

    return true;
}

function verificarCPF(input) {
    if (!validarCPF(input.value)) {
        input.style.borderColor = 'red';

        Swal.fire({
            icon: 'warning',
            title: 'CPF inválido',
            text: 'Verifique se digitou corretamente o CPF.',
            confirmButtonColor: '#3085d6'
        });
        return;
    }

    input.style.borderColor = '#4caf50';
}

function mascaraTelefone(input) {
    let valor = input.value.replace(/\D/g, '').slice(0, 11);

    if (valor.length <= 2) {
        valor = valor.replace(/(\d+)/, '($1');
    } else if (valor.length <= 6) {
        valor = valor.replace(/(\d{2})(\d+)/, '($1) $2');
    } else if (valor.length <= 10) {
        // Fixo: (00) 0000-0000
        valor = valor.replace(/(\d{2})(\d{4})(\d+)/, '($1) $2-$3');
    } else {
        // Celular: (00) 00000-0000
        valor = valor.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    }

    input.value = valor;
}