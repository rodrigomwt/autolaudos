const descricoes = {
    premium: [
        { icon: "✅", text: "Base BIN nacional / estadual" },
        { icon: "✅", text: "DENATRAN CSV" },
        { icon: "✅", text: "Roubo / Furto" },
        { icon: "✅", text: "Débitos do veículo" },
        { icon: "✅", text: "Restrições do veículo" },
        { icon: "✅", text: "Gravame" },
        { icon: "✅", text: "Recall" },
        { icon: "✅", text: "Sinistro" },
        { icon: "✅", text: "Leilão" },
        { icon: "✅", text: "Fotos ", obs: "Sujeito a constar na base nacional." },
        { icon: "✅", text: "Hodômetro ", obs: "Sujeito a constar na base nacional." },
        { icon: "✅", text: "Multas (PRF, Renainf)" },
        { icon: "✅", text: "E-decodificador de chassis" },
        { icon: "✅", text: "Tabela FIPE" },
        { icon: "✅", text: "Histórico de proprietários" },
    ],
    completa: [
        { icon: "✅", text: "Base BIN nacional / estadual" },
        { icon: "✅", text: "DENATRAN CSV" },
        { icon: "✅", text: "Roubo / Furto" },
        { icon: "✅", text: "Débitos do veículo" },
        { icon: "✅", text: "Restrições do veículo" },
        { icon: "✅", text: "Gravame" },
        { icon: "✅", text: "Recall" },
        { icon: "✅", text: "Sinistro" },
        { icon: "✅", text: "Leilão" },
        { icon: "✅", text: "Fotos ", obs: "Sujeito a constar na base nacional." },
        { icon: "✅", text: "Hodômetro ", obs: "Sujeito a constar na base nacional." },
        { icon: "✅", text: "Multas (PRF, Renainf)" },
    ],
    essencial: [
        { icon: "✅", text: "Base BIN nacional / estadual" },
        { icon: "✅", text: "DENATRAN CSV" },
        { icon: "✅", text: "Roubo / Furto" },
        { icon: "✅", text: "Débitos do veículo" },
        { icon: "✅", text: "Restrições do veículo" },
        { icon: "✅", text: "Gravame" },
        { icon: "✅", text: "Recall" },
        { icon: "✅", text: "Sinistro" },
    ]
};

function renderDescricao(id) {
    const items = descricoes[id];
    const container = document.getElementById('descricao-content');
    container.innerHTML = '';
    items.forEach(item => {
        const div = document.createElement('div');
        div.className = 'descricao-item';
        div.innerHTML = `<span class="icon">${item.icon}</span><span>${item.text}</span>`;
        container.appendChild(div);
        if (item.obs) {
            const obs = document.createElement('p');
            obs.className = 'descricao-obs';
            obs.textContent = item.obs;
            container.appendChild(obs);
        }
    });
}

function selecionarLaudo(el) {
    document.querySelectorAll('.laudo-item').forEach(i => i.classList.remove('selected'));
    el.classList.add('selected');
    renderDescricao(el.dataset.id);
}

// Renderiza a descrição do primeiro plano selecionado dinamicamente
const primeiroPlano = document.querySelector('.laudo-item.selected');
if (primeiroPlano) {
    renderDescricao(primeiroPlano.dataset.id);
}

function voltarStep1() {
    window.location.href = '/step1';
}

async function stepPayment() {
    const laudoSelecionado = document.querySelector('.laudo-item.selected');

    if (!laudoSelecionado) {
        Swal.fire({
            icon: 'warning',
            title: 'Nenhum laudo selecionado',
            text: 'Por favor, selecione um laudo antes de prosseguir.',
            confirmButtonColor: '#3085d6'
        });
        return;
    }

    const plano = laudoSelecionado.dataset.id;

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    await fetch('/selected-plan', {
        method: 'POST',
        headers,
        body: JSON.stringify({ plano })
    });

    window.location.href = '/stepPayment';
}