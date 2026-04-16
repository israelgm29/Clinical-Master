package com.mycompany.hospitalgeneral.util;

import com.mycompany.hospitalgeneral.model.Company;
import com.mycompany.hospitalgeneral.services.CompanyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Locale;

/**
 * Bean para información de fecha/hora usando las APIs modernas de Jakarta EE 11
 * ApplicationScoped es óptimo para datos que no cambian por usuario
 */
@Named("appInfo")
@ApplicationScoped
public class ApplicationInfoBean implements Serializable {

    @Inject
    private CompanyService companyService;
    private static final ZoneId HOSPITAL_ZONE = ZoneId.of("America/Guayaquil"); // Ajusta según tu zona

    /**
     * Usa ZonedDateTime para manejo correcto de zonas horarias
     */
    public String getCurrentTime() {
        return ZonedDateTime.now(HOSPITAL_ZONE)
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getCurrentDate() {
        return ZonedDateTime.now(HOSPITAL_ZONE)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getCurrentYear() {
        return String.valueOf(ZonedDateTime.now(HOSPITAL_ZONE).getYear());
    }

    /**
     * Método para obtener fecha formateada con locale específico
     */
    public String getFormattedDateTime(String pattern) {
        return ZonedDateTime.now(HOSPITAL_ZONE)
                .format(DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("es-EC")));
    }

    /*
    Metodo para obtener la informacion del hospital
     */
    public Company getCompanyName() {
        return companyService.findFirst();
    }

    /**
     * Información del sistema para el footer
     */
    public String getJavaVersion() {
        return System.getProperty("java.version");
    }

    public String getJakartaVersion() {
        return "Jakarta EE 11";
    }

    public String getPrimeFacesVersion() {
        return "PrimeFaces 15.0.0";
    }
}
