package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.services.ThemeService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class ThemeController {

    @Inject
    private ThemeService themeService;

    public String getPrimaryColor() {
        return themeService.getPrimaryColor();
    }

    public String getSecondaryColor() {
        return themeService.getSecondaryColor();
    }

    public String getPrimaryColorRGB() {
        return hexToRGB(themeService.getPrimaryColor());
    }

    public String getSecondaryColorRGB() {
        return hexToRGB(themeService.getSecondaryColor());
    }

    private String hexToRGB(String hex) {
        if (hex == null || !hex.startsWith("#")) return "2, 132, 199";
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return r + ", " + g + ", " + b;
        } catch (Exception e) {
            return "2, 132, 199";
        }
    }
}