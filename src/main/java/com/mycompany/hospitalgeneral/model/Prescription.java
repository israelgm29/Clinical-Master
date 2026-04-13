package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "prescription")
@NamedQueries({
    @NamedQuery(name = "Prescription.findByMedicalRecord",
            query = "SELECT p FROM Prescription p "
            + "WHERE p.medicalrecordid.id = :recordId "
            + "AND (p.deleted = false OR p.deleted IS NULL) "
            + "ORDER BY p.createdat ASC")
})
public class Prescription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @JoinColumn(name = "medicalrecordid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Medicalrecord medicalrecordid;

    @NotBlank(message = "El medicamento es obligatorio")
    @Size(max = 200)
    @Column(name = "medication", nullable = false)
    private String medication;

    @Size(max = 100)
    @Column(name = "dose")
    private String dose;

    @Size(max = 100)
    @Column(name = "frequency")
    private String frequency;

    @Size(max = 100)
    @Column(name = "duration")
    private String duration;

    @Size(max = 100)
    @Column(name = "route")
    private String route;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    // === AUDITORÍA ===
    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdat;

    @Column(name = "createdby", updatable = false)
    private Integer createdby;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @Column(name = "deletedat")
    private LocalDateTime deletedat;

    @Column(name = "deletedby")
    private Integer deletedby;

    @PrePersist
    protected void onCreate() {
        this.createdat = LocalDateTime.now();
        this.deleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.deleted != null && this.deleted) {
            this.deletedat = LocalDateTime.now();
        }
    }

    public Prescription() {
    }

    public Prescription(Integer id) {
        this.id = id;
    }

    // === GETTERS Y SETTERS ===
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Medicalrecord getMedicalrecordid() {
        return medicalrecordid;
    }

    public void setMedicalrecordid(Medicalrecord medicalrecordid) {
        this.medicalrecordid = medicalrecordid;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
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
        Prescription other = (Prescription) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "Prescription[ id=" + id + ", medication=" + medication + " ]";
    }
}
