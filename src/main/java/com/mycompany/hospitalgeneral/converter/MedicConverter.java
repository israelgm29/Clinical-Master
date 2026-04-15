package com.mycompany.hospitalgeneral.converter;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.services.MedicService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author jhonatan
 */
@FacesConverter(value = "medicConverter", managed = true)
public class MedicConverter implements Converter<Medic> {

    @Inject
    private MedicService medicService;

    @Override
    public Medic getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            return medicService.findById(id);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Medic value) {
        
        if (value == null) {
            return "";
        }
        return String.valueOf(value.getId());
    }

}
