package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Company;
import com.mycompany.hospitalgeneral.services.CompanyService;
import com.mycompany.hospitalgeneral.services.ThemeService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class CompanyController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CompanyService companyService;
    
    @Inject
    private ThemeService themeService;

    private Company company;

    @PostConstruct
    public void init() {
        company = companyService.findFirst();
        if (company == null) {
            company = new Company();
            company.setName("Hospital General");
            company.setPrimarycolor("#0284c7");
            company.setSecondarycolor("#64748b");
        }
    }

    public void save() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        try {
            // Validaciones básicas
            if (company.getName() == null || company.getName().trim().isEmpty()) {
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El nombre del hospital es obligatorio"));
                return;
            }

            // Guardar
            companyService.save(company);
            
            // Recargar colores
            themeService.refreshColors();
            
            // Mensaje de éxito
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Configuración guardada correctamente"));
            
            // Actualizar growl y recargar página después de 1.5 segundos
            PrimeFaces.current().ajax().update("form:messages");
            PrimeFaces.current().executeScript("setTimeout(function(){ location.reload(); }, 1500);");
            
        } catch (Exception e) {
            e.printStackTrace(); // Para ver error en consola del servidor
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar: " + e.getMessage()));
            PrimeFaces.current().ajax().update("form:messages");
        }
    }

    public void previewColors() {
        PrimeFaces.current().executeScript(
            "document.documentElement.style.setProperty('--primary-color', '" + company.getPrimarycolor() + "');" +
            "document.documentElement.style.setProperty('--secondary-color', '" + company.getSecondarycolor() + "');"
        );
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}