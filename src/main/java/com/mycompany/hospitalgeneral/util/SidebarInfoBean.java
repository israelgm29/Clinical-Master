package com.mycompany.hospitalgeneral.util;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Named("sidebarInfo")
@RequestScoped
public class SidebarInfoBean implements Serializable {
    
    private static final ZoneId HOSPITAL_ZONE = ZoneId.systemDefault();
    
    public String getTime() {
        return LocalDateTime.now(HOSPITAL_ZONE)
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    public String getDate() {
        return LocalDateTime.now(HOSPITAL_ZONE)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    public String getYear() {
        return String.valueOf(LocalDateTime.now(HOSPITAL_ZONE).getYear());
    }
    
    public int getActivePatientsCount() {
        // TODO: Implementar lógica real desde la base de datos
        return 42; // Valor de ejemplo
    }
}