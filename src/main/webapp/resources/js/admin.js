/**
 * Control del Sidebar - Hospital General
 * Gestiona el colapso, expansión y persistencia del estado del menú.
 */

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const main = document.getElementById('mainContent');
    const btnIcon = document.querySelector('.sidebar-toggle-btn i');
    const toggleBtn = document.querySelector('.sidebar-toggle-btn');

    sidebar.classList.toggle('collapsed');
    const isCollapsed = sidebar.classList.contains('collapsed');

    // Mover contenido principal
    if (main) {
        main.style.marginLeft = isCollapsed
                ? 'var(--sidebar-collapsed-width)'
                : 'var(--sidebar-width)';
    }

    // CORRECCIÓN: Mover el botón para que encabalgue (efecto tirador)
    if (toggleBtn) {
        // Obtenemos el ancho real del botón (por ejemplo, 36px)
        const btnWidth = toggleBtn.offsetWidth;
        // Calculamos la mitad (por ejemplo, 18px)
        const halfWidth = btnWidth / 2;

        // Calculamos la posición base según las variables CSS
        const basePosition = isCollapsed
                ? 'var(--sidebar-collapsed-width)'
                : 'var(--sidebar-width)';

        // Aplicamos la posición restando la mitad para encabalgarlo
        // Resultado: left: calc(var(--base-position) - 18px);
        toggleBtn.style.left = `calc(${basePosition} - ${halfWidth}px)`;
    }

    // Cambiar icono
    if (btnIcon) {
        btnIcon.classList.toggle('pi-bars', !isCollapsed);
        btnIcon.classList.toggle('pi-chevron-right', isCollapsed);
    }

    sessionStorage.setItem('adminSidebarCollapsed', isCollapsed);
}

/**
 * Restauración del estado al cargar la página (IIFE)
 */
(function () {
    // Es vital usar DOMContentLoaded para que offsetWidth funcione
    document.addEventListener("DOMContentLoaded", function () {
        const sidebar = document.getElementById('sidebar');
        const main = document.getElementById('mainContent');
        const btnIcon = document.querySelector('.sidebar-toggle-btn i');
        const toggleBtn = document.querySelector('.sidebar-toggle-btn');

        const isCollapsed = sessionStorage.getItem('adminSidebarCollapsed') === 'true';

        // Restaurar estado del sidebar
        if (sidebar) {
            if (isCollapsed) {
                sidebar.classList.add('collapsed');
            } else {
                sidebar.classList.remove('collapsed');
            }
        }

        // Restaurar posición del contenido
        if (main) {
            main.style.marginLeft = isCollapsed
                    ? 'var(--sidebar-collapsed-width)'
                    : 'var(--sidebar-width)';
        }

        // Restaurar posición encabalgada del botón al cargar
        if (toggleBtn) {
            // Repetimos la lógica matemática
            const btnWidth = toggleBtn.offsetWidth;
            const halfWidth = btnWidth / 2;

            const basePosition = isCollapsed
                    ? 'var(--sidebar-collapsed-width)'
                    : 'var(--sidebar-width)';

            toggleBtn.style.left = `calc(${basePosition} - ${halfWidth}px)`;
        }

        // Restaurar icono
        if (btnIcon) {
            if (isCollapsed) {
                btnIcon.classList.add('pi-chevron-right');
                btnIcon.classList.remove('pi-bars');
            } else {
                btnIcon.classList.add('pi-bars');
                btnIcon.classList.remove('pi-chevron-right');
            }
        }
    });
})();

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const main = document.getElementById('mainContent');
    const navbar = document.querySelector('.top-navbar'); // Referencia al navbar
    const toggleBtn = document.querySelector('.sidebar-toggle-btn');
    const btnIcon = document.querySelector('.sidebar-toggle-btn i');

    sidebar.classList.toggle('collapsed');
    const isCollapsed = sidebar.classList.contains('collapsed');

    // Definir el ancho actual del sidebar basado en las variables CSS
    const sidebarWidth = isCollapsed
            ? 'var(--sidebar-collapsed-width)'
            : 'var(--sidebar-width)';

    // 1. Mover contenido principal
    if (main) {
        main.style.marginLeft = sidebarWidth;
    }

    // 2. Mover Navbar (ajustando la propiedad 'left')
    if (navbar) {
        navbar.style.left = sidebarWidth;
    }

    // 3. Mover botón flotante (encabalgado a la mitad)
    if (toggleBtn) {
        const halfWidth = toggleBtn.offsetWidth / 2;
        toggleBtn.style.left = `calc(${sidebarWidth} - ${halfWidth}px)`;
    }

    // 4. Icono y Persistencia
    if (btnIcon) {
        btnIcon.classList.toggle('pi-bars', !isCollapsed);
        btnIcon.classList.toggle('pi-chevron-right', isCollapsed);
    }
    sessionStorage.setItem('adminSidebarCollapsed', isCollapsed);
}

// Inicialización al cargar la página
(function () {
    document.addEventListener("DOMContentLoaded", function () {
        const sidebar = document.getElementById('sidebar');
        const main = document.getElementById('mainContent');
        const navbar = document.querySelector('.top-navbar');
        const toggleBtn = document.querySelector('.sidebar-toggle-btn');
        const btnIcon = document.querySelector('.sidebar-toggle-btn i');

        const isCollapsed = sessionStorage.getItem('adminSidebarCollapsed') === 'true';
        const sidebarWidth = isCollapsed ? 'var(--sidebar-collapsed-width)' : 'var(--sidebar-width)';

        if (isCollapsed && sidebar)
            sidebar.classList.add('collapsed');
        if (main)
            main.style.marginLeft = sidebarWidth;
        if (navbar)
            navbar.style.left = sidebarWidth;

        if (toggleBtn) {
            const halfWidth = toggleBtn.offsetWidth / 2;
            toggleBtn.style.left = `calc(${sidebarWidth} - ${halfWidth}px)`;
        }

        if (btnIcon && isCollapsed) {
            btnIcon.classList.remove('pi-bars');
            btnIcon.classList.add('pi-chevron-right');
        }
    });
})();