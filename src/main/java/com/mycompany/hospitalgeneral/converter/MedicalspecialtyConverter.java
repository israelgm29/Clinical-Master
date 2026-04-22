package com.mycompany.hospitalgeneral.converter;

import com.mycompany.hospitalgeneral.model.Medicalspecialty;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Converter para la entidad Medicalspecialty (versión con EntityManager directo).
 * 
 * @author jhonatan
 */
@FacesConverter(value = "medicalspecialtyConverter", managed = true)
public class MedicalspecialtyConverter implements Converter<Medicalspecialty> {

    @PersistenceContext(unitName = "HospitalPU")
    private EntityManager em;

    @Override
    public Medicalspecialty getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            Integer id = Integer.parseInt(value);
            return em.find(Medicalspecialty.class, id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Medicalspecialty value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return value.getId().toString();
    }
}