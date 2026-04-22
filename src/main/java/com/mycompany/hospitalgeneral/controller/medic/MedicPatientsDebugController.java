package com.mycompany.hospitalgeneral.controller.medic;

import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.services.MedicalRecordService;
import com.mycompany.hospitalgeneral.session.UserSession;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class MedicPatientsDebugController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MedicalRecordService medicalRecordService;

    @Inject
    private UserSession userSession;

    // Datos para debug
    private String debugInfo;
    private Integer currentUserId;
    private Integer medicId;
    private List<Medicalrecord> allRecords;
    private List<Patient> uniquePatients;

    @PostConstruct
    public void init() {
        debug();
    }

    public void debug() {
        StringBuilder sb = new StringBuilder();

        // 1. Verificar usuario
        if (userSession == null) {
            sb.append("ERROR: userSession es NULL\n");
            debugInfo = sb.toString();
            return;
        }

        if (userSession.getUser() == null) {
            sb.append("ERROR: userSession.getUser() es NULL\n");
            debugInfo = sb.toString();
            return;
        }

        currentUserId = userSession.getUser().getId();
        sb.append("User ID: ").append(currentUserId).append("\n");

        // 2. Verificar si el usuario tiene médico asociado
        try {
            // Ajusta esto según tu modelo real
            if (userSession.getUser().isMedic()) {
                medicId = userSession.getMedic().getId();
                sb.append("Medic ID (desde user.medicid): ").append(medicId).append("\n");
            } else {
                // Si no hay relación directa, usar el user ID como medic ID
                medicId = currentUserId;
                sb.append("Medic ID (usando user ID): ").append(medicId).append("\n");
            }
        } catch (Exception e) {
            sb.append("ERROR al obtener medicId: ").append(e.getMessage()).append("\n");
            medicId = currentUserId; // fallback
        }

        // 3. Consultar todas las historias de este médico
        try {
            allRecords = medicalRecordService.findByMedic(medicId);
            sb.append("Total records encontrados: ").append(allRecords != null ? allRecords.size() : "NULL").append("\n");

            if (allRecords != null && !allRecords.isEmpty()) {
                sb.append("Primer record - ID: ").append(allRecords.get(0).getId()).append("\n");
                sb.append("Primer record - Patient: ").append(
                    allRecords.get(0).getPatientid() != null 
                        ? allRecords.get(0).getPatientid().getFirstname() + " " + allRecords.get(0).getPatientid().getLastname()
                        : "NULL"
                ).append("\n");
                sb.append("Primer record - Created: ").append(allRecords.get(0).getCreatedat()).append("\n");
            }
        } catch (Exception e) {
            sb.append("ERROR en findByMedic: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }

        // 4. Extraer pacientes únicos
        try {
            if (allRecords != null) {
                uniquePatients = allRecords.stream()
                    .map(Medicalrecord::getPatientid)
                    .filter(p -> p != null)
                    .distinct()
                    .collect(Collectors.toList());
                sb.append("Pacientes únicos: ").append(uniquePatients.size()).append("\n");
            }
        } catch (Exception e) {
            sb.append("ERROR extrayendo pacientes: ").append(e.getMessage()).append("\n");
        }

        debugInfo = sb.toString();
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Debug", "Revisa la consola"));
    }

    // Getters
    public String getDebugInfo() { return debugInfo; }
    public Integer getCurrentUserId() { return currentUserId; }
    public Integer getMedicId() { return medicId; }
    public List<Medicalrecord> getAllRecords() { return allRecords; }
    public List<Patient> getUniquePatients() { return uniquePatients; }
}