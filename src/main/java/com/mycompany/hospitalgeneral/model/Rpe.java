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
@Table(name = "rpe")
@NamedQueries({
    @NamedQuery(name = "Rpe.findAll", query = "SELECT r FROM Rpe r"),
    @NamedQuery(name = "Rpe.findByCreatedat", query = "SELECT r FROM Rpe r WHERE r.createdat = :createdat"),
    @NamedQuery(name = "Rpe.findByCreatedby", query = "SELECT r FROM Rpe r WHERE r.createdby = :createdby"),
    @NamedQuery(name = "Rpe.findByEditedat", query = "SELECT r FROM Rpe r WHERE r.editedat = :editedat"),
    @NamedQuery(name = "Rpe.findByEditedby", query = "SELECT r FROM Rpe r WHERE r.editedby = :editedby"),
    @NamedQuery(name = "Rpe.findByDeleted", query = "SELECT r FROM Rpe r WHERE r.deleted = :deleted"),
    @NamedQuery(name = "Rpe.findByDeletedat", query = "SELECT r FROM Rpe r WHERE r.deletedat = :deletedat"),
    @NamedQuery(name = "Rpe.findByDeletedby", query = "SELECT r FROM Rpe r WHERE r.deletedby = :deletedby"),
    @NamedQuery(name = "Rpe.findById", query = "SELECT r FROM Rpe r WHERE r.id = :id"),
    @NamedQuery(name = "Rpe.findByHead", query = "SELECT r FROM Rpe r WHERE r.head = :head"),
    @NamedQuery(name = "Rpe.findByNeck", query = "SELECT r FROM Rpe r WHERE r.neck = :neck"),
    @NamedQuery(name = "Rpe.findByChest", query = "SELECT r FROM Rpe r WHERE r.chest = :chest"),
    @NamedQuery(name = "Rpe.findByAbdomen", query = "SELECT r FROM Rpe r WHERE r.abdomen = :abdomen"),
    @NamedQuery(name = "Rpe.findByPelvis", query = "SELECT r FROM Rpe r WHERE r.pelvis = :pelvis"),
    @NamedQuery(name = "Rpe.findByExtremities", query = "SELECT r FROM Rpe r WHERE r.extremities = :extremities"),
    @NamedQuery(name = "Rpe.findByObservations", query = "SELECT r FROM Rpe r WHERE r.observations = :observations")
})
public class Rpe implements Serializable {

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

    // --- Campos de Examen Físico ---
    @Column(name = "head")
    private Boolean head;

    @Column(name = "neck")
    private Boolean neck;

    @Column(name = "chest")
    private Boolean chest;

    @Column(name = "abdomen")
    private Boolean abdomen;

    @Column(name = "pelvis")
    private Boolean pelvis;

    @Column(name = "extremities")
    private Boolean extremities;

    @Size(max = 2147483647)
    @Column(name = "observations")
    private String observations;

    // --- Relaciones ---
    @JoinColumn(name = "medicalrecordid", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Medicalrecord medicalrecordid;

    // --- Callbacks de Ciclo de Vida (Auditoría) ---
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
    public Rpe() {
    }

    public Rpe(Integer id) {
        this.id = id;
    }

    public Rpe(Integer id, LocalDateTime createdat, int createdby) {
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

    public Boolean getHead() {
        return head;
    }

    public void setHead(Boolean head) {
        this.head = head;
    }

    public Boolean getNeck() {
        return neck;
    }

    public void setNeck(Boolean neck) {
        this.neck = neck;
    }

    public Boolean getChest() {
        return chest;
    }

    public void setChest(Boolean chest) {
        this.chest = chest;
    }

    public Boolean getAbdomen() {
        return abdomen;
    }

    public void setAbdomen(Boolean abdomen) {
        this.abdomen = abdomen;
    }

    public Boolean getPelvis() {
        return pelvis;
    }

    public void setPelvis(Boolean pelvis) {
        this.pelvis = pelvis;
    }

    public Boolean getExtremities() {
        return extremities;
    }

    public void setExtremities(Boolean extremities) {
        this.extremities = extremities;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public Medicalrecord getMedicalrecordid() {
        return medicalrecordid;
    }

    public void setMedicalrecordid(Medicalrecord medicalrecordid) {
        this.medicalrecordid = medicalrecordid;
    }

    // --- Métodos de Objeto ---
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Rpe)) {
            return false;
        }
        Rpe other = (Rpe) object;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "com.mycompany.hospitalgeneral.model.Rpe[ id=" + id + " ]";
    }
}
