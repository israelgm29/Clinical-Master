package com.mycompany.hospitalgeneral.converter;

import com.mycompany.hospitalgeneral.model.Exam;
import com.mycompany.hospitalgeneral.services.ExamService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@FacesConverter(value = "examConverter", managed = true)
public class ExamConverter implements Converter<Exam> {

    @Inject
    private ExamService examService;

    @Override
    public Exam getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            return examService.findById(id);
        } catch (NumberFormatException e) {
            System.out.println("Error converting exam: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Exam value) {
        if (value == null) {
            return "";
        }
        return value.getId().toString();
    }
}