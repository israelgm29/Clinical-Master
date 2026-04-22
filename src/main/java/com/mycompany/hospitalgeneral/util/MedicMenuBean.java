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
public class MedicMenuBean implements Serializable {

    private static final long serialVersionUID = 1L; // Añadido por seguridad de sesión
    private MenuModel menuModel;

    @PostConstruct
    public void init() {
        menuModel = new DefaultMenuModel();

        // Al usar *.xhtml en web.xml, JSF se encarga de todo.
        String redirect = "?faces-redirect=true";

        // === DASHBOARD ===
        menuModel.getElements().add(
                DefaultMenuItem.builder()
                        .value("Mi Agenda")
                        .icon("pi pi-calendar")
                        .outcome("/views/medic/dashboard.xhtml" + redirect)
                        .build()
        );

        // === PACIENTES (Submenú) ===
        DefaultSubMenu pacientesSub = DefaultSubMenu.builder()
                .label("Pacientes")
                .icon("pi pi-users")
                .build();

        pacientesSub.getElements().add(
                DefaultMenuItem.builder()
                        .value("Mis Pacientes")
                        .icon("pi pi-list")
                        .outcome("/views/medic/my-patients.xhtml" + redirect)
                        .build()
        );

        pacientesSub.getElements().add(
                DefaultMenuItem.builder()
                        .value("Nueva Consulta")
                        .icon("pi pi-plus-circle")
                        .outcome("/views/medic/consulta.xhtml" + redirect)
                        .build()
        );
        menuModel.getElements().add(pacientesSub);

        // === RECETAS (Submenú) ===
        DefaultSubMenu recetasSub = DefaultSubMenu.builder()
                .label("Recetas")
                .icon("pi pi-file")
                .build();

        recetasSub.getElements().add(
                DefaultMenuItem.builder()
                        .value("Emitir Receta")
                        .icon("pi pi-pencil")
                        .outcome("/views/medic/dashboard.xhtml" + redirect)
                        .build()
        );

        recetasSub.getElements().add(
                DefaultMenuItem.builder()
                        .value("Mis Recetas")
                        .icon("pi pi-book")
                        .outcome("/views/medic/dashboard.xhtml" + redirect)
                        .build()
        );
        menuModel.getElements().add(recetasSub);

        // === RESULTADOS ===
        menuModel.getElements().add(
                DefaultMenuItem.builder()
                        .value("Ver Exámenes")
                        .icon("pi pi-chart-bar")
                        .outcome("/views/medic/dashboard.xhtml" + redirect)
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
