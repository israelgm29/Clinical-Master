package com.mycompany.hospitalgeneral.converter;

import com.mycompany.hospitalgeneral.model.Disease;
import com.mycompany.hospitalgeneral.services.DiseaseService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@FacesConverter(value = "diseaseConverter", managed = true)
public class DiseaseConverter implements Converter<Disease> {

    @Inject
    private DiseaseService diseaseService;

    @Override
    public Disease getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            return diseaseService.findById(id);
        } catch (NumberFormatException e) {
            System.out.println("Error converting disease: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Disease value) {
        if (value == null) {
            return "";
        }
        return value.getId().toString();
    }
}