package com.mycompany.hospitalgeneral.converter;

import com.mycompany.hospitalgeneral.model.Module;
import com.mycompany.hospitalgeneral.services.ModuleService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@FacesConverter(value = "moduleConverter", managed = true)
public class ModuleConverter implements Converter<Module> {

    @Inject
    private ModuleService moduleService;

    @Override
    public Module getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            Integer id = Integer.valueOf(value);
            return moduleService.findById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Module module) {
        if (module == null) {
            return "";
        }

        return String.valueOf(module.getId());
    }
}
