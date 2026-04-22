package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Disease;
import com.mycompany.hospitalgeneral.model.Diseasetype;
import com.mycompany.hospitalgeneral.services.DiseaseService;
import com.mycompany.hospitalgeneral.services.DiseasetypeService;
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
public class DiseaseController implements Serializable {

    @Inject
    private DiseaseService diseaseService;

    @Inject
    private DiseasetypeService diseasetypeService;

    private List<Disease> items;
    private List<Diseasetype> diseasetypes;
    private Disease selected;
    private boolean editMode = false;

    // Filtros de búsqueda avanzada
    private String searchTerm;
    private Integer filterTypeId;
    private String filterCodePrefix;

    private static final Integer DEFAULT_USER_ID = 1;

    @PostConstruct
    public void init() {
        loadItems();
        loadDiseasetypes();
        prepareCreate();
    }

    public void loadItems() {
        items = diseaseService.findAll();
    }

    public void loadDiseasetypes() {
        diseasetypes = diseasetypeService.findAll();
    }

    /**
     * Búsqueda rápida (código o nombre)
     */
    public void search() {
        items = diseaseService.search(searchTerm);
    }

    /**
     * Búsqueda avanzada (múltiples filtros)
     */
    public void searchAdvanced() {
        items = diseaseService.searchAdvanced(searchTerm, filterTypeId, filterCodePrefix);
    }

    /**
     * Limpiar filtros
     */
    public void clearFilters() {
        searchTerm = null;
        filterTypeId = null;
        filterCodePrefix = null;
        loadItems();
    }

    public void prepareCreate() {
        this.selected = new Disease();
        this.editMode = false;
        System.out.println(editMode);
    }

    public void prepareEdit(Disease disease) {
        this.selected = disease;
        this.editMode = true;
        System.out.println(editMode);
    }

    public void save() {
        try {
            // Validaciones
            if (selected.getName() == null || selected.getName().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "El nombre es obligatorio"));
                return;
            }

            if (selected.getDiseasetypeid() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe seleccionar un tipo de enfermedad"));
                return;
            }

            diseaseService.save(selected, DEFAULT_USER_ID);
            loadItems();

            String msg = editMode ? "Enfermedad actualizada" : "Enfermedad creada";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg));

            PrimeFaces.current().resetInputs(":dialogs:manage-disease-content");

        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void delete(Disease disease) {
        try {
            diseaseService.delete(disease.getId(), DEFAULT_USER_ID);
            loadItems();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Enfermedad eliminada"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    /**
     * Total de enfermedades en el catálogo
     * @return 
     */
    public int getTotalDiseases() {
        return items != null ? items.size() : 0;
    }

    /**
     * Total de tipos de enfermedades
     * @return 
     */
    public int getTotalTypes() {
        return diseasetypes != null ? diseasetypes.size() : 0;
    }

    /**
     * Categorías CIE-10 únicas (primera letra del código)
     * @return 
     */
    public int getCieCategories() {
        if (items == null) {
            return 0;
        }
        return (int) items.stream()
                .filter(d -> d.getCode() != null && !d.getCode().isEmpty())
                .map(d -> d.getCode().substring(0, 1).toUpperCase())
                .distinct()
                .count();
    }

    // Getters y Setters
    public List<Disease> getItems() {
        return items;
    }

    public List<Diseasetype> getDiseasetypes() {
        return diseasetypes;
    }

    public Disease getSelected() {
        return selected;
    }

    public void setSelected(Disease selected) {
        this.selected = selected;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Integer getFilterTypeId() {
        return filterTypeId;
    }

    public void setFilterTypeId(Integer filterTypeId) {
        this.filterTypeId = filterTypeId;
    }

    public String getFilterCodePrefix() {
        return filterCodePrefix;
    }

    public void setFilterCodePrefix(String filterCodePrefix) {
        this.filterCodePrefix = filterCodePrefix;
    }
}
