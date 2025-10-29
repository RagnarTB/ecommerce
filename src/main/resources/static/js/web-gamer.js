/**
 * ============================================
 * JAVASCRIPT - FRONTEND GAMER
 * Funcionalidad para el botón PlayStation y más
 * ============================================
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('<® Frontend Gamer JS cargado');

    // ========================================
    // BOTÓN PLAYSTATION - REDES SOCIALES
    // ========================================

    const buttons = {
        cross: document.getElementById('cross'),
        circle: document.getElementById('circle'),
        square: document.getElementById('square'),
        triangle: document.getElementById('triangle')
    };

    // Función para abrir red social
    function openSocialMedia(button) {
        const url = button.getAttribute('data-url');

        if (url && url !== 'null' && url.trim() !== '') {
            console.log('= Abriendo:', url);
            window.open(url, '_blank', 'noopener,noreferrer');
        } else {
            console.warn('  URL no configurada para este botón');
            // Opcional: mostrar mensaje al usuario
            showNotification('Red social no configurada');
        }
    }

    // Agregar eventos a los botones
    Object.values(buttons).forEach(button => {
        if (button) {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                openSocialMedia(this);
            });
        }
    });

    // ========================================
    // NOTIFICACIONES
    // ========================================

    function showNotification(message) {
        // Crear elemento de notificación
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

        // Remover después de 3 segundos
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

    function updateCartBadge() {
        // Aquí puedes agregar lógica para actualizar el contador del carrito
        // si decides implementarlo en el navbar
        const cartBadge = document.querySelector('.cart-badge');
        if (cartBadge) {
            // Lógica para contar items en el carrito
        }
    }

    // ========================================
    // BÚSQUEDA CON ANIMACIÓN
    // ========================================

    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('focus', function() {
            this.style.transform = 'scale(1.05)';
        });

        searchInput.addEventListener('blur', function() {
            this.style.transform = 'scale(1)';
        });
    }

    // ========================================
    // EFECTOS DE HOVER EN CARDS
    // ========================================

    document.querySelectorAll('.product-card').forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px) scale(1.02)';
        });

        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });

    // ========================================
    // LOADING PARA BOTONES
    // ========================================

    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function() {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn && !submitBtn.disabled) {
                submitBtn.disabled = true;
                const originalText = submitBtn.innerHTML;
                submitBtn.innerHTML = '<span class="loading"></span> Procesando...';

                // Restaurar después de 3 segundos si no hay redirección
                setTimeout(() => {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalText;
                }, 3000);
            }
        });
    });

    // ========================================
    // ANIMACIÓN DEL HERO
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
