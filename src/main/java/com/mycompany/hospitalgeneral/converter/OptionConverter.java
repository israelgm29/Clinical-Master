package com.mycompany.hospitalgeneral.converter;

import com.mycompany.hospitalgeneral.model.Option;
import com.mycompany.hospitalgeneral.services.OptionService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 *
 * @author jhonatan
 */
@Named
@FacesConverter(value = "optionConverter", managed = true)
public class OptionConverter implements Converter<Option> {

    @Inject
    private OptionService optionService; // necesitarás este servicio

    @Override
    public Option getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return optionService.findById(Integer.parseInt(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Option option) {
        if (option == null) {
            return "";
        }
        return String.valueOf(option.getId());
    }
}
