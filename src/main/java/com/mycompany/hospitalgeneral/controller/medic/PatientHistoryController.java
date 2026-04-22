package com.mycompany.hospitalgeneral.controller.medic;

import com.mycompany.hospitalgeneral.model.*;
import com.mycompany.hospitalgeneral.services.AntecedentService;
import com.mycompany.hospitalgeneral.services.MedicalRecordService;
import com.mycompany.hospitalgeneral.services.VitalsignService;
import com.mycompany.hospitalgeneral.session.ConsultationContext;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class PatientHistoryController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MedicalRecordService medicalRecordService;

    @Inject
    private VitalsignService vitalsignService;

    @Inject
    private AntecedentService antecedentService;

    @Inject
    private ConsultationContext consultationContext;

    // Paciente actual
    private Patient patient;
    private Antecedent antecedent;
    private Vitalsign lastVitalSign;
    private LocalDateTime lastVitalSignDate;

    // Consultas
    private List<Medicalrecord> allRecords;
    private List<Medicalrecord> filteredRecords;
    private String filter = "ALL";

    @PostConstruct
    public void init() {
        this.patient = consultationContext.getCurrentPatient();

        if (this.patient == null) {
            return;
        }

        loadPatientData();
        loadConsultations();
    }

    private void loadPatientData() {
        this.antecedent = antecedentService.findByPatientId(patient.getId());
        this.lastVitalSign = vitalsignService.findLastByPatientId(patient.getId());
        if (this.lastVitalSign != null) {
            this.lastVitalSignDate = this.lastVitalSign.getCreatedat();
        }
    }

    private void loadConsultations() {
        // Todas las consultas del paciente, de todos los médicos
        this.allRecords = medicalRecordService.findByPatient(patient.getId());
        this.allRecords.sort((a, b) -> b.getCreatedat().compareTo(a.getCreatedat()));
        applyFilter();
    }

    public void applyFilter() {
        LocalDateTime now = LocalDateTime.now();

        this.filteredRecords = this.allRecords.stream()
                .filter(r -> {
                    switch (filter) {
                        case "YEAR":
                            return r.getCreatedat().isAfter(now.minusYears(1));
                        case "COMPLETED":
                            return Boolean.TRUE.equals(r.getDone());
                        case "PENDING":
                            return Boolean.FALSE.equals(r.getDone()) && Boolean.FALSE.equals(r.getCanceled());
                        case "ALL":
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }

    public void setFilter(String filter) {
        this.filter = filter;
        applyFilter();
    }

    // ==================== HELPERS ====================
    public String getPatientInitials() {
        String f = patient.getFirstname() != null ? patient.getFirstname() : "";
        String l = patient.getLastname() != null ? patient.getLastname() : "";
        return (f.isEmpty() ? "" : f.substring(0, 1)) + (l.isEmpty() ? "" : l.substring(0, 1));
    }

    public String getPatientFullName() {
        return patient.getLastname() + " " + patient.getFirstname();
    }

    public int getPatientAge() {
        if (patient.getBirthday() == null) {
            return 0;
        }
        return Period.between(patient.getBirthday(), LocalDate.now()).getYears();
    }

    public boolean isBloodPressureAlert() {
        if (lastVitalSign == null) {
            return false;
        }
        try {
            int systolic = Integer.parseInt(lastVitalSign.getSystolicpressure());
            int diastolic = Integer.parseInt(lastVitalSign.getDiastolicpressure());
            return systolic > 140 || diastolic > 90 || systolic < 90 || diastolic < 60;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isAllergies() {
        return antecedent != null
                && antecedent.getAllergy() != null
                && !antecedent.getAllergy().trim().isEmpty();
    }

    public List<String> getAllergies() {
        if (!isAllergies()) {
            return List.of();
        }
        return Arrays.stream(antecedent.getAllergy().split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public boolean isChronicConditions() {
        return !getChronicConditions().isEmpty();
    }

    public List<String> getChronicConditions() {
        List<String> conditions = new ArrayList<>();

        if (antecedent != null && antecedent.getClinician() != null) {
            String clin = antecedent.getClinician().toLowerCase();
            if (clin.contains("diabetes")) {
                conditions.add("Diabetes Mellitus");
            }
            if (clin.contains("hipertensión")) {
                conditions.add("Hipertensión Arterial");
            }
            if (clin.contains("asma")) {
                conditions.add("Asma");
            }
            if (clin.contains("epoc")) {
                conditions.add("EPOC");
            }
        }

        Map<String, Long> diagnosisCount = allRecords.stream()
                .flatMap(r -> r.getDiagnosticCollection() != null
                ? r.getDiagnosticCollection().stream()
                : java.util.stream.Stream.empty())
                .filter(d -> d.getDeleted() == null || !d.getDeleted())
                .collect(Collectors.groupingBy(
                        d -> d.getDiseaseid().getName(),
                        Collectors.counting()
                ));

        diagnosisCount.entrySet().stream()
                .filter(e -> e.getValue() >= 3)
                .forEach(e -> {
                    if (!conditions.contains(e.getKey())) {
                        conditions.add(e.getKey() + " (" + e.getValue() + " consultas)");
                    }
                });

        return conditions;
    }

    public int getTotalConsultations() {
        return allRecords != null ? allRecords.size() : 0;
    }

    // ==================== ACCIONES ====================
    public String viewFullConsultation(Medicalrecord record) {
        consultationContext.setCurrentMedicalRecord(record);
        return "/views/medic/consultation-detail.xhtml?faces-redirect=true";
    }

    public String newSimilarConsultation(Medicalrecord record) {
        consultationContext.setCurrentPatient(patient);
//        if (record.getReason() != null) {
//            consultationContext.setPreFilledReason("Seguimiento: " + record.getReason());
//        }
        return "/views/medic/new-consultation.xhtml?faces-redirect=true";
    }

    public String backToMyPatients() {
        return "/views/medic/my-patients.xhtml?faces-redirect=true";
    }

    // ==================== GETTERS ====================
    public Patient getPatient() {
        return patient;
    }

    public Antecedent getAntecedent() {
        return antecedent;
    }

    public Vitalsign getLastVitalSign() {
        return lastVitalSign;
    }

    public LocalDateTime getLastVitalSignDate() {
        return lastVitalSignDate;
    }

    public List<Medicalrecord> getFilteredRecords() {
        return filteredRecords;
    }

    public String getFilter() {
        return filter;
    }

}
