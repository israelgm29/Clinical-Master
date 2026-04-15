package com.mycompany.hospitalgeneral.session;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Patient;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class ConsultationContext implements Serializable {

    private Medicalrecord currentMedicalRecord;
    private Patient currentPatient;
    private Medic currentMedic;

    public boolean hasActiveConsultation() {
        return currentMedicalRecord != null;
    }

    public void clear() {
        currentMedicalRecord = null;
        currentPatient = null;
    }

    // Getters y Setters
    public Medicalrecord getCurrentMedicalRecord() {
        return currentMedicalRecord;
    }

    public void setCurrentMedicalRecord(Medicalrecord currentMedicalRecord) {
        this.currentMedicalRecord = currentMedicalRecord;
    }

    public Patient getCurrentPatient() {
        return currentPatient;
    }

    public void setCurrentPatient(Patient currentPatient) {
        this.currentPatient = currentPatient;
    }

    public Medic getCurrentMedic() {
        return currentMedic;
    }

    public void setCurrentMedic(Medic currentMedic) {
        this.currentMedic = currentMedic;
    }
    
}
