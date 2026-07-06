function iniciarLaudo() {
    const placa = document.getElementById('placaInput').value.toUpperCase();

    if (placa.length < 8) {
        Swal.fire({
            icon: 'warning',
            title: 'Placa inválida',
            text: 'Verifique se digitou corretamente a placa do veículo.',
            confirmButtonColor: '#3085d6'
        });
        return;
    }

    // Toast de sucesso
    Swal.fire({
        toast: true,
        position: 'top-end',
        icon: 'success',
        title: 'Sucesso!',
        showConfirmButton: false,
        timer: 2000,
        timerProgressBar: true
    });
	
	const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
	const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
	
    setTimeout(async () => {
		await fetch('/new-session', {
		    method: 'POST',
		    headers: {
		        'Content-Type': 'application/json',
		        [csrfHeader]: csrfToken
		    },
		    body: JSON.stringify({ placa })
		});
        window.location.href = '/step1';
    }, 2000);
}

function iniciarPesquisa(plano) {
    const placa = document.getElementById('placaInput').value.toUpperCase();
    if (placa.length < 7) {
        alert('Por favor, digite a placa do veículo primeiro!');
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return;
    }
    alert('Iniciando pesquisa do plano ' + plano + ' para a placa: ' + placa);
}

document.getElementById('placaInput').addEventListener('input', e => {
    let v = e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (v.length > 3) v = v.slice(0, 3) + '-' + v.slice(3, 7);
    e.target.value = v
});