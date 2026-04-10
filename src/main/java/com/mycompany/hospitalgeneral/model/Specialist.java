package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad Specialist refactorizada con LocalDateTime y auditoría automática.
 *
 * @author jhonatan
 */
@Entity
@Table(name = "specialist")
@NamedQueries({
    @NamedQuery(name = "Specialist.findAll", query = "SELECT s FROM Specialist s"),
    @NamedQuery(name = "Specialist.findById", query = "SELECT s FROM Specialist s WHERE s.id = :id"),
    @NamedQuery(name = "Specialist.findByCreatedat", query = "SELECT s FROM Specialist s WHERE s.createdat = :createdat")
})
public class Specialist implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    // --- Auditoría Automática ---
    @Basic(optional = false)
    @NotNull
    @Column(name = "createdat", nullable = false, updatable = false)
    private LocalDateTime createdat;

    @Basic(optional = false)
    @NotNull
    @Column(name = "createdby", nullable = false, updatable = false)
    private int createdby;

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
    @JoinColumn(name = "medicid", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Medic medicid;

    @JoinColumn(name = "medicalspecialtyid", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Medicalspecialty medicalspecialtyid;

    // --- Lifecycle Callbacks ---
    @PrePersist
    protected void onCreate() {
        this.createdat = LocalDateTime.now();
        if (this.deleted == null) {
            this.deleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.editedat = LocalDateTime.now();
    }

    // --- Constructores ---
    public Specialist() {
    }

    public Specialist(Integer id) {
        this.id = id;
    }

    public Specialist(Integer id, LocalDateTime createdat, int createdby) {
        this.id = id;
        this.createdat = createdat;
        this.createdby = createdby;
    }

    // --- Getters y Setters ---
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

    public int getCreatedby() {
        return createdby;
    }

    public void setCreatedby(int createdby) {
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

    public Medic getMedicid() {
        return medicid;
    }

    public void setMedicid(Medic medicid) {
        this.medicid = medicid;
    }

    public Medicalspecialty getMedicalspecialtyid() {
        return medicalspecialtyid;
    }

    public void setMedicalspecialtyid(Medicalspecialty medicalspecialtyid) {
        this.medicalspecialtyid = medicalspecialtyid;
    }

    // --- Métodos Equals/Hash/String ---
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Specialist)) {
            return false;
        }
        Specialist other = (Specialist) object;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "com.mycompany.hospitalgeneral.model.Specialist[ id=" + id + " ]";
    }
}
