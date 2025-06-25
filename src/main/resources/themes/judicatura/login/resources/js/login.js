document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('kc-form-login');
    const additionalCheckbox = document.getElementById('additional-data');
    const organizationField = document.getElementById('judicatura-field');
    const organizationInput = document.getElementById('organization');
    
    // Mostrar/ocultar campo organización
    if (additionalCheckbox && organizationField) {
        additionalCheckbox.addEventListener('change', function() {
            organizationField.style.display = this.checked ? 'block' : 'none';
            
            // Limpiar organización si se desmarca
            if (!this.checked && organizationInput) {
                organizationInput.value = '';
            }
        });
    }
    
    // Interceptar submit del formulario
    if (form) {
        form.addEventListener('submit', function(e) {
            // NO concatenar - dejar los campos separados
            // Tu authenticator los procesará individualmente
            
            // Solo cambiar botón a estado de carga
            const button = this.querySelector('.login-btn');
            if (button) {
                button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Autenticando...';
                button.disabled = true;
            }
        });
    }
});