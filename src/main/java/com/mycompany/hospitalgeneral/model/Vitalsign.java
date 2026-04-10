package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "vitalsign")
@NamedQueries({
    @NamedQuery(name = "Vitalsign.findAll", query = "SELECT v FROM Vitalsign v WHERE v.deleted = false OR v.deleted IS NULL"),
    @NamedQuery(name = "Vitalsign.findById", query = "SELECT v FROM Vitalsign v WHERE v.id = :id"),
    @NamedQuery(name = "Vitalsign.findByMedicalrecord", query = "SELECT v FROM Vitalsign v WHERE v.medicalrecordid.id = :medicalRecordId AND (v.deleted = false OR v.deleted IS NULL)"),
    @NamedQuery(name = "Vitalsign.findByTemperature", query = "SELECT v FROM Vitalsign v WHERE v.temperature = :temperature"),
    @NamedQuery(name = "Vitalsign.findBySystolicpressure", query = "SELECT v FROM Vitalsign v WHERE v.systolicpressure = :systolicpressure"),
    @NamedQuery(name = "Vitalsign.findByDiastolicpressure", query = "SELECT v FROM Vitalsign v WHERE v.diastolicpressure = :diastolicpressure")
})
public class Vitalsign implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "temperature")
    private String temperature;

    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "systolicpressure")
    private String systolicpressure;

    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "diastolicpressure")
    private String diastolicpressure;

    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "pulse")
    private String pulse;

    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "breathingfrequency")
    private String breathingfrequency;

    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "oxygensaturation")
    private String oxygensaturation;

    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "tall")
    private String tall;

    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "weight")
    private String weight;

    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "mass")
    private String mass;

    // Campos de Auditoría - LocalDateTime
    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdat;

    @Column(name = "createdby", updatable = false)
    private Integer createdby;

    @Column(name = "editedat")
    private LocalDateTime editedat;

    @Column(name = "editedby")
    private Integer editedby;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @Column(name = "deletedat")
    private LocalDateTime deletedat;

    @Column(name = "deletedby")
    private Integer deletedby;

    // Relaciones
    @JoinColumn(name = "medicalrecordid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Medicalrecord medicalrecordid;

    // Lógica de Auditoría
    @PrePersist
    protected void onCreate() {
        this.createdat = LocalDateTime.now();
        this.deleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.deleted != null && this.deleted) {
            this.deletedat = LocalDateTime.now();
        } else {
            this.editedat = LocalDateTime.now();
        }
    }

    public Vitalsign() {
    }

    public Vitalsign(Integer id) {
        this.id = id;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getSystolicpressure() {
        return systolicpressure;
    }

    public void setSystolicpressure(String systolicpressure) {
        this.systolicpressure = systolicpressure;
    }

    public String getDiastolicpressure() {
        return diastolicpressure;
    }

    public void setDiastolicpressure(String diastolicpressure) {
        this.diastolicpressure = diastolicpressure;
    }

    public String getPulse() {
        return pulse;
    }

    public void setPulse(String pulse) {
        this.pulse = pulse;
    }

    public String getBreathingfrequency() {
        return breathingfrequency;
    }

    public void setBreathingfrequency(String breathingfrequency) {
        this.breathingfrequency = breathingfrequency;
    }

    public String getOxygensaturation() {
        return oxygensaturation;
    }

    public void setOxygensaturation(String oxygensaturation) {
        this.oxygensaturation = oxygensaturation;
    }

    public String getTall() {
        return tall;
    }

    public void setTall(String tall) {
        this.tall = tall;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getMass() {
        return mass;
    }

    public void setMass(String mass) {
        this.mass = mass;
    }

    public LocalDateTime getCreatedat() {
        return createdat;
    }

    public void setCreatedat(LocalDateTime createdat) {
        this.createdat = createdat;
    }

    public Integer getCreatedby() {
        return createdby;
    }

    public void setCreatedby(Integer createdby) {
        this.createdby = createdby;
    }

    public LocalDateTime getEditedat() {
        return editedat;
    }

    public void setEditedat(LocalDateTime editedat) {
        this.editedat = editedat;
    }

    public Integer getEditedby() {
        return editedby;
    }

    public void setEditedby(Integer editedby) {
        this.editedby = editedby;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeletedat() {
        return deletedat;
    }

    public void setDeletedat(LocalDateTime deletedat) {
        this.deletedat = deletedat;
    }

    public Integer getDeletedby() {
        return deletedby;
    }

    public void setDeletedby(Integer deletedby) {
        this.deletedby = deletedby;
    }

    public Medicalrecord getMedicalrecordid() {
        return medicalrecordid;
    }

    public void setMedicalrecordid(Medicalrecord medicalrecordid) {
        this.medicalrecordid = medicalrecordid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Vitalsign other = (Vitalsign) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "Vitalsign[ id=" + id + ", temp=" + temperature + "°C ]";
    }
}
