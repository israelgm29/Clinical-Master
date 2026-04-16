package com.mycompany.hospitalgeneral.util;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import java.io.Serializable;

@Named
@SessionScoped
public class NurseMenuBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private MenuModel menuModel;

    @PostConstruct
    public void init() {
        menuModel = new DefaultMenuModel();

        // Al usar *.xhtml en web.xml, JSF maneja el contexto automáticamente.
        // Mantenemos la extensión .xhtml para mayor claridad en el resultado.
        String redirect = "?faces-redirect=true";

        // === DASHBOARD ===
        menuModel.getElements().add(
                DefaultMenuItem.builder()
                        .value("Dashboard Enfermería")
                        .icon("pi pi-home")
                        .outcome("/views/nurse/dashboard.xhtml" + redirect)
                        .build()
        );

        // === SUBMENÚ PACIENTES ===
        DefaultSubMenu pacientesSub = DefaultSubMenu.builder()
                .label("Pacientes")
                .icon("pi pi-users")
                .build();

        // Gestión de Pacientes
        pacientesSub.getElements().add(
                DefaultMenuItem.builder()
                        .value("Gestión de Pacientes")
                        .icon("pi pi-user-plus")
                        .outcome("/views/nurse/gestion-pacientes.xhtml" + redirect)
                        .build()
        );

        // Signos Vitales (temporalmente al dashboard según tu lógica)
        pacientesSub.getElements().add(
                DefaultMenuItem.builder()
                        .value("Signos Vitales")
                        .icon("pi pi-heart")
                        .outcome("/views/nurse/dashboard.xhtml" + redirect)
                        .build()
        );

        menuModel.getElements().add(pacientesSub);

        // === REPORTES ===
        menuModel.getElements().add(
                DefaultMenuItem.builder()
                        .value("Reportes")
                        .icon("pi pi-chart-line")
                        .outcome("/views/nurse/reportes/lista.xhtml" + redirect)
                        .build()
        );
    }

    public MenuModel getMenuModel() {
        return menuModel;
    }

    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }
}
