package com.mycompany.hospitalgeneral.converter;

import com.mycompany.hospitalgeneral.model.Role;
import com.mycompany.hospitalgeneral.services.RoleService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@FacesConverter(value = "roleConverter", managed = true)
public class RoleConverter implements Converter<Role> {

    @Inject
    private RoleService roleService;

    @Override
    public Role getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        try {
            Integer id = Integer.valueOf(value);
            return roleService.findById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Role role) {
        if (role == null) {
            return "";
        }
        
        return String.valueOf(role.getId());
    }
}