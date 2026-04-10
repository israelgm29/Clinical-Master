package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author jhonatan
 */
@Entity
@Table(name = "cros")
@NamedQueries({
    @NamedQuery(name = "Cros.findAll", query = "SELECT c FROM Cros c"),
    @NamedQuery(name = "Cros.findByCreatedat", query = "SELECT c FROM Cros c WHERE c.createdat = :createdat"),
    @NamedQuery(name = "Cros.findByCreatedby", query = "SELECT c FROM Cros c WHERE c.createdby = :createdby"),
    @NamedQuery(name = "Cros.findByEditedat", query = "SELECT c FROM Cros c WHERE c.editedat = :editedat"),
    @NamedQuery(name = "Cros.findByEditedby", query = "SELECT c FROM Cros c WHERE c.editedby = :editedby"),
    @NamedQuery(name = "Cros.findByDeleted", query = "SELECT c FROM Cros c WHERE c.deleted = :deleted"),
    @NamedQuery(name = "Cros.findByDeletedat", query = "SELECT c FROM Cros c WHERE c.deletedat = :deletedat"),
    @NamedQuery(name = "Cros.findByDeletedby", query = "SELECT c FROM Cros c WHERE c.deletedby = :deletedby"),
    @NamedQuery(name = "Cros.findById", query = "SELECT c FROM Cros c WHERE c.id = :id"),
    @NamedQuery(name = "Cros.findBySenseorgans", query = "SELECT c FROM Cros c WHERE c.senseorgans = :senseorgans"),
    @NamedQuery(name = "Cros.findByRespiratory", query = "SELECT c FROM Cros c WHERE c.respiratory = :respiratory"),
    @NamedQuery(name = "Cros.findByCardiovascular", query = "SELECT c FROM Cros c WHERE c.cardiovascular = :cardiovascular"),
    @NamedQuery(name = "Cros.findByDigestive", query = "SELECT c FROM Cros c WHERE c.digestive = :digestive"),
    @NamedQuery(name = "Cros.findByGenital", query = "SELECT c FROM Cros c WHERE c.genital = :genital"),
    @NamedQuery(name = "Cros.findByUrinary", query = "SELECT c FROM Cros c WHERE c.urinary = :urinary"),
    @NamedQuery(name = "Cros.findBySkeletalmuscle", query = "SELECT c FROM Cros c WHERE c.skeletalmuscle = :skeletalmuscle"),
    @NamedQuery(name = "Cros.findByEndocrine", query = "SELECT c FROM Cros c WHERE c.endocrine = :endocrine"),
    @NamedQuery(name = "Cros.findByLymphaticheme", query = "SELECT c FROM Cros c WHERE c.lymphaticheme = :lymphaticheme"),
    @NamedQuery(name = "Cros.findByNervous", query = "SELECT c FROM Cros c WHERE c.nervous = :nervous"),
    @NamedQuery(name = "Cros.findByObservations", query = "SELECT c FROM Cros c WHERE c.observations = :observations")
})
public class Cros implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    // --- Auditoría ---

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

    // --- Campos de Diagnóstico ---

    @Column(name = "senseorgans")
    private Boolean senseorgans;

    @Column(name = "respiratory")
    private Boolean respiratory;

    @Column(name = "cardiovascular")
    private Boolean cardiovascular;

    @Column(name = "digestive")
    private Boolean digestive;

    @Column(name = "genital")
    private Boolean genital;

    @Column(name = "urinary")
    private Boolean urinary;

    @Column(name = "skeletalmuscle")
    private Boolean skeletalmuscle;

    @Column(name = "endocrine")
    private Boolean endocrine;

    @Column(name = "lymphaticheme")
    private Boolean lymphaticheme;

    @Column(name = "nervous")
    private Boolean nervous;

    @Size(max = 2147483647)
    @Column(name = "observations")
    private String observations;

    // --- Relaciones ---

    @JoinColumn(name = "medicalrecordid", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Medicalrecord medicalrecordid;

    // --- Auditoría PrePersist y PreUpdate ---

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

    public Cros() {
    }

    public Cros(Integer id) {
        this.id = id;
    }

    public Cros(Integer id, LocalDateTime createdat, int createdby) {
        this.id = id;
        this.createdat = createdat;
        this.createdby = createdby;
    }

    // --- Getters y Setters ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getCreatedat() { return createdat; }
    public void setCreatedat(LocalDateTime createdat) { this.createdat = createdat; }

    public int getCreatedby() { return createdby; }
    public void setCreatedby(int createdby) { this.createdby = createdby; }

    public LocalDateTime getEditedat() { return editedat; }
    public void setEditedat(LocalDateTime editedat) { this.editedat = editedat; }

    public Integer getEditedby() { return editedby; }
    public void setEditedby(Integer editedby) { this.editedby = editedby; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getDeletedat() { return deletedat; }
    public void setDeletedat(LocalDateTime deletedat) { this.deletedat = deletedat; }

    public Integer getDeletedby() { return deletedby; }
    public void setDeletedby(Integer deletedby) { this.deletedby = deletedby; }

    public Boolean getSenseorgans() { return senseorgans; }
    public void setSenseorgans(Boolean senseorgans) { this.senseorgans = senseorgans; }

    public Boolean getRespiratory() { return respiratory; }
    public void setRespiratory(Boolean respiratory) { this.respiratory = respiratory; }

    public Boolean getCardiovascular() { return cardiovascular; }
    public void setCardiovascular(Boolean cardiovascular) { this.cardiovascular = cardiovascular; }

    public Boolean getDigestive() { return digestive; }
    public void setDigestive(Boolean digestive) { this.digestive = digestive; }

    public Boolean getGenital() { return genital; }
    public void setGenital(Boolean genital) { this.genital = genital; }

    public Boolean getUrinary() { return urinary; }
    public void setUrinary(Boolean urinary) { this.urinary = urinary; }

    public Boolean getSkeletalmuscle() { return skeletalmuscle; }
    public void setSkeletalmuscle(Boolean skeletalmuscle) { this.skeletalmuscle = skeletalmuscle; }

    public Boolean getEndocrine() { return endocrine; }
    public void setEndocrine(Boolean endocrine) { this.endocrine = endocrine; }

    public Boolean getLymphaticheme() { return lymphaticheme; }
    public void setLymphaticheme(Boolean lymphaticheme) { this.lymphaticheme = lymphaticheme; }

    public Boolean getNervous() { return nervous; }
    public void setNervous(Boolean nervous) { this.nervous = nervous; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public Medicalrecord getMedicalrecordid() { return medicalrecordid; }
    public void setMedicalrecordid(Medicalrecord medicalrecordid) { this.medicalrecordid = medicalrecordid; }

    // --- Métodos Reemplazados (Standard) ---

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Cros)) return false;
        Cros other = (Cros) object;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "com.mycompany.hospitalgeneral.model.Cros[ id=" + id + " ]";
    }
}