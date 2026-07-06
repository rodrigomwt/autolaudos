/**
   * Chamado ao clicar em "Confirmar e gerar pagamento".
   * Valida os campos e chama o serviço de geração do QR Code PIX.
   */
async function confirmarPagamento() {
    const nome = document.getElementById('nomeCompleto').value.trim();
    const cel = document.getElementById('celular').value.trim();
    const cpf = document.getElementById('cpf').value.trim();
    const email = document.getElementById('email').value.trim();

    if (!nome || !cel || !cpf || !email) {
        Swal.fire({
            icon: 'warning',
            title: 'Campos obrigatórios',
            text: 'Por favor, preencha todos os campos antes de continuar.',
            confirmButtonColor: '#3085d6'
        });
        return;
    }

    const btn = document.getElementById('btnConfirmar');
    btn.disabled = true;
    btn.textContent = 'Gerando QR Code...';

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    try {
        const response = await fetch('/pagamento/gerar-pix', {
            method: 'POST',
            headers,
            body: JSON.stringify({ fullName: nome, phone: cel, federalId: cpf, email: email })
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Erro HTTP:', response.status, errorText);
            throw new Error('Erro ao gerar pagamento');
        }

        const data = await response.json();
        console.log('Resposta da API:', data);

        btn.textContent = 'Pagamento gerado ✔';
        btn.classList.add('active');

        // ✅ Abre o modal com o QR Code retornado
        abrirModalPixStep(data);

        // ✅ Inicia o polling com o externalId retornado pela API
        iniciarPolling(data.externalId);

    } catch (err) {
        console.error(err);
        Swal.fire({
            icon: 'error',
            title: 'Erro ao gerar pagamento',
            text: 'Não foi possível gerar o QR Code. Tente novamente.',
            confirmButtonColor: '#3085d6'
        });
        btn.disabled = false;
        btn.innerHTML = 'Confirmar e gerar pagamento <span class="check-icon">✔</span>';
    }
}

/**
 * Abre o modal PIX preenchido com os dados retornados pelo /gerar-pix
 * @param {object} data - { qrCodeBase64, qrCode, externalId }
 */
function abrirModalPixStep(data) {
    document.getElementById('pixLoading').style.display = 'none';
    document.getElementById('pixExpirado').style.display = 'none';
    document.getElementById('modalPix').style.display = 'flex';

    const qrBase64 = data.qrCodeBase64?.startsWith('data')
        ? data.qrCodeBase64
        : 'data:image/png;base64,' + data.qrCodeBase64;

    document.getElementById('pixQrImg').src = qrBase64;
    document.getElementById('pixCopyInput').value = data.qrCode;
    document.getElementById('pixContent').style.display = 'block';

    iniciarContador(10 * 60);
}

/**
 * Preenche os campos do Passo 3 com dados vindos do controller.
 */
function preencherStatus({ status, veiculo, gravame }) {
    if (status) document.getElementById('statusPagamento').textContent = status;
    if (veiculo) document.getElementById('veiculoInfo').textContent = veiculo;
    if (gravame) document.getElementById('gravameInfo').textContent = gravame;
}

function voltarEscolha() {
    window.location.href = '/step2';
}

// ─── Polling ────────────────────────────────────────────────────────────────

let pollingInterval = null;
let pollingTimeout = null;

function iniciarPolling(externalId) {
    if (pollingInterval) clearInterval(pollingInterval);
    if (pollingTimeout) clearTimeout(pollingTimeout);

    pollingInterval = setInterval(async () => {
        try {
            const response = await fetch(`/status-payment/${externalId}`);
            if (!response.ok) return;

            const data = await response.json();
            const status = data.status;

            console.log('Status atual:', status);

            if (status === 'PAID') {

                fbq('track', 'Purchase');

                pararPolling();
                fecharModalPix(); // ✅ fecha o modal ao confirmar
                document.getElementById('statusPagamento').textContent = '✅ Pagamento confirmado!';
                document.getElementById('btnAguardando').innerHTML = '<span>✅ Pagamento confirmado!</span>';

                Swal.fire({
                    icon: 'success',
                    title: '✅ Pagamento confirmado!',
                    html: `Seu laudo está disponível!<br><br>
                           Faça o <strong>login</strong> para acessar e efetuar o download.`,
                    confirmButtonText: 'Fazer Login',
                    confirmButtonColor: '#3085d6'
                }).then(() => {
                    abrirModalLogin();
                });

            } else if (status === 'REJECTED' || status === 'CANCELLED') {
                pararPolling();
                document.getElementById('statusPagamento').textContent = '❌ Pagamento não aprovado';
                Swal.fire({
                    icon: 'error',
                    title: 'Pagamento não aprovado',
                    text: 'Seu pagamento foi recusado ou cancelado. Tente novamente.',
                    confirmButtonColor: '#3085d6'
                });
            }

        } catch (err) {
            console.error('Erro no polling:', err);
        }
    }, 3000);

    // ⏱️ Para o polling automaticamente após 10 minutos
    pollingTimeout = setTimeout(() => {
        pararPolling();
        document.getElementById('statusPagamento').textContent = '⏰ Tempo expirado';
        document.getElementById('btnAguardando').innerHTML = '<span>⏰ QR Code expirado</span>';

        Swal.fire({
            icon: 'warning',
            title: 'QR Code expirado',
            text: 'O tempo para pagamento expirou. Gere um novo QR Code.',
            confirmButtonColor: '#3085d6'
        });
    }, 10 * 60 * 1000);
}

function pararPolling() {
    clearInterval(pollingInterval);
    clearTimeout(pollingTimeout);
    pollingInterval = null;
    pollingTimeout = null;
}

// ─── Máscaras e Validações ───────────────────────────────────────────────────

/**
 * Aplica a máscara de CPF: 000.000.000-00
 */
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

/**
 * Valida o CPF (dígitos verificadores)
 * @param {string} cpf - CPF com ou sem máscara
 * @returns {boolean}
 */
function validarCPF(cpf) {
    cpf = cpf.replace(/\D/g, '');

    if (cpf.length !== 11) return false;
    if (/^(\d)\1{10}$/.test(cpf)) return false;

    let soma = 0;
    for (let i = 0;i < 9;i++) soma += parseInt(cpf[i]) * (10 - i);
    let resto = (soma * 10) % 11;
    if (resto === 10 || resto === 11) resto = 0;
    if (resto !== parseInt(cpf[9])) return false;

    soma = 0;
    for (let i = 0;i < 10;i++) soma += parseInt(cpf[i]) * (11 - i);
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

/**
 * Aplica a máscara de telefone: (00) 00000-0000 ou (00) 0000-0000
 */
function mascaraTelefone(input) {
    let valor = input.value.replace(/\D/g, '').slice(0, 11);

    if (valor.length <= 2) {
        valor = valor.replace(/(\d+)/, '($1');
    } else if (valor.length <= 6) {
        valor = valor.replace(/(\d{2})(\d+)/, '($1) $2');
    } else if (valor.length <= 10) {
        valor = valor.replace(/(\d{2})(\d{4})(\d+)/, '($1) $2-$3');
    } else {
        valor = valor.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    }

    input.value = valor;
}

function fecharModalPix() {
    document.getElementById('modalPix').style.display = 'none';
    pararPolling();
}

function copiarPixCode() {
    const input = document.getElementById('pixCopyInput');
    const btn = document.querySelector('.btn-copiar-pix'); // Certifique-se que o botão tem essa classe ou use o ID
    const textoOriginal = btn.innerHTML; // Salva o texto original ("Copiar")

    // Função para dar o feedback visual no botão
    const feedbackSucesso = () => {
        btn.innerHTML = 'Copiado! ✅';
        btn.style.background = '#2ecc71'; // Muda para verde

        setTimeout(() => {
            btn.innerHTML = textoOriginal;
            btn.style.background = ''; // Volta ao original (do CSS)
        }, 3000);
    };

    // 1. Tenta usar a API moderna (com HTTPS)
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(input.value)
            .then(feedbackSucesso)
            .catch(() => fallbackCopy(input, feedbackSucesso));
    } else {
        // 2. Tenta o fallback para Mobile/Safari
        fallbackCopy(input, feedbackSucesso);
    }
}

function fallbackCopy(input, callback) {
    input.select();
    input.setSelectionRange(0, 99999); // Essencial para mobile

    try {
        const successful = document.execCommand('copy');
        if (successful) callback();
    } catch (err) {
        console.error('Erro ao copiar no fallback:', err);
    }
}

function mostrarToastCopiado() {
    Swal.fire({
        icon: 'success',
        title: 'Copiado!',
        text: 'Código PIX copiado.',
        timer: 1500,
        showConfirmButton: false
    });
}

let contadorInterval = null;

function iniciarContador(segundos) {
    if (contadorInterval) clearInterval(contadorInterval);
    const el = document.getElementById('pixContador');

    contadorInterval = setInterval(() => {
        const min = Math.floor(segundos / 60);
        const sec = segundos % 60;
        el.textContent = `${String(min).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;

        if (segundos <= 0) {
            clearInterval(contadorInterval);
            pararPolling();
            document.getElementById('pixContent').style.display = 'none';
            document.getElementById('pixExpirado').style.display = 'block';
        }
        segundos--;
    }, 1000);
}

// ===== Ativa botão quando todos os campos estão preenchidos =====
function verificarCampos() {
    const nome = document.getElementById('nomeCompleto').value.trim();
    const cel = document.getElementById('celular').value.trim();
    const cpf = document.getElementById('cpf').value.trim();
    const email = document.getElementById('email').value.trim();

    const btn = document.getElementById('btnConfirmar');

    if (nome && cel && cpf && email) {
        btn.classList.add('active');
    } else {
        btn.classList.remove('active');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    ['nomeCompleto', 'celular', 'cpf', 'email'].forEach(id => {
        document.getElementById(id).addEventListener('input', verificarCampos);
    });
});