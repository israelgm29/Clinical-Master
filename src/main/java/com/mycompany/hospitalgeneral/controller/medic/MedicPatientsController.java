package com.mycompany.hospitalgeneral.controller.medic;

import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.services.MedicalRecordService;
import com.mycompany.hospitalgeneral.session.UserSession;
import com.mycompany.hospitalgeneral.session.ConsultationContext;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class MedicPatientsController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MedicalRecordService medicalRecordService;

    @Inject
    private UserSession userSession;

    @Inject
    private ConsultationContext consultationContext;

    private List<Patient> patients;
    private List<Patient> allPatients;
    private String searchQuery;
    private String activeFilter = "ALL";

    @PostConstruct
    public void init() {
        loadPatients();
    }

    /**
     * Carga pacientes únicos atendidos por este médico
     */
    public void loadPatients() {
        // ✅ CORREGIDO: Usar getMedic().getId() en lugar de getUser().getId()
        Integer medicId = getMedicId();

        if (medicId == null) {
            this.patients = List.of(); // Lista vacía si no es médico
            this.allPatients = List.of();
            return;
        }

        List<Medicalrecord> records = medicalRecordService.findByMedic(medicId);

        this.allPatients = records.stream()
                .map(Medicalrecord::getPatientid)
                .filter(p -> p != null) // ✅ Agregado filtro null
                .distinct()
                .collect(Collectors.toList());

        this.patients = this.allPatients;
    }

    /**
     * Obtiene el ID del médico logueado de forma segura
     */
    private Integer getMedicId() {
        if (userSession == null || userSession.getUser() == null) {
            return null;
        }

        // ✅ CORREGIDO: Verificar si es médico y obtener ID correcto
        if (userSession.getUser().isMedic() && userSession.getMedic() != null) {
            return userSession.getMedic().getId();
        }

        return null;
    }

    public void setFilter(String filter) {
        this.activeFilter = filter;
        applyFilters();
    }

    public void search() {
        applyFilters();
    }

    private void applyFilters() {
        Integer medicId = getMedicId();
        if (medicId == null) {
            this.patients = List.of();
            return;
        }

        this.patients = this.allPatients.stream()
                .filter(this::matchesSearch)
                .filter(p -> matchesFilter(p, medicId))
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(Patient p) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return true;
        }
        String q = searchQuery.toLowerCase();
        String fullName = (p.getFirstname() + " " + p.getLastname()).toLowerCase();
        return fullName.contains(q)
                || p.getHc().toLowerCase().contains(q)
                || (p.getDni() != null && p.getDni().toLowerCase().contains(q));
    }

    /**
     * Filtros basados en datos reales de tu base de datos
     */
    private boolean matchesFilter(Patient p, Integer medicId) {
        Medicalrecord lastRecord = getLastMedicalRecord(p.getId(), medicId);

        switch (activeFilter) {
            case "ACTIVE":
                return lastRecord != null
                        && Boolean.FALSE.equals(lastRecord.getDone())
                        && Boolean.FALSE.equals(lastRecord.getCanceled());

            case "COMPLETED":
                return lastRecord != null
                        && Boolean.TRUE.equals(lastRecord.getDone());

            case "WITH_VITALS":
                return lastRecord != null
                        && lastRecord.getVitalsignCollection() != null
                        && !lastRecord.getVitalsignCollection().isEmpty();

            case "WITH_DIAGNOSTIC":
                return lastRecord != null
                        && lastRecord.getDiagnosticCollection() != null
                        && !lastRecord.getDiagnosticCollection().isEmpty();

            case "WITHOUT_DIAGNOSTIC":
                return lastRecord != null
                        && (lastRecord.getDiagnosticCollection() == null
                        || lastRecord.getDiagnosticCollection().isEmpty());

            case "ALL":
            default:
                return true;
        }
    }

    // ==================== HELPERS ====================
    public String getInitials(Patient p) {
        String f = p.getFirstname() != null ? p.getFirstname() : "";
        String l = p.getLastname() != null ? p.getLastname() : "";
        return (f.isEmpty() ? "" : f.substring(0, 1))
                + (l.isEmpty() ? "" : l.substring(0, 1));
    }

    public int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private Medicalrecord getLastMedicalRecord(Integer patientId, Integer medicId) {
        return medicalRecordService.findLastByPatientAndMedic(patientId, medicId);
    }

    public LocalDateTime getLastVisitDateTime(Patient p) {
        Integer medicId = getMedicId();
        if (medicId == null) {
            return null;
        }
        Medicalrecord last = getLastMedicalRecord(p.getId(), medicId);
        return last != null ? last.getCreatedat() : null;
    }

    public LocalDate getLastVisitDate(Patient p) {
        LocalDateTime ldt = getLastVisitDateTime(p);
        return ldt != null ? ldt.toLocalDate() : null;
    }

    public String getDaysSinceLastVisit(Patient p) {
        LocalDateTime last = getLastVisitDateTime(p);
        if (last == null) {
            return "Sin visitas";
        }

        long days = ChronoUnit.DAYS.between(last, LocalDateTime.now());

        if (days == 0) {
            return "Hoy";
        }
        if (days == 1) {
            return "Ayer";
        }
        if (days < 30) {
            return "Hace " + days + " días";
        }
        if (days < 365) {
            return "Hace " + (days / 30) + " meses";
        }
        return "Hace " + (days / 365) + " años";
    }

    public boolean hasActiveConsultation(Patient p) {
        Integer medicId = getMedicId();
        if (medicId == null) {
            return false;
        }
        Medicalrecord last = getLastMedicalRecord(p.getId(), medicId);
        return last != null
                && Boolean.FALSE.equals(last.getDone())
                && Boolean.FALSE.equals(last.getCanceled());
    }

    // ==================== ACCIONES ====================
    public String viewHistory(Patient patient) {
        consultationContext.setCurrentPatient(patient);
        // Limpiar cualquier consulta anterior en contexto
        consultationContext.setCurrentMedicalRecord(null);
        return "/views/medic/patient-history.xhtml?faces-redirect=true";
    }

    public String newConsultation(Patient patient) {
        consultationContext.setCurrentPatient(patient);
        return "/views/medic/new-consultation.xhtml?faces-redirect=true";
    }

    // ==================== GETTERS / SETTERS ====================
    public List<Patient> getPatients() {
        return patients;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getActiveFilter() {
        return activeFilter;
    }
}
