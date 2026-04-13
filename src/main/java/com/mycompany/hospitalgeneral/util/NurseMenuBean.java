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

    private MenuModel menuModel;

    @PostConstruct
    public void init() {
        menuModel = new DefaultMenuModel();

        // === DASHBOARD ===
        DefaultMenuItem dashboard = DefaultMenuItem.builder()
                .value("Dashboard Enfermería")
                .icon("pi pi-home")
                .url("/views/nurse/Dashboard.xhtml")
                .build();
        menuModel.getElements().add(dashboard);

        // === PACIENTES ===
        DefaultSubMenu pacientesSub = DefaultSubMenu.builder()
                .label("Pacientes")
                .icon("pi pi-users")
                .build();

        DefaultMenuItem recepcion = DefaultMenuItem.builder()
                .value("Recepción")
                .icon("pi pi-user-plus")
                .url("/views/nurse/Dashboard.xhtml")
                .build();

        DefaultMenuItem signosVitales = DefaultMenuItem.builder()
                .value("Signos Vitales")
                .icon("pi pi-heart")
                .url("/views/nurse/Dashboard.xhtml")
                .build();

        pacientesSub.getElements().add(recepcion);
        pacientesSub.getElements().add(signosVitales);
        menuModel.getElements().add(pacientesSub);

        // === REPORTES ===
        DefaultMenuItem reportes = DefaultMenuItem.builder()
                .value("Reportes")
                .icon("pi pi-chart-line")
                .url("/views/nurse/reportes/lista.xhtml")
                .build();
        menuModel.getElements().add(reportes);
    }

    public MenuModel getMenuModel() {
        return menuModel;
    }
}
