package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad Antecedent refactorizada con Java Time API y auditoría automática.
 *
 * @author jhonatan
 */
@Entity
@Table(name = "antecedent")
@NamedQueries({
    @NamedQuery(name = "Antecedent.findAll", query = "SELECT a FROM Antecedent a"),
    @NamedQuery(name = "Antecedent.findByCreatedat", query = "SELECT a FROM Antecedent a WHERE a.createdat = :createdat"),
    @NamedQuery(name = "Antecedent.findByCreatedby", query = "SELECT a FROM Antecedent a WHERE a.createdby = :createdby"),
    @NamedQuery(name = "Antecedent.findByEditedat", query = "SELECT a FROM Antecedent a WHERE a.editedat = :editedat"),
    @NamedQuery(name = "Antecedent.findByEditedby", query = "SELECT a FROM Antecedent a WHERE a.editedby = :editedby"),
    @NamedQuery(name = "Antecedent.findByDeleted", query = "SELECT a FROM Antecedent a WHERE a.deleted = :deleted"),
    @NamedQuery(name = "Antecedent.findByDeletedat", query = "SELECT a FROM Antecedent a WHERE a.deletedat = :deletedat"),
    @NamedQuery(name = "Antecedent.findByDeletedby", query = "SELECT a FROM Antecedent a WHERE a.deletedby = :deletedby"),
    @NamedQuery(name = "Antecedent.findById", query = "SELECT a FROM Antecedent a WHERE a.id = :id"),
    @NamedQuery(name = "Antecedent.findByPersonal", query = "SELECT a FROM Antecedent a WHERE a.personal = :personal"),
    @NamedQuery(name = "Antecedent.findBySurgical", query = "SELECT a FROM Antecedent a WHERE a.surgical = :surgical"),
    @NamedQuery(name = "Antecedent.findByFamily", query = "SELECT a FROM Antecedent a WHERE a.family = :family"),
    @NamedQuery(name = "Antecedent.findByProfessional", query = "SELECT a FROM Antecedent a WHERE a.professional = :professional"),
    @NamedQuery(name = "Antecedent.findByHabits", query = "SELECT a FROM Antecedent a WHERE a.habits = :habits"),
    @NamedQuery(name = "Antecedent.findByClinician", query = "SELECT a FROM Antecedent a WHERE a.clinician = :clinician"),
    @NamedQuery(name = "Antecedent.findByTrauma", query = "SELECT a FROM Antecedent a WHERE a.trauma = :trauma"),
    @NamedQuery(name = "Antecedent.findByAllergy", query = "SELECT a FROM Antecedent a WHERE a.allergy = :allergy"),
    @NamedQuery(name = "Antecedent.findByAgo", query = "SELECT a FROM Antecedent a WHERE a.ago = :ago")
})
public class Antecedent implements Serializable {

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

    // --- Campos de Antecedentes ---
    @Size(max = 200)
    @Column(name = "personal")
    private String personal;

    @Size(max = 200)
    @Column(name = "surgical")
    private String surgical;

    @Size(max = 200)
    @Column(name = "family")
    private String family;

    @Size(max = 200)
    @Column(name = "professional")
    private String professional;

    @Size(max = 200)
    @Column(name = "habits")
    private String habits;

    @Size(max = 200)
    @Column(name = "clinician")
    private String clinician;

    @Size(max = 200)
    @Column(name = "trauma")
    private String trauma;

    @Size(max = 200)
    @Column(name = "allergy")
    private String allergy;

    @Size(max = 200)
    @Column(name = "ago")
    private String ago;

    // --- Relaciones ---
    @JoinColumn(name = "patientid", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Patient patientid;

    // --- Ciclo de Vida JPA (Auditoría) ---
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
    public Antecedent() {
    }

    public Antecedent(Integer id) {
        this.id = id;
    }

    public Antecedent(Integer id, LocalDateTime createdat, int createdby) {
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

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }

    public String getSurgical() {
        return surgical;
    }

    public void setSurgical(String surgical) {
        this.surgical = surgical;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getProfessional() {
        return professional;
    }

    public void setProfessional(String professional) {
        this.professional = professional;
    }

    public String getHabits() {
        return habits;
    }

    public void setHabits(String habits) {
        this.habits = habits;
    }

    public String getClinician() {
        return clinician;
    }

    public void setClinician(String clinician) {
        this.clinician = clinician;
    }

    public String getTrauma() {
        return trauma;
    }

    public void setTrauma(String trauma) {
        this.trauma = trauma;
    }

    public String getAllergy() {
        return allergy;
    }

    public void setAllergy(String allergy) {
        this.allergy = allergy;
    }

    public String getAgo() {
        return ago;
    }

    public void setAgo(String ago) {
        this.ago = ago;
    }

    public Patient getPatientid() {
        return patientid;
    }

    public void setPatientid(Patient patientid) {
        this.patientid = patientid;
    }

    // --- Implementaciones Estándar ---
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Antecedent)) {
            return false;
        }
        Antecedent other = (Antecedent) object;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "com.mycompany.hospitalgeneral.model.Antecedent[ id=" + id + " ]";
    }
}
