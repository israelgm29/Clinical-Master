package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

/**
 *
 * @author jhonatan
 */
@Entity
@Table(name = "medicalspecialty")
@NamedQueries({
    @NamedQuery(name = "Medicalspecialty.findAll", query = "SELECT m FROM Medicalspecialty m"),
    @NamedQuery(name = "Medicalspecialty.findById", query = "SELECT m FROM Medicalspecialty m WHERE m.id = :id"),
    @NamedQuery(name = "Medicalspecialty.findByName", query = "SELECT m FROM Medicalspecialty m WHERE m.name = :name")
})
public class Medicalspecialty implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    // --- Campos de Auditoría Automática ---
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

    // --- Campos de Negocio ---
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 75)
    @Column(name = "name")
    private String name;

    @Size(max = 150)
    @Column(name = "description")
    private String description;

    // --- Relaciones ---
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicalspecialtyid", fetch = FetchType.LAZY)
    private Collection<Specialist> specialistCollection;

    // --- Lifecycle Callbacks (Auditoría) ---
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
    public Medicalspecialty() {
    }

    public Medicalspecialty(Integer id) {
        this.id = id;
    }

    public Medicalspecialty(Integer id, LocalDateTime createdat, int createdby, String name) {
        this.id = id;
        this.createdat = createdat;
        this.createdby = createdby;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<Specialist> getSpecialistCollection() {
        return specialistCollection;
    }

    public void setSpecialistCollection(Collection<Specialist> specialistCollection) {
        this.specialistCollection = specialistCollection;
    }

    // --- Métodos Estándar ---
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Medicalspecialty)) {
            return false;
        }
        Medicalspecialty other = (Medicalspecialty) object;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "com.mycompany.hospitalgeneral.model.Medicalspecialty[ id=" + id + " ]";
    }
}
