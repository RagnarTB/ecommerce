/** 
 * ============================================
 * ADMIN GAMER - JavaScript Principal
 * ============================================
 * Scripts para el panel administrativo
 */

// ============================================
// CONFIGURACIÓN GLOBAL
// ============================================
const AdminGamer = {
    // URL base de la API
    apiUrl: '/admin',

    // Configuración de toasts
    toastConfig: {
        position: 'top-end',
        timer: 3000,
        timerProgressBar: true,
        showConfirmButton: false
    },

    // Inicializar al cargar la página
    init: function () {
        this.initDataTables();
        this.initSelect2();
        this.initDatePickers();
        this.initTooltips();
        this.initConfirmButtons();
        this.initImagePreview();
        this.initFormValidation();
    },

    // ============================================
    // DATATABLES
    // ============================================
    initDataTables: function () {
        if ($.fn.DataTable) {
            $('.data-table').DataTable({
                language: { url: '//cdn.datatables.net/plug-ins/1.13.7/i18n/es-ES.json' },
                responsive: true,
                autoWidth: false,
                pageLength: 25,
                dom: "<'row'<'col-sm-12 col-md-6'l><'col-sm-12 col-md-6'f>>" +
                    "<'row'<'col-sm-12'tr>>" +
                    "<'row'<'col-sm-12 col-md-5'i><'col-sm-12 col-md-7'p>>",
                order: [[0, 'desc']]
            });
        }
    },

    // ============================================
    // SELECT2
    // ============================================
    initSelect2: function () {
        if ($.fn.select2) {
            $('.select2').select2({
                theme: 'bootstrap4',
                width: '100%'
            });
        }
    },

    // ============================================
    // DATE PICKERS
    // ============================================
    initDatePickers: function () {
        if ($.fn.datepicker) {
            $('.datepicker').datepicker({
                format: 'dd/mm/yyyy',
                autoclose: true,
                todayHighlight: true,
                language: 'es'
            });
        }
    },

    // ============================================
    // TOOLTIPS
    // ============================================
    initTooltips: function () {
        $('[data-toggle="tooltip"]').tooltip();
    },

    // ============================================
    // CONFIRMACIONES
    // ============================================
    initConfirmButtons: function () {
        $(document).on('click', '.btn-confirm', function (e) {
            e.preventDefault();
            const url = $(this).attr('href');
            const mensaje = $(this).data('mensaje') || '¿Estás seguro de realizar esta acción?';

            Swal.fire({
                title: '¿Estás seguro?',
                text: mensaje,
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#00ff00',
                cancelButtonColor: '#ff0066',
                confirmButtonText: 'Sí, continuar',
                cancelButtonText: 'Cancelar',
                background: '#1a1a2e',
                color: '#fff'
            }).then((result) => {
                if (result.isConfirmed) {
                    window.location.href = url;
                }
            });
        });
    },

    // ============================================
    // PREVIEW DE IMÁGENES
    // ============================================
    initImagePreview: function () {
        $(document).on('change', 'input[type="file"][accept="image/*"]', function (e) {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    const preview = $(this).closest('.form-group').find('.image-preview');
                    if (preview.length) {
                        preview.html(`<img src="${e.target.result}" class="img-thumbnail" style="max-height: 200px;">`);
                    } else {
                        $(this).after(`<div class="image-preview mt-2"><img src="${e.target.result}" class="img-thumbnail" style="max-height: 200px;"></div>`);
                    }
                }.bind(this);
                reader.readAsDataURL(file);
            }
        });
    },

    // ============================================
    // VALIDACIÓN DE FORMULARIOS
    // ============================================
    initFormValidation: function () {
        $('.needs-validation').on('submit', function (e) {
            if (!this.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
            }
            $(this).addClass('was-validated');
        });
    },

    // ============================================
    // TOASTS
    // ============================================
    showToast: function (icon, title, message) {
        const Toast = Swal.mixin(this.toastConfig);
        Toast.fire({
            icon: icon,
            title: title,
            text: message
        });
    },

    // ============================================
    // LOADING OVERLAY
    // ============================================
    showLoading: function (mensaje = 'Cargando...') {
        Swal.fire({
            title: mensaje,
            allowOutsideClick: false,
            allowEscapeKey: false,
            didOpen: () => {
                Swal.showLoading();
            },
            background: '#1a1a2e',
            color: '#fff'
        });
    },

    hideLoading: function () {
        Swal.close();
    },

    // ============================================
    // FORMATEO DE NÚMEROS
    // ============================================
    formatMoney: function (amount) {
        return new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(amount);
    },

    formatNumber: function (number) {
        return new Intl.NumberFormat('es-PE').format(number);
    }
};

