/**
 * ============================================
 * JAVASCRIPT - FRONTEND GAMER
 * Funcionalidad para el bot�n PlayStation y m�s
 * ============================================
 */

document.addEventListener('DOMContentLoaded', function () {
    console.log('<� Frontend Gamer JS cargado');

    // --- 1. FUNCIÓN HELPER PARA CSRF (VITAL) ---
    function getCSRFHeaders() {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        // Prepara los headers base
        const headers = {
            // El Content-Type se definirá en cada llamada fetch
        };

        if (token && header) {
            headers[header] = token;
        } else {
            console.error("Meta tags CSRF no encontrados. Las peticiones POST fallarán.");
        }
        return headers;
    }

    // ========================================
    // BOT�N PLAYSTATION - REDES SOCIALES
    // ========================================

    const buttons = {
        cross: document.getElementById('cross'),
        circle: document.getElementById('circle'),
        square: document.getElementById('square'),
        triangle: document.getElementById('triangle')
    };

    // Funci�n para abrir red social
    function openSocialMedia(button) {
        const url = button.getAttribute('data-url');

        if (url && url !== 'null' && url.trim() !== '') {
            console.log('= Abriendo:', url);
            window.open(url, '_blank', 'noopener,noreferrer');
        } else {
            console.warn('� URL no configurada para este bot�n');
            // Opcional: mostrar mensaje al usuario
            showNotification('Red social no configurada');
        }
    }

    // Agregar eventos a los botones
    Object.values(buttons).forEach(button => {
        if (button) {
            button.addEventListener('click', function (e) {
                e.preventDefault();
                openSocialMedia(this);
            });
        }
    });

    // ========================================
    // NOTIFICACIONES
    // ========================================

    function showNotification(message) {
        // Crear elemento de notificaci�n
        const notification = document.createElement('div');
        notification.className = 'gamer-notification';
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: linear-gradient(135deg, #FF0080, #7B2FFF);
            color: white;
            padding: 1rem 1.5rem;
            border-radius: 10px;
            box-shadow: 0 5px 20px rgba(255, 0, 128, 0.5);
            z-index: 10000;
            font-weight: 600;
            animation: slideInRight 0.3s ease;
        `;

        document.body.appendChild(notification);

        // Remover despu�s de 3 segundos
        setTimeout(() => {
            notification.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => {
                notification.remove();
            }, 300);
        }, 3000);
    }

    // ========================================
    // ANIMACIONES DE SCROLL
    // ========================================

    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    // Observar cards de productos
    document.querySelectorAll('.product-card').forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        card.style.transition = 'all 0.6s ease';
        observer.observe(card);
    });

    // ========================================
    // CARRITO - CONTADOR
    // ========================================

    async function updateCartBadge() {
        try {
            const res = await fetch('/carrito/cantidad');
            if (!res.ok) return;
            const json = await res.json();
            const cantidad = json.cantidadTotal || 0;
            // Actualizar todos los elementos con clase cart-badge
            document.querySelectorAll('.cart-badge').forEach(el => {
                el.textContent = cantidad;
                el.style.display = cantidad > 0 ? 'inline-block' : 'none';
            });
        } catch (err) {
            console.warn('No se pudo actualizar contador de carrito', err);
        }
    }

    // ========================================
    // B�SQUEDA CON ANIMACI�N
    // ========================================

    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('focus', function () {
            this.style.transform = 'scale(1.05)';
        });

        searchInput.addEventListener('blur', function () {
            this.style.transform = 'scale(1)';
        });
    }

    // ========================================
    // EFECTOS DE HOVER EN CARDS
    // ========================================

    document.querySelectorAll('.product-card').forEach(card => {
        card.addEventListener('mouseenter', function () {
            this.style.transform = 'translateY(-10px) scale(1.02)';
        });

        card.addEventListener('mouseleave', function () {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });

    // ========================================
    // LOADING PARA BOTONES
    // ========================================

    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function () {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn && !submitBtn.disabled) {
                submitBtn.disabled = true;
                const originalText = submitBtn.innerHTML;
                submitBtn.innerHTML = '<span class="loading"></span> Procesando...';

                // Restaurar despu�s de 3 segundos si no hay redirecci�n
                setTimeout(() => {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalText;
                }, 3000);
            }
        });
    });

    async function addToCart(productoId, cantidad = 1, button) {
        const originalHtml = button.innerHTML;
        button.disabled = true;
        button.innerHTML = '<span class="loading"></span>';

        try {
            const body = new URLSearchParams();
            body.append('productoId', productoId);
            body.append('cantidad', cantidad);

            const res = await fetch('/carrito/agregar', {
                method: 'POST',
                headers: {
                    ...getCSRFHeaders(), // <-- ¡AQUÍ ESTÁ LA CORRECCIÓN!
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: body.toString()
            });

            const json = await res.json();
            if (json.success) {
                updateCartBadge();
                showNotification(json.message || 'Producto agregado al carrito', 'success');
            } else {
                showNotification(json.error || 'No se pudo agregar al carrito', 'error');
            }
        } catch (err) {
            console.error(err);
            showNotification('Error al agregar al carrito', 'error');
        } finally {
            button.disabled = false;
            button.innerHTML = originalHtml;
        }
    }

    // Delegated listener for "Add to cart" buttons
    document.body.addEventListener('click', function (e) {
        const btn = e.target.closest('.add-to-cart');
        if (!btn) return;
        e.preventDefault();

        const productoId = btn.getAttribute('data-product-id');
        let qty = 1;
        try {
            // Intenta buscar el input de cantidad (para pág. de detalle)
            const qtyInput = document.getElementById('cantidad-' + productoId);
            if (qtyInput) {
                qty = qtyInput.value || 1;
            }
        } catch (err) { /* ignora si no lo encuentra */ }

        addToCart(productoId, qty, btn); // Pasa el botón
    });

    // Inicializar contador al cargar
    updateCartBadge();
    // ========================================
    // ANIMACI�N DEL HERO
    // ========================================

    const heroTitle = document.querySelector('.hero-title');
    const heroSubtitle = document.querySelector('.hero-subtitle');

    if (heroTitle) {
        setTimeout(() => {
            heroTitle.style.opacity = '1';
            heroTitle.style.transform = 'translateY(0)';
        }, 100);
    }

    if (heroSubtitle) {
        setTimeout(() => {
            heroSubtitle.style.opacity = '1';
            heroSubtitle.style.transform = 'translateY(0)';
        }, 300);
    }

    console.log(' Todas las funcionalidades cargadas');
});

// ========================================
// (AÑADIDO) LÓGICA DE LA PÁGINA DEL CARRITO
// ========================================

// Muestra un loader en la página
function showPageLoader() {
    const loader = document.createElement('div');
    loader.id = 'page-loader';
    loader.style.cssText = `
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.5); z-index: 9998;
            display: flex; align-items: center; justify-content: center;
        `;
    loader.innerHTML = '<span class="loading" style="width:50px; height:50px; border-top-color: var(--color-primario);"></span>';
    document.body.appendChild(loader);
}

function hidePageLoader() {
    document.getElementById('page-loader')?.remove();
}

// Asignar eventos a los botones de cantidad en carrito.html
document.querySelectorAll('.quantity-btn').forEach(btn => {
    btn.addEventListener('click', function () {
        const productoId = this.getAttribute('data-producto-id');
        const input = document.querySelector(`.quantity-input[data-producto-id="${productoId}"]`);
        const cantidadActual = parseInt(input.value);
        const max = parseInt(input.getAttribute('max'));

        if (this.innerHTML.includes('fa-plus')) {
            // Incrementar
            if (cantidadActual < max) {
                actualizarCantidad(productoId, cantidadActual + 1);
            } else {
                showNotification('No hay más stock disponible', 'warning');
            }
        } else if (this.innerHTML.includes('fa-minus')) {
            // Decrementar
            if (cantidadActual > 1) {
                actualizarCantidad(productoId, cantidadActual - 1);
            }
        }
    });
});

// Asignar eventos a botones de eliminar
document.querySelectorAll('.btn-danger[onclick*="eliminarProducto"]').forEach(btn => {
    btn.onclick = function (e) { // Sobrescribir el onclick
        e.preventDefault();
        if (confirm('¿Eliminar este producto del carrito?')) {
            const productoId = this.getAttribute('data-producto-id');
            eliminarProducto(productoId);
        }
    };
});

async function actualizarCantidad(productoId, cantidad) {
    showPageLoader();
    try {
        const body = new URLSearchParams();
        body.append('productoId', productoId);
        body.append('cantidad', cantidad);

        const res = await fetch('/carrito/actualizar', {
            method: 'POST',
            headers: getCSRFHeaders(), // <-- ¡CSRF FIX!
            body: body.toString()
        });
        const json = await res.json();
        if (json.success) {
            location.reload(); // Recarga la página para ver cambios
        } else {
            showNotification(json.error || 'Error al actualizar', 'error');
            hidePageLoader();
        }
    } catch (err) {
        showNotification('Error de conexión al actualizar', 'error');
        hidePageLoader();
    }
}

async function eliminarProducto(productoId) {
    showPageLoader();
    try {
        const body = new URLSearchParams();
        body.append('productoId', productoId);

        const res = await fetch('/carrito/eliminar', {
            method: 'POST',
            headers: getCSRFHeaders(), // <-- ¡CSRF FIX!
            body: body.toString()
        });
        const json = await res.json();
        if (json.success) {
            location.reload();
        } else {
            showNotification(json.error || 'Error al eliminar', 'error');
            hidePageLoader();
        }
    } catch (err) {
        showNotification('Error de conexión al eliminar', 'error');
        hidePageLoader();
    }
}

// ========================================
// (AÑADIDO) LÓGICA DE LA PÁGINA CHECKOUT
// ========================================
const formCheckout = document.getElementById('checkout-form');
if (formCheckout) {
    const tipoEntrega = document.getElementById('tipoEntrega');
    const direccionGroup = document.getElementById('direccion-group');

    tipoEntrega?.addEventListener('change', function () {
        direccionGroup.style.display = this.value === 'DELIVERY' ? 'block' : 'none';
    });

    formCheckout.addEventListener('submit', async function (e) {
        e.preventDefault();
        const submitBtn = document.getElementById('checkout-submit');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="loading"></span> Procesando...';

        const data = {
            documento: document.getElementById('documento').value,
            nombres: document.getElementById('nombres').value,
            apellidos: document.getElementById('apellidos').value,
            telefono: document.getElementById('telefono').value,
            email: document.getElementById('email').value,
            tipoEntrega: document.getElementById('tipoEntrega').value,
            direccion: document.getElementById('direccion').value,
            referencia: document.getElementById('referencia').value,
            notas: document.getElementById('notas').value
        };

        const headers = getCSRFHeaders();
        headers['Content-Type'] = 'application/json'; // El endpoint espera JSON

        try {
            const res = await fetch('/carrito/procesar', {
                method: 'POST',
                headers: headers, // <-- ¡CSRF FIX!
                body: JSON.stringify(data) // El endpoint espera JSON
            });
            const json = await res.json();
            if (json.success) {
                window.location.href = '/carrito/confirmacion/' + json.pedidoId;
            } else {
                showNotification(json.error || 'Error al procesar el pedido', 'error');
                submitBtn.disabled = false;
                submitBtn.innerHTML = 'Procesar Pedido';
            }
        } catch (err) {
            console.error(err);
            showNotification('Error de conexión al procesar el pedido', 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Procesar Pedido';
        }
    });
}

console.log('Todas las funcionalidades (incluyendo Carrito y Checkout) cargadas');
// ========================================
// ANIMACIONES CSS
// ========================================

const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }

    @keyframes fadeInUp {
        from {
            opacity: 0;
            transform: translateY(30px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
`;
document.head.appendChild(style);
