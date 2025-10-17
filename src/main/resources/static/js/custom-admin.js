/* ============================================
   CUSTOM ADMIN SCRIPTS - E-COMMERCE
   ============================================ */

$(document).ready(function () {

    // ============================================
    // CONFIGURACIÓN GLOBAL
    // ============================================

    // Configurar CSRF token para AJAX (Spring Security)
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });

    // ============================================
    // DATATABLES CONFIGURACIÓN GLOBAL
    // ============================================

    $.extend(true, $.fn.dataTable.defaults, {
        language: {
            url: '//cdn.datatables.net/plug-ins/1.13.7/i18n/es-ES.json'
        },
        responsive: true,
        autoWidth: false,
        pageLength: 10,
        lengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, "Todos"]],
        dom: '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>rt<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',
    });

    // Inicializar DataTables en todas las tablas con clase .datatable
    if ($('.datatable').length > 0) {
        $('.datatable').DataTable();
    }

    // ============================================
    // SELECT2 CONFIGURACIÓN
    // ============================================

    if ($('.select2').length > 0) {
        $('.select2').select2({
            theme: 'bootstrap4',
            width: '100%',
            language: 'es'
        });
    }

    // ============================================
    // SWEETALERT2 - CONFIRMACIÓN DE ELIMINACIÓN
    // ============================================

    $(document).on('click', '.btn-delete', function (e) {
        e.preventDefault();

        const url = $(this).attr('href') || $(this).data('url');
        const itemName = $(this).data('name') || 'este elemento';

        Swal.fire({
            title: '¿Estás seguro?',
            text: `Se eliminará ${itemName}`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#ee0979',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            reverseButtons: true
        }).then((result) => {
            if (result.isConfirmed) {
                showLoading();
                window.location.href = url;
            }
        });
    });

    // ============================================
    // SWEETALERT2 - CAMBIAR ESTADO
    // ============================================

    $(document).on('click', '.btn-toggle-status', function (e) {
        e.preventDefault();

        const url = $(this).data('url');
        const currentStatus = $(this).data('status');
        const newStatus = !currentStatus;
        const statusText = newStatus ? 'activar' : 'desactivar';

        Swal.fire({
            title: '¿Estás seguro?',
            text: `Se ${statusText}á este elemento`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#667eea',
            cancelButtonColor: '#6c757d',
            confirmButtonText: `Sí, ${statusText}`,
            cancelButtonText: 'Cancelar',
            reverseButtons: true
        }).then((result) => {
            if (result.isConfirmed) {
                showLoading();
                window.location.href = url;
            }
        });
    });

    // ============================================
    // ALERTAS AUTOMÁTICAS
    // ============================================

    // Mostrar alertas de éxito/error si existen
    if ($('.alert-success').length > 0) {
        Swal.fire({
            icon: 'success',
            title: '¡Éxito!',
            text: $('.alert-success').text().trim(),
            timer: 3000,
            timerProgressBar: true,
            showConfirmButton: false
        });
    }

    if ($('.alert-danger').length > 0) {
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: $('.alert-danger').text().trim(),
            confirmButtonColor: '#ee0979'
        });
    }

    // ============================================
    // VALIDACIÓN EN TIEMPO REAL
    // ============================================

    // Validar campos requeridos
    $('input[required], textarea[required], select[required]').on('blur', function () {
        validateField($(this));
    });

    // Validar email
    $('input[type="email"]').on('blur', function () {
        const email = $(this).val();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (email && !emailRegex.test(email)) {
            showFieldError($(this), 'Ingrese un email válido');
        } else {
            removeFieldError($(this));
        }
    });

    // Validar números
    $('input[type="number"]').on('blur', function () {
        const min = $(this).attr('min');
        const max = $(this).attr('max');
        const value = parseFloat($(this).val());

        if (min && value < parseFloat(min)) {
            showFieldError($(this), `El valor mínimo es ${min}`);
        } else if (max && value > parseFloat(max)) {
            showFieldError($(this), `El valor máximo es ${max}`);
        } else {
            removeFieldError($(this));
        }
    });

    // Validar formulario antes de enviar
    $('form').on('submit', function (e) {
        let isValid = true;

        $(this).find('input[required], textarea[required], select[required]').each(function () {
            if (!validateField($(this))) {
                isValid = false;
            }
        });

        if (!isValid) {
            e.preventDefault();
            Swal.fire({
                icon: 'error',
                title: 'Formulario incompleto',
                text: 'Por favor complete todos los campos requeridos',
                confirmButtonColor: '#ee0979'
            });
        } else {
            showLoading();
        }
    });

    // ============================================
    // PREVIEW DE IMÁGENES
    // ============================================

    $('input[type="file"][accept*="image"]').on('change', function (e) {
        const file = e.target.files[0];
        const preview = $(this).data('preview');

        if (file && preview) {
            const reader = new FileReader();

            reader.onload = function (e) {
                $(`#${preview}`).attr('src', e.target.result).show();
            };

            reader.readAsDataURL(file);
        }
    });

    // ============================================
    // TOOLTIPS Y POPOVERS
    // ============================================

    $('[data-toggle="tooltip"]').tooltip();
    $('[data-toggle="popover"]').popover();

    // ============================================
    // BÚSQUEDA DE CLIENTES POR DOCUMENTO
    // ============================================

    $('#buscarCliente').on('click', function () {
        const documento = $('#clienteDocumento').val();

        if (!documento) {
            Swal.fire({
                icon: 'warning',
                title: 'Atención',
                text: 'Ingrese un número de documento',
                confirmButtonColor: '#f7971e'
            });
            return;
        }

        showLoading();

        $.ajax({
            url: '/admin/clientes/buscar',
            method: 'GET',
            data: { documento: documento },
            success: function (response) {
                hideLoading();

                if (response.success) {
                    // Llenar campos con datos del cliente
                    $('#clienteId').val(response.cliente.id);
                    $('#clienteNombre').val(response.cliente.nombre).prop('readonly', true);

                    Swal.fire({
                        icon: 'success',
                        title: 'Cliente encontrado',
                        text: response.cliente.nombre,
                        timer: 2000,
                        showConfirmButton: false
                    });
                } else {
                    Swal.fire({
                        icon: 'info',
                        title: 'Cliente no encontrado',
                        text: 'Se registrará como nuevo cliente',
                        confirmButtonColor: '#667eea'
                    });
                }
            },
            error: function () {
                hideLoading();
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'No se pudo consultar el documento',
                    confirmButtonColor: '#ee0979'
                });
            }
        });
    });

    // ============================================
    // CÁLCULO AUTOMÁTICO EN POS
    // ============================================

    $(document).on('input', '.producto-cantidad, .producto-precio', function () {
        calcularTotalProducto($(this).closest('.producto-item'));
        calcularTotalVenta();
    });

    // ============================================
    // FUNCIONES AUXILIARES
    // ============================================

    function validateField($field) {
        const value = $field.val();

        if ($field.prop('required') && !value) {
            showFieldError($field, 'Este campo es requerido');
            return false;
        }

        removeFieldError($field);
        return true;
    }

    function showFieldError($field, message) {
        $field.addClass('is-invalid');

        let $feedback = $field.siblings('.invalid-feedback');
        if ($feedback.length === 0) {
            $feedback = $('<div class="invalid-feedback"></div>');
            $field.after($feedback);
        }

        $feedback.text(message);
    }

    function removeFieldError($field) {
        $field.removeClass('is-invalid');
        $field.siblings('.invalid-feedback').remove();
    }

    function showLoading() {
        Swal.fire({
            title: 'Procesando...',
            allowOutsideClick: false,
            allowEscapeKey: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });
    }

    function hideLoading() {
        Swal.close();
    }

    function calcularTotalProducto($item) {
        const cantidad = parseFloat($item.find('.producto-cantidad').val()) || 0;
        const precio = parseFloat($item.find('.producto-precio').val()) || 0;
        const subtotal = cantidad * precio;

        $item.find('.producto-subtotal').text('S/ ' + subtotal.toFixed(2));
    }

    function calcularTotalVenta() {
        let total = 0;

        $('.producto-item').each(function () {
            const cantidad = parseFloat($(this).find('.producto-cantidad').val()) || 0;
            const precio = parseFloat($(this).find('.producto-precio').val()) || 0;
            total += cantidad * precio;
        });

        $('#totalVenta').text('S/ ' + total.toFixed(2));
        $('#montoTotal').val(total.toFixed(2));
    }

    // ============================================
    // ANIMACIONES DE ENTRADA
    // ============================================

    $('.card').addClass('fade-in');

    // ============================================
    // AUTO-CLOSE ALERTS
    // ============================================

    setTimeout(function () {
        $('.alert').fadeOut('slow');
    }, 5000);

});

// ============================================
// FUNCIONES GLOBALES
// ============================================

// Formatear moneda
function formatCurrency(amount) {
    return 'S/ ' + parseFloat(amount).toFixed(2);
}

// Formatear fecha
function formatDate(date) {
    const d = new Date(date);
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}/${month}/${year}`;
}

// Copiar al portapapeles
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(function () {
        Swal.fire({
            icon: 'success',
            title: 'Copiado',
            text: 'Texto copiado al portapapeles',
            timer: 1500,
            showConfirmButton: false
        });
    });
}