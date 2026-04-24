package com.mycompany.hospitalgeneral.util;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class AdminMenuBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<MenuItemModel> dashboard;
    private List<MenuItemModel> gestionPrincipal;
    private List<MenuItemModel> catalogos;
    private List<MenuItemModel> configuracion;

    @PostConstruct
    public void init() {

        String redirect = "?faces-redirect=true";

        // === DASHBOARD ===
        dashboard = List.of(
                new MenuItemModel(
                        "Dashboard",
                        "pi pi-th-large",
                        "/views/admin/dashboard/dashboard.xhtml" + redirect,
                        "Dashboard Principal"
                )
        );

        // === GESTIÓN PRINCIPAL ===
        gestionPrincipal = List.of(
                new MenuItemModel("Pacientes", "pi pi-users",
                        "/views/admin/patient/GestionPacientes.xhtml" + redirect,
                        "Gestión de Pacientes"),
                new MenuItemModel("Hospital", "pi pi-building",
                        "/views/admin/company/GestionHospital.xhtml" + redirect,
                        "Información del Hospital"),
                new MenuItemModel("Médicos", "pi pi-users",
                        "/views/admin/medic/GestionMedicos.xhtml" + redirect,
                        "Gestión de Médicos"),
                new MenuItemModel("Asignacion de Especialidades", "pi pi-wave-pulse",
                        "/views/admin/medicalspecialty/GestionAsignacionEspecialidades.xhtml" + redirect,
                        "Asignacion de especialidades medicas")
        );

        // === CATÁLOGOS MÉDICOS ===
        catalogos = List.of(
                new MenuItemModel("Enfermedades", "pi pi-book",
                        "/views/admin/disease/GestionEnfermedades.xhtml" + redirect,
                        "Catálogo de Enfermedades"),
                new MenuItemModel("Tipos de Enfermedades", "pi pi-book",
                        "/views/admin/diseasetype/GestionTiposEnfermedades.xhtml" + redirect,
                        "Tipos de Patologías"),
                new MenuItemModel("Exámenes", "pi pi-chart-bar",
                        "/views/admin/exam/GestionExamenes.xhtml" + redirect,
                        "Gestión de Exámenes"),
                new MenuItemModel("Tipos de Exámenes", "pi pi-list-check",
                        "/views/admin/examtype/GestionTiposExamenes.xhtml" + redirect,
                        "Categorías de Exámenes")
        );

        // === CONFIGURACIÓN ===
        configuracion = List.of(
                new MenuItemModel("Opciones", "pi pi-sliders-h",
                        "/views/admin/option/GestionOpciones.xhtml" + redirect,
                        "Opciones del Sistema"),
                new MenuItemModel("Permisos", "pi pi-shield",
                        "/views/admin/permission/GestionPermisos.xhtml" + redirect,
                        "Gestión de Seguridad"),
                new MenuItemModel("Módulos", "pi pi-address-book",
                        "/views/admin/module/GestionModulos.xhtml" + redirect,
                        "Módulos Instalados"),
                new MenuItemModel("Usuarios", "pi pi-user-edit",
                        "/views/admin/module/GestionModulos.xhtml" + redirect,
                        "Usuarios del sistema"),
                new MenuItemModel("Especialidades Medicas", "pi pi-briefcase",
                        "/views/admin/specialist/GestionEspecialidades.xhtml" + redirect,
                        "Especialidades Medicas")
        );
    }

    public List<MenuItemModel> getDashboard() {
        return dashboard;
    }

    public List<MenuItemModel> getGestionPrincipal() {
        return gestionPrincipal;
    }

    public List<MenuItemModel> getCatalogos() {
        return catalogos;
    }

    public List<MenuItemModel> getConfiguracion() {
        return configuracion;
    }
}