// ============================================
// POS - PUNTO DE VENTA
// ============================================
const POS = {
    carrito: [],
    cliente: null,

    init: function () {
        this.initProductSearch();
        this.initClientSearch();
        this.initPaymentMethods();
    },

    // Buscar productos
    initProductSearch: function () {
        $('#producto-search').on('input', function () {
            const search = $(this).val().toLowerCase();
            $('.producto-card').each(function () {
                const nombre = $(this).find('.producto-nombre').text().toLowerCase();
                $(this).toggle(nombre.includes(search));
            });
        });
    },

    // Buscar cliente por DNI/RUC
    initClientSearch: function () {
        $('#btn-buscar-cliente').on('click', function () {
            const documento = $('#cliente-documento').val();
            if (!documento) {
                AdminGamer.showToast('error', 'Error', 'Ingrese un número de documento');
                return;
            }

            AdminGamer.showLoading('Buscando cliente...');

            $.get(`/admin/clientes/buscar?documento=${documento}`)
                .done(function (response) {
                    AdminGamer.hideLoading();
                    const data = JSON.parse(response);
                    if (data.success) {
                        POS.cliente = data.cliente;
                        $('#cliente-nombre').text(data.cliente.nombre);
                        AdminGamer.showToast('success', 'Cliente encontrado', '');
                    } else {
                        AdminGamer.showToast('error', 'Error', data.error);
                    }
                })
                .fail(function () {
                    AdminGamer.hideLoading();
                    AdminGamer.showToast('error', 'Error', 'Error al buscar cliente');
                });
        });
    },

    // Agregar producto al carrito
    agregarProducto: function (id, nombre, precio) {
        const existe = this.carrito.find(item => item.id === id);

        if (existe) {
            existe.cantidad++;
        } else {
            this.carrito.push({ id, nombre, precio, cantidad: 1 });
        }

        this.actualizarCarrito();
        AdminGamer.showToast('success', 'Producto agregado', '');
    },

    // Actualizar vista del carrito
    actualizarCarrito: function () {
        const tbody = $('#carrito-tbody');
        tbody.empty();

        let subtotal = 0;

        this.carrito.forEach((item, index) => {
            const total = item.precio * item.cantidad;
            subtotal += total;

            tbody.append(`
        <tr>
          <td>${item.nombre}</td>
          <td>
            <input type="number" class="form-control form-control-sm" value="${item.cantidad}" min="1"
              onchange="POS.cambiarCantidad(${index}, this.value)">
          </td>
          <td>${AdminGamer.formatMoney(item.precio)}</td>
          <td>${AdminGamer.formatMoney(total)}</td>
          <td>
            <button class="btn btn-danger btn-sm" onclick="POS.eliminarItem(${index})">
              <i class="fas fa-trash"></i>
            </button>
          </td>
        </tr>
      `);
        });

        $('#subtotal').text(AdminGamer.formatMoney(subtotal));
        $('#total').text(AdminGamer.formatMoney(subtotal));
    },

    // Cambiar cantidad de un item
    cambiarCantidad: function (index, cantidad) {
        this.carrito[index].cantidad = parseInt(cantidad);
        this.actualizarCarrito();
    },

    // Eliminar item del carrito
    eliminarItem: function (index) {
        this.carrito.splice(index, 1);
        this.actualizarCarrito();
    },

    // Procesar venta
    procesarVenta: function () {
        if (this.carrito.length === 0) {
            AdminGamer.showToast('error', 'Error', 'El carrito está vacío');
            return;
        }

        if (!this.cliente) {
            AdminGamer.showToast('error', 'Error', 'Debe seleccionar un cliente');
            return;
        }

        // Aquí iría la lógica de pago y guardado
        AdminGamer.showLoading('Procesando venta...');

        setTimeout(() => {
            AdminGamer.hideLoading();
            Swal.fire({
                icon: 'success',
                title: 'Venta registrada',
                text: 'La venta se registró correctamente',
                confirmButtonColor: '#00ff00',
                background: '#1a1a2e',
                color: '#fff'
            }).then(() => {
                this.limpiarCarrito();
            });
        }, 1500);
    },

    // Limpiar carrito
    limpiarCarrito: function () {
        this.carrito = [];
        this.cliente = null;
        $('#cliente-documento').val('');
        $('#cliente-nombre').text('');
        this.actualizarCarrito();
    },

    // Métodos de pago
    initPaymentMethods: function () {
        $('input[name="tipo-pago"]').on('change', function () {
            const tipo = $(this).val();
            $('#cuotas-section').toggle(tipo === 'CREDITO');
        });
    }
};

// ============================================
// GESTIÓN DE PRODUCTOS
// ============================================
const Productos = {
    // Eliminar producto
    eliminar: function (id) {
        Swal.fire({
            title: '¿Eliminar producto?',
            text: 'Esta acción marcará el producto como inactivo',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#00ff00',
            cancelButtonColor: '#ff0066',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            background: '#1a1a2e',
            color: '#fff'
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = `/admin/productos/eliminar/${id}`;
            }
        });
    },

    // Validar formulario de producto
    validarFormulario: function () {
        const nombre = $('#nombre').val();
        const precio = $('#precioBase').val();
        const categoria = $('#categoria').val();

        if (!nombre || !precio || !categoria) {
            AdminGamer.showToast('error', 'Error', 'Complete los campos obligatorios');
            return false;
        }

        if (parseFloat(precio) <= 0) {
            AdminGamer.showToast('error', 'Error', 'El precio debe ser mayor a 0');
            return false;
        }

        return true;
    }
};

// ============================================
// INICIALIZAR AL CARGAR LA PÁGINA
// ============================================
$(document).ready(function () {
    AdminGamer.init();

    if ($('#pos-container').length) {
        POS.init();
    }
});

// ============================================
// UTILIDADES GLOBALES
// ============================================
window.AdminGamer = AdminGamer;
window.POS = POS;
window.Productos = Productos;
