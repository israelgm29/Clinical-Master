package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Exam;
import com.mycompany.hospitalgeneral.model.Examtype;
import com.mycompany.hospitalgeneral.services.ExamService;
import com.mycompany.hospitalgeneral.services.ExamtypeService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class ExamController implements Serializable {

    @Inject
    private ExamService examService;

    @Inject
    private ExamtypeService examtypeService;

    private List<Exam> items;
    private List<Examtype> examtypes;
    private Exam selected;
    private boolean editMode;

    private Integer filterExamtypeId;
    private List<Exam> filteredItems;
    private Examtype selectedExamtype; // Tipo seleccionado actualmente
    private String selectedExamtypeName; // Nombre para mostrar en header

    private static final Integer DEFAULT_USER_ID = 1;

    @PostConstruct
    public void init() {
        loadItems();
        loadExamtypes();
        prepareCreate();
    }

    public void loadItems() {
        items = examService.findAll();
        filteredItems = items;
    }

    public void loadExamtypes() {
        examtypes = examtypeService.findAll();
    }

    public void filterByExamtype() {
        if (filterExamtypeId != null) {
            filteredItems = examService.findByExamtype(filterExamtypeId);
        } else {
            filteredItems = items;
        }
    }

    public void prepareCreate() {
        this.selected = new Exam();
        this.editMode = false;
    }

    public void prepareEdit(Exam exam) {
        this.selected = exam;
        this.editMode = true;
    }

    public void save() {
        try {
            if (selected.getName() == null || selected.getName().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "El nombre del examen es obligatorio"));
                return;
            }

            if (selected.getExamtypeid() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe seleccionar un tipo de examen"));
                return;
            }

            examService.save(selected, DEFAULT_USER_ID);

            loadItems();

            String msg = editMode ? "Examen actualizado" : "Examen creado";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg));

            PrimeFaces.current().resetInputs(":dialogs:manage-exam-content");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar: " + e.getMessage()));
        }
    }

    public void delete(Exam exam) {
        try {
            examService.delete(exam.getId(), DEFAULT_USER_ID);
            loadItems();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Examen eliminado"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar: " + e.getMessage()));
        }
    }

    public void onExamtypeChange() {
        if (filterExamtypeId != null) {
            selectedExamtype = examtypes.stream()
                    .filter(et -> et.getId().equals(filterExamtypeId))
                    .findFirst()
                    .orElse(null);
            selectedExamtypeName = selectedExamtype != null ? selectedExamtype.getName() : "";
            filterByExamtype();
        }
    }

    // Getters y Setters
    public List<Exam> getItems() {
        return items;
    }

    public List<Exam> getFilteredItems() {
        return filteredItems;
    }

    public List<Examtype> getExamtypes() {
        return examtypes;
    }

    public Exam getSelected() {
        return selected;
    }

    public void setSelected(Exam selected) {
        this.selected = selected;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public Integer getFilterExamtypeId() {
        return filterExamtypeId;
    }

    public void setFilterExamtypeId(Integer filterExamtypeId) {
        this.filterExamtypeId = filterExamtypeId;
    }

    public void selectExamtype(Examtype examtype) {
        this.selectedExamtype = examtype;
        this.filterExamtypeId = examtype.getId();
        this.selectedExamtypeName = examtype.getName();
        filterByExamtype();
    }

    public Examtype getSelectedExamtype() {
        return selectedExamtype;
    }

    public void setSelectedExamtype(Examtype selectedExamtype) {
        this.selectedExamtype = selectedExamtype;
    }

    public String getSelectedExamtypeName() {
        return selectedExamtypeName;
    }

}
