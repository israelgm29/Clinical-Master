package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author jhonatan
 */
@Entity
@Table(name = "diagnostic")
@NamedQueries({
    @NamedQuery(name = "Diagnostic.findAll", query = "SELECT d FROM Diagnostic d"),
    @NamedQuery(name = "Diagnostic.findById", query = "SELECT d FROM Diagnostic d WHERE d.id = :id")
})
public class Diagnostic implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // --- Campos de Auditoría ---
    @NotNull
    @Column(name = "createdat", nullable = false, updatable = false)
    private LocalDateTime createdat;

    @NotNull
    @Column(name = "createdby", nullable = false, updatable = false)
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

    // --- Relaciones ---
    @JoinColumn(name = "diseaseid", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Disease diseaseid;

    @JoinColumn(name = "medicalrecordid", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Medicalrecord medicalrecordid;

    // --- Lifecycle Callbacks (Auditoría Automática) ---
    @PrePersist
    protected void onCreate() {
        this.createdat = LocalDateTime.now();
        this.deleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.editedat = LocalDateTime.now();
    }

    // --- Constructores ---
    public Diagnostic() {
    }

    public Diagnostic(Integer id) {
        this.id = id;
    }

    // --- Getters y Setters ---
    // (Omitidos por brevedad, pero iguales reemplazando Date por LocalDateTime)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Disease getDiseaseid() {
        return diseaseid;
    }

    public void setDiseaseid(Disease diseaseid) {
        this.diseaseid = diseaseid;
    }

    public Medicalrecord getMedicalrecordid() {
        return medicalrecordid;
    }

    public void setMedicalrecordid(Medicalrecord medicalrecordid) {
        this.medicalrecordid = medicalrecordid;
    }

    // --- Equals & HashCode Modernos ---
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Diagnostic)) {
            return false;
        }
        Diagnostic that = (Diagnostic) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Diagnostic[ id=" + id + " ]";
    }
}
