package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Company;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Singleton
// @Startup eliminado - ya no fuerza inicialización al arrancar
public class ThemeService {

    @Inject
    private CompanyService companyService;

    private Map<String, String> colors = new HashMap<>();
    private boolean loaded = false;

    private void ensureLoaded() {
        if (!loaded) {
            loadColors();
        }
    }

    public void loadColors() {
        // Defaults siempre primero
        colors.put("primary", "#0284c7");
        colors.put("secondary", "#64748b");

        try {
            Company company = companyService.findFirst();
            if (company != null) {
                if (company.getPrimarycolor() != null) colors.put("primary", company.getPrimarycolor());
                if (company.getSecondarycolor() != null) colors.put("secondary", company.getSecondarycolor());
            }
            loaded = true;
        } catch (Exception e) {
            System.out.println("[ThemeService] Usando colores por defecto: " + e.getMessage());
        }
    }

    public String getPrimaryColor() {
        ensureLoaded();
        return colors.getOrDefault("primary", "#0284c7");
    }

    public String getSecondaryColor() {
        ensureLoaded();
        return colors.getOrDefault("secondary", "#64748b");
    }

    public void refreshColors() {
        loaded = false;
        loadColors();
    }
}