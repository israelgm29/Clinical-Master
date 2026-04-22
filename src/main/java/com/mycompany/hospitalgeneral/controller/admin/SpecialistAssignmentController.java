package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Medicalspecialty;
import com.mycompany.hospitalgeneral.services.MedicService;
import com.mycompany.hospitalgeneral.services.MedicalspecialtyService;
import com.mycompany.hospitalgeneral.services.SpecialistService;
import com.mycompany.hospitalgeneral.session.UserSession;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.primefaces.model.DualListModel;

@Named
@ViewScoped
public class SpecialistAssignmentController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MedicService medicService;

    @Inject
    private MedicalspecialtyService specialtyService;

    @Inject
    private SpecialistService specialistService;

    @Inject
    private UserSession userSession;

    private List<Medic> medics;
    private List<Medicalspecialty> allSpecialties;

    private Medic selectedMedic;
    private DualListModel<Medicalspecialty> specialtyPickList;
    private String searchTerm;

    @PostConstruct
    public void init() {
        loadMedics();
        loadAllSpecialties();
    }

    public void loadMedics() {
        medics = medicService.findAllActive();
        System.out.println(">>> Médicos cargados: " + (medics != null ? medics.size() : 0));
    }

    public void loadAllSpecialties() {
        allSpecialties = specialtyService.findAll();
        System.out.println(">>> Especialidades cargadas: " + (allSpecialties != null ? allSpecialties.size() : 0));
    }

    public void onMedicSelect(Medic medic) {
        this.selectedMedic = medic;
        System.out.println(">>> Médico seleccionado: " + (medic != null ? medic.getFullName() : "null"));

        if (selectedMedic != null) {
            loadPickListModel();
        } else {
            specialtyPickList = null;
        }
    }

    private void loadPickListModel() {
        // Especialidades ya asignadas al médico
        List<Medicalspecialty> assigned = specialistService.findSpecialtiesByMedic(selectedMedic.getId());
        System.out.println(">>> Especialidades asignadas: " + (assigned != null ? assigned.size() : 0));

        // Especialidades disponibles (todas - asignadas)
        List<Medicalspecialty> available = new ArrayList<>(allSpecialties);
        if (assigned != null) {
            available.removeAll(assigned);
        }
        System.out.println(">>> Especialidades disponibles: " + available.size());

        specialtyPickList = new DualListModel<>(available, assigned != null ? assigned : new ArrayList<>());
    }

    public void assignSpecialties() {
        if (selectedMedic == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Seleccione un médico");
            return;
        }

        List<Medicalspecialty> selectedSpecialties = specialtyPickList.getTarget();
        if (selectedSpecialties == null || selectedSpecialties.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Seleccione al menos una especialidad");
            return;
        }

        try {
            Integer userId = getCurrentUserId();

            // Obtener las especialidades actualmente asignadas
            List<Medicalspecialty> currentlyAssigned = specialistService.findSpecialtiesByMedic(selectedMedic.getId());

            // Remover las que ya no están en la lista target
            if (currentlyAssigned != null) {
                for (Medicalspecialty specialty : currentlyAssigned) {
                    if (!selectedSpecialties.contains(specialty)) {
                        specialistService.removeByMedicAndSpecialty(selectedMedic.getId(), specialty.getId(), userId);
                    }
                }
            }

            // Asignar las nuevas
            for (Medicalspecialty specialty : selectedSpecialties) {
                specialistService.assign(selectedMedic.getId(), specialty.getId(), userId);
            }

            // Recargar el modelo
            loadPickListModel();

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Especialidades actualizadas correctamente");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            e.printStackTrace();
        }
    }

    public void searchMedics() {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadMedics();
        } else {
            String term = searchTerm.toLowerCase();
            medics = medicService.findAllActive().stream()
                    .filter(m -> (m.getFirstname() + " " + m.getLastname()).toLowerCase().contains(term)
                    || (m.getDni() != null && m.getDni().contains(term))
                    || (m.getEmail() != null && m.getEmail().toLowerCase().contains(term)))
                    .collect(Collectors.toList());
        }
    }

    private Integer getCurrentUserId() {
        if (userSession != null && userSession.getUser() != null) {
            return userSession.getUser().getId();
        }
        return 1;
    }

    private void addMessage(FacesMessage.Severity severity, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, detail, null));
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ==================== GETTERS Y SETTERS ====================
    public List<Medic> getMedics() {
        return medics;
    }

    public Medic getSelectedMedic() {
        return selectedMedic;
    }

    public void setSelectedMedic(Medic selectedMedic) {
        this.selectedMedic = selectedMedic;
    }

    public DualListModel<Medicalspecialty> getSpecialtyPickList() {
        return specialtyPickList;
    }

    public void setSpecialtyPickList(DualListModel<Medicalspecialty> specialtyPickList) {
        this.specialtyPickList = specialtyPickList;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public int getTotalMedics() {
        return medics != null ? medics.size() : 0;
    }

    public int getTotalAssigned() {
        if (specialtyPickList != null && specialtyPickList.getTarget() != null) {
            return specialtyPickList.getTarget().size();
        }
        return 0;
    }

    public String medicInitials(Medic medic) {
        if (medic == null) {
            return "MD";
        }
        String first = medic.getFirstname() != null && !medic.getFirstname().isEmpty()
                ? medic.getFirstname().substring(0, 1) : "";
        String last = medic.getLastname() != null && !medic.getLastname().isEmpty()
                ? medic.getLastname().substring(0, 1) : "";
        return (first + last).toUpperCase();
    }
}
