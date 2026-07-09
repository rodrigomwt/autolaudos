// ---- CONTROLE DE TELA ----
function mostrarLoading() {
    document.getElementById('laudoLoading').style.display = 'flex';
    document.getElementById('laudoContent').style.display = 'none';
    document.getElementById('laudoErro').style.display = 'none';
}

function esconderLoading() {
    document.getElementById('laudoLoading').style.display = 'none';
}

function mostrarErro(msg) {
    esconderLoading();
    document.getElementById('laudoErroMsg').textContent = msg;
    document.getElementById('laudoErro').style.display = 'block';
    document.getElementById('laudoContent').style.display = 'none';
}

function mostrarConteudo() {
    esconderLoading();
    document.getElementById('laudoErro').style.display = 'none';
    document.getElementById('laudoContent').style.display = 'block';
}

// ---- POLLING ----
var tentativas = 0;
var MAX_TENTATIVAS = 20;

async function carregarDadosDoLaudo() {
    try {
        var response = await fetch('/dashboard/consultas/laudo/' + LAUDO_ID + '/dados');
        var data = await response.json();

        if (data.pendente) {
            tentativas++;
            if (tentativas >= MAX_TENTATIVAS) {
                mostrarErro('A consulta está demorando mais que o esperado. Por favor, tente recarregar a página em instantes.');
                return;
            }
            setTimeout(carregarDadosDoLaudo, 5000);
            return;
        }

        if (data.erro || !data.laudo) {
            mostrarErro(data.mensagem || 'Não foi possível recuperar os dados do laudo.');
            return;
        }

        var laudoObj = typeof data.laudo === 'string' ? JSON.parse(data.laudo) : data.laudo;
        renderizarLaudo(laudoObj);
        mostrarConteudo();

    } catch (error) {
        console.error('Erro no fetch:', error);
        mostrarErro('Falha na comunicação com o servidor.');
    }
}

// ---- RENDERIZAÇÃO ----
function renderizarLaudo(parsed) {
    var body = parsed.body && parsed.body.data ? parsed.body.data : {};

    // ---- HELPERS ----
    function txt(id, val) {
        var el = document.getElementById(id);
        if (el) el.textContent = val || '—';
    }
    function fmtBRL(val) {
        var n = parseFloat(val);
        if (isNaN(n)) return '—';
        return 'R$ ' + n.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
    function nada(val) {
        if (!val || val === 'NADA CONSTA' || val === 'NAO' || val === '') return true;
        return false;
    }

    // ---- HERO ----
    var db = body.dadosBasicosDoVeiculo || {};
    txt('heroPlaca', body.placa || db.placa);
    txt('heroModelo', db.marca + ' ' + db.descricao);
    txt('heroAnoFab', db.anoFabricacao || body.anoFabricacao);
    txt('heroAnoMod', db.anoModelo || body.anoModelo);
    txt('heroCombustivel', db.combustivel || body.combustivel);
    txt('heroUf', body.uf);

    var fipeInfo = db.informacoesFipe && db.informacoesFipe[0];
    if (fipeInfo) {
        var fipeVal = parseFloat(fipeInfo.valorAtual);
        document.getElementById('heroFipe').textContent = fmtBRL(fipeVal);
        document.getElementById('heroFipeSub').textContent = fipeInfo.versao + ' · ' + fipeInfo.combustivel;
    }

    // ---- RISCO ----
    var risco = body.analiseRisco || {};
    var indice = parseInt(risco.indiceRisco) || 1;
    var riscoEl = document.getElementById('riscoCircle');
    txt('riscoCircle', indice);
    txt('riscoParecer', risco.parecer);
    if (indice <= 2) {
        riscoEl.className = 'risco-score-circle baixo';
        txt('riscoTitulo', 'Baixo Risco');
    } else if (indice <= 4) {
        riscoEl.className = 'risco-score-circle medio';
        txt('riscoTitulo', 'Risco Médio');
    } else {
        riscoEl.className = 'risco-score-circle alto';
        txt('riscoTitulo', 'Alto Risco');
    }

    // ---- DADOS BÁSICOS ----
    txt('dbMarca', db.marca);
    txt('dbModelo', db.descricao);
    txt('dbChassi', db.chassi || body.chassi);
    txt('dbRenavam', body.renavam);
    txt('dbCilindradas', db.cilindradas ? db.cilindradas + ' cc' : '—');
    txt('dbCodigoFipe', db.codigoFipe);

    // ---- SITUAÇÃO ----
    var bn = body.baseNacional || {};
    var be = body.baseEstadual || {};
    var statusGrid = document.getElementById('statusGrid');
    var statusItems = [
        { label: 'Situação', val: bn.situacaoVeiculo || be.situacaoVeiculo, ok: true },
        { label: 'Roubo/Furto', val: bn.ocorrencia || be.restricaoRouboFurto, ok: nada(be.restricaoRouboFurto) },
        { label: 'Restrição Judicial', val: be.restricaoJudicial, ok: nada(be.restricaoJudicial) },
        { label: 'Restrição Renajud', val: be.restricaoRenajud, ok: nada(be.restricaoRenajud) },
        { label: 'Restrição Financeira', val: be.restricaoFinanceira, ok: nada(be.restricaoFinanceira) },
        { label: 'Restrição Adm.', val: be.restricaoAdminisrativa, ok: nada(be.restricaoAdminisrativa) },
        { label: 'Restrição Tributária', val: be.restricaoTributaria, ok: nada(be.restricaoTributaria) },
        { label: 'Restrição Guincho', val: be.restricaoGuincho, ok: nada(be.restricaoGuincho) },
        { label: 'Indício Sinistro', val: (body.indicioSinistro || {}).descricao, ok: true },
        { label: 'Registro Locadora', val: (body.registroEmLocadora || {}).registroEmLocadora ? 'SIM' : 'NÃO', ok: !(body.registroEmLocadora || {}).registroEmLocadora },
    ];
    statusItems.forEach(function(s) {
        var cls = s.ok ? 'ok' : 'alert';
        var icon = s.ok ? 'fa-check-circle' : 'fa-exclamation-circle';
        statusGrid.innerHTML += '<div class="status-item ' + cls + '"><i class="fas ' + icon + '"></i>' + s.label + ': <b>' + (s.val || '—') + '</b></div>';
    });

    // ---- DÉBITOS ----
    var debitosContainer = document.getElementById('debitosContainer');
    var debitos = [
        { label: 'IPVA', existe: be.existeDebitoIpva, valor: be.debitoIpva },
        { label: 'Licenciamento', existe: be.existeDebitoLicenciamento, valor: be.debitoLicenciamento },
        { label: 'Multas', existe: be.existeDebitoMulta, valor: be.debitoMultas },
        { label: 'DPVAT', existe: be.existeDebitoDpvat, valor: be.debitoDpvat },
    ];
    debitos.forEach(function(d) {
        var temDebito = d.existe && d.existe.indexOf('EXISTE DEBITO') === 0 && d.existe.indexOf('NAO') === -1;
        var cls = temDebito ? 'alert' : 'ok';
        var icon = temDebito ? 'fa-exclamation-circle' : 'fa-check-circle';
        var valorFmt = d.valor ? 'R$ ' + d.valor : 'R$ 0,00';
        debitosContainer.innerHTML +=
            '<div class="debito-row ' + cls + '">' +
            '<div class="debito-label"><i class="fas ' + icon + '"></i>' + d.label + '</div>' +
            '<div class="debito-valor">' + valorFmt + '</div>' +
            '</div>';
    });

    // ---- BASE ESTADUAL ----
    txt('bePronome', be.pronome);
    txt('beMunicipio', be.municipio);
    txt('beUf', be.uf);
    txt('beCategoria', be.categoria);
    txt('beEspecie', be.especie);
    txt('beSituacao', be.situacaoVeiculo);
    txt('beRestFinanceira', be.restricaoFinanceira);
    txt('beRestJudicial', be.restricaoJudicial);
    txt('beRestRoubo', be.restricaoRouboFurto);
    txt('beRestRenajud', be.restricaoRenajud);
    txt('beComVenda', be.comunicacaoVenda);
    txt('beEmissaoCrv', be.dataEmissaoCrv);

    // ---- LEILÃO ----
    var leilao = body.leilao || {};
    var score = leilao.score || {};
    var leilaoScores = document.getElementById('leilaoScores');
    [
        { label: 'Aceitação', val: score.aceitacao ? score.aceitacao + '%' : '—' },
        { label: 'Score', val: score.score || '—' },
        { label: '% Sobre Ref.', val: score.percentualSobreRef ? score.percentualSobreRef + '%' : '—' },
        { label: 'Vistoria Especial', val: score.exigenciaVistoriaEspecial ? score.exigenciaVistoriaEspecial + '%' : '—' },
    ].forEach(function(s) {
        leilaoScores.innerHTML += '<div class="leilao-score-box"><div class="ls-label">' + s.label + '</div><div class="ls-value">' + s.val + '</div></div>';
    });
    document.getElementById('leilaoDescricao').textContent = leilao.descricao || '';
    var registros = leilao.registros || [];
    if (registros.length > 0) {
        var tbl = '<table class="leilao-table"><thead><tr><th>Data</th><th>Placa</th><th>Comitente</th><th>Lote</th><th>Cor</th><th>Ano Fab/Mod</th></tr></thead><tbody>';
        registros.forEach(function(r) {
            tbl += '<tr><td>' + (r.dataLeilao || '—') + '</td><td>' + (r.placa || '—') + '</td><td>' + (r.comitente || '—') + '</td><td>' + (r.lote || '—') + '</td><td>' + (r.cor || '—') + '</td><td>' + (r.anoFabricacao || '—') + '/' + (r.anoModelo || '—') + '</td></tr>';
        });
        tbl += '</tbody></table>';
        document.getElementById('leilaoTabelaWrapper').innerHTML = tbl;
    } else {
        document.getElementById('leilaoTabelaWrapper').innerHTML = '<p style="font-size:13px;color:#27ae60;"><i class="fas fa-check-circle"></i> Nenhum registro de leilão encontrado.</p>';
    }

    // ---- GRAVAMES ----
    var gravames = body.gravame || [];
    var gravamesContainer = document.getElementById('gravamesContainer');
    if (gravames.length === 0) {
        gravamesContainer.innerHTML = '<p style="font-size:13px;color:#27ae60;"><i class="fas fa-check-circle"></i> Nenhum gravame encontrado.</p>';
    } else {
        gravames.forEach(function(g) {
            var isBaixado = g.situacao && g.situacao.indexOf('BAIXADO') !== -1;
            var badgeCls = isBaixado ? 'baixado' : 'ativo';
            var badgeTxt = isBaixado ? 'Baixado' : 'Ativo';
            gravamesContainer.innerHTML +=
                '<div class="gravame-item">' +
                '<div class="gravame-header"><span class="gravame-agente">' + (g.agente || '—') + '</span><span class="gravame-badge ' + badgeCls + '">' + badgeTxt + '</span></div>' +
                '<div class="gravame-meta">' +
                '<span>Inclusão: <b>' + (g.dataInclusao || '—') + '</b></span>' +
                '<span>Contrato: <b>' + (g.numero || '—') + '</b></span>' +
                '<span>UF Placa: <b>' + (g.ufPlaca || '—') + '</b></span>' +
                '<span>Obs: <b>' + (g.observacoes || '—') + '</b></span>' +
                '</div></div>';
        });
    }

    // ---- GRÁFICO FIPE ----
    var fipeHist = fipeInfo && fipeInfo.historicoPreco ? fipeInfo.historicoPreco : [];
    var meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
    var labels = fipeHist.map(function(h) { return meses[parseInt(h.mes) - 1] + '/' + String(h.ano).slice(2); });
    var valores = fipeHist.map(function(h) { return parseFloat(h.valor); });
    var ctx = document.getElementById('fipeChart').getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Valor FIPE (R$)',
                data: valores,
                borderColor: '#1e90ff',
                backgroundColor: 'rgba(30,144,255,0.08)',
                borderWidth: 2,
                pointRadius: 2,
                pointHoverRadius: 5,
                fill: true,
                tension: 0.3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: { callbacks: { label: function(c) { return 'R$ ' + c.parsed.y.toLocaleString('pt-BR'); } } }
            },
            scales: {
                x: { ticks: { font: { size: 9 }, maxRotation: 45, color: '#888' }, grid: { display: false } },
                y: { ticks: { font: { size: 10 }, color: '#888', callback: function(v) { return 'R$ ' + (v / 1000).toFixed(0) + 'k'; } }, grid: { color: '#f0f2f5' } }
            }
        }
    });

    // ---- PROPRIETÁRIOS ----
    var props = body.historicoProprietarios || [];
    var propContainer = document.getElementById('proprietariosContainer');
    if (props.length === 0) {
        propContainer.innerHTML = '<p style="font-size:13px;color:#7a8fa6;">Nenhum registro encontrado.</p>';
    } else {
        var vistos = new Set();
        props.forEach(function(p) {
            var key = p.proprietario + p.uf;
            if (vistos.has(key)) return;
            vistos.add(key);
            propContainer.innerHTML +=
                '<div class="prop-item">' +
                '<div class="prop-icon"><i class="fas fa-user"></i></div>' +
                '<div class="prop-info"><div class="prop-name">' + (p.proprietario || '—') + '</div>' +
                '<div class="prop-meta">UF: ' + (p.uf || '—') + ' · Placa: ' + (p.placa || '—') + ' · Exercício: ' + (p.anoExercicio || '—') + '</div></div></div>';
        });
    }

    // ---- KM ----
    var kms = body.historicoKm || [];
    var kmContainer = document.getElementById('kmContainer');
    if (kms.length === 0) {
        kmContainer.innerHTML = '<p style="font-size:13px;color:#7a8fa6;">Nenhum registro encontrado.</p>';
    } else {
        kms.forEach(function(k) {
            kmContainer.innerHTML +=
                '<div class="prop-item">' +
                '<div class="prop-icon"><i class="fas fa-tachometer-alt"></i></div>' +
                '<div class="prop-info"><div class="prop-name">' + parseInt(k.km).toLocaleString('pt-BR') + ' km</div>' +
                '<div class="prop-meta">' + (k.dataInclusao || '—') + '</div></div></div>';
        });
    }

    // ---- REVISÃO ----
    var revisaoData = body.revisao && body.revisao.veiculosFipe && body.revisao.veiculosFipe[0];
    var revisaoRegistros = revisaoData ? revisaoData.registros : [];
    var tabsEl = document.getElementById('revisaoTabs');
    var contentEl = document.getElementById('revisaoContent');
    revisaoRegistros.forEach(function(r, i) {
        var tabId = 'rev-tab-' + i;
        var btn = document.createElement('button');
        btn.className = 'revisao-tab-btn' + (i === 0 ? ' active' : '');
        btn.textContent = r.kilometragem.toLocaleString('pt-BR') + ' km';
        btn.setAttribute('data-tab', tabId);
        btn.onclick = function() {
            document.querySelectorAll('.revisao-tab-btn').forEach(function(b) { b.classList.remove('active'); });
            document.querySelectorAll('.revisao-body').forEach(function(b) { b.classList.remove('active'); });
            this.classList.add('active');
            document.getElementById(this.getAttribute('data-tab')).classList.add('active');
        };
        tabsEl.appendChild(btn);

        var pecasHtml = r.pecasTrocadas.map(function(p) {
            return '<span class="revisao-peca-tag"><i class="fas fa-wrench"></i> ' + p.descricao + ' (x' + p.quantidade + ')</span>';
        }).join('');
        var inspHtml = r.inspecoes.map(function(ins) {
            return '<span class="revisao-insp-tag"><i class="fas fa-search"></i> ' + ins + '</span>';
        }).join('');

        var div = document.createElement('div');
        div.className = 'revisao-body' + (i === 0 ? ' active' : '');
        div.id = tabId;
        div.innerHTML =
            '<div class="revisao-info-row">' +
            '<div class="revisao-info-item"><div class="revisao-info-label">Quilometragem</div><div class="revisao-info-value">' + r.kilometragem.toLocaleString('pt-BR') + ' km</div></div>' +
            '<div class="revisao-info-item"><div class="revisao-info-label">Prazo</div><div class="revisao-info-value">' + r.meses + ' meses</div></div>' +
            '<div class="revisao-info-item"><div class="revisao-info-label">Custo Total</div><div class="revisao-info-value">' + fmtBRL(r.precoTotal) + '</div></div>' +
            '<div class="revisao-info-item"><div class="revisao-info-label">Parcelas (4x)</div><div class="revisao-info-value">' + fmtBRL(r.precoParcela) + '</div></div>' +
            '</div>' +
            '<div class="revisao-sub-title"><i class="fas fa-box-open"></i> Peças Trocadas</div>' +
            '<div class="revisao-pecas-list">' + pecasHtml + '</div>' +
            '<div class="revisao-sub-title" style="margin-top:14px;"><i class="fas fa-clipboard-list"></i> Inspeções</div>' +
            '<div class="revisao-insp-list">' + inspHtml + '</div>';
        contentEl.appendChild(div);
    });

    // ---- ACESSÓRIOS ----
    var acessoriosData = body.acessorios && body.acessorios.veiculosFipe && body.acessorios.veiculosFipe[0];
    var acessoriosGrid = document.getElementById('acessoriosGrid');
    if (acessoriosData && acessoriosData.registros) {
        var unicos = [];
        var vistos2 = new Set();
        acessoriosData.registros.forEach(function(a) {
            if (!vistos2.has(a.descricao)) { vistos2.add(a.descricao); unicos.push(a); }
        });
        unicos.forEach(function(a) {
            acessoriosGrid.innerHTML += '<div class="acessorio-item"><i class="fas fa-check-circle"></i>' + a.descricao + '</div>';
        });
    }
}

// ---- INICIALIZAÇÃO ----
document.addEventListener('DOMContentLoaded', function() {
    if (typeof LAUDO_ID !== 'undefined' && LAUDO_ID) {
        mostrarLoading();
        carregarDadosDoLaudo();
    } else {
        mostrarErro('ID do laudo não encontrado.');
    }
});