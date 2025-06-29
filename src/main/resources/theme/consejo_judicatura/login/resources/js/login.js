document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('kc-form-login');
    const additionalCheckbox = document.getElementById('additional-data');
    const organizationField = document.getElementById('judicatura-field');
    const organizationInput = document.getElementById('organization');

    // Mostrar/ocultar campo organización con animación suave
    if (additionalCheckbox && organizationField) {
        additionalCheckbox.addEventListener('change', function() {
            if (this.checked) {
                organizationField.style.display = 'block';
                // Pequeño delay para permitir que display: block tome efecto antes de la animación
                setTimeout(() => {
                    organizationField.classList.add('show');
                }, 10);
            } else {
                organizationField.classList.remove('show');
                // Ocultar después de la animación
                setTimeout(() => {
                    organizationField.style.display = 'none';
                }, 300);
            }

            // Limpiar organización si se desmarca
            if (!this.checked && organizationInput) {
                organizationInput.value = '';
            }
        });
    }

    // Interceptar submit del formulario
    if (form) {
        form.addEventListener('submit', function(e) {
            // Solo cambiar botón a estado de carga
            const button = this.querySelector('.login-btn');
            if (button) {
                button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Autenticando...';
                button.disabled = true;
            }
        });
    }
});