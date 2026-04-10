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

    private MenuModel menuModel;

    @PostConstruct
    public void init() {
        menuModel = new DefaultMenuModel();

        // === DASHBOARD ===
        DefaultMenuItem dashboard = DefaultMenuItem.builder()
                .value("Mi Agenda")
                .icon("pi pi-calendar")
                .url("/views/medic/dashboard.xhtml")
                .build();
        menuModel.getElements().add(dashboard);

        // === PACIENTES (Submenú) ===
        DefaultSubMenu pacientesSub = DefaultSubMenu.builder()
                .label("Pacientes")
                .icon("pi pi-users")
                .build();

        DefaultMenuItem misPacientes = DefaultMenuItem.builder()
                .value("Mis Pacientes")
                .icon("pi pi-list")
                .url("/views/medic/pacientes/lista.xhtml")
                .build();

        DefaultMenuItem nuevaConsulta = DefaultMenuItem.builder()
                .value("Nueva Consulta")
                .icon("pi pi-plus-circle")
                .url("/views/medic/pacientes/consulta.xhtml")
                .build();

        pacientesSub.getElements().add(misPacientes);
        pacientesSub.getElements().add(nuevaConsulta);
        menuModel.getElements().add(pacientesSub);

        // === RECETAS (Submenú) ===
        DefaultSubMenu recetasSub = DefaultSubMenu.builder()
                .label("Recetas")
                .icon("pi pi-file")
                .build();

        DefaultMenuItem emitirReceta = DefaultMenuItem.builder()
                .value("Emitir Receta")
                .icon("pi pi-pencil")
                .url("/views/medic/recetas/emitir.xhtml")
                .build();

        DefaultMenuItem misRecetas = DefaultMenuItem.builder()
                .value("Mis Recetas")
                .icon("pi pi-book")
                .url("/views/medic/recetas/mis-recetas.xhtml")
                .build();

        recetasSub.getElements().add(emitirReceta);
        recetasSub.getElements().add(misRecetas);
        menuModel.getElements().add(recetasSub);

        // === RESULTADOS ===
        DefaultMenuItem resultados = DefaultMenuItem.builder()
                .value("Ver Exámenes")
                .icon("pi pi-chart-bar")
                .url("/views/medic/resultados/lista.xhtml")
                .build();
        menuModel.getElements().add(resultados);
    }

    public MenuModel getMenuModel() {
        return menuModel;
    }
}
