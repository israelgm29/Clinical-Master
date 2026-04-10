package com.mycompany.hospitalgeneral.converter;

import com.mycompany.hospitalgeneral.model.Examtype;
import com.mycompany.hospitalgeneral.services.ExamtypeService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@FacesConverter(value = "examtypeConverter", managed = true)
public class ExamtypeConverter implements Converter<Examtype> {

    @Inject
    private ExamtypeService examtypeService;

    @Override
    public Examtype getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            return examtypeService.findById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Examtype value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value.getId());
    }
}