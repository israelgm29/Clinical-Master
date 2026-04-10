package com.mycompany.hospitalgeneral.converter;

import com.mycompany.hospitalgeneral.model.Tgroup;
import com.mycompany.hospitalgeneral.services.TgroupService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@FacesConverter(value = "tgroupConverter", managed = true)
public class TgroupConverter implements Converter<Tgroup> {

    @Inject
    private TgroupService tgroupService;

    @Override
    public Tgroup getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            return tgroupService.findById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Tgroup value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value.getId());
    }
}