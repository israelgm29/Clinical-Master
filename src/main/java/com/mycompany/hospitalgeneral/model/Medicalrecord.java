package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "medicalrecord")
@NamedQueries({
    @NamedQuery(name = "Medicalrecord.findAll", query = "SELECT m FROM Medicalrecord m WHERE m.deleted = false OR m.deleted IS NULL"),
    @NamedQuery(name = "Medicalrecord.findById", query = "SELECT m FROM Medicalrecord m WHERE m.id = :id"),
    @NamedQuery(name = "Medicalrecord.findByMedic", query = "SELECT m FROM Medicalrecord m WHERE m.medicid.id = :medicId AND (m.deleted = false OR m.deleted IS NULL)"),
    @NamedQuery(name = "Medicalrecord.findByPatient", query = "SELECT m FROM Medicalrecord m WHERE m.patientid.id = :patientId AND (m.deleted = false OR m.deleted IS NULL)"),
    @NamedQuery(name = "Medicalrecord.findPendingByMedic", query = "SELECT m FROM Medicalrecord m WHERE m.medicid.id = :medicId AND m.done = false AND m.canceled = false AND (m.deleted = false OR m.deleted IS NULL)"),
    @NamedQuery(name = "Medicalrecord.findByDone", query = "SELECT m FROM Medicalrecord m WHERE m.done = :done AND (m.deleted = false OR m.deleted IS NULL)"),
    @NamedQuery(name = "Medicalrecord.findByCanceled", query = "SELECT m FROM Medicalrecord m WHERE m.canceled = :canceled AND (m.deleted = false OR m.deleted IS NULL)"),
    // ✅ CORREGIDO: Eliminadas las named queries con FUNCTION('DATE', ...) - usar JPQL dinámico en el servicio

    // Pacientes en espera: tienen signos vitales pero consulta no realizada
    @NamedQuery(name = "Medicalrecord.findWaitingPatients", query = "SELECT m FROM Medicalrecord m WHERE m.medicid.id = :medicId AND m.done = false AND m.canceled = false AND (m.deleted = false OR m.deleted IS NULL) AND EXISTS (SELECT v FROM Vitalsign v WHERE v.medicalrecordid = m AND (v.deleted = false OR v.deleted IS NULL)) ORDER BY m.createdat ASC"),

    // Contar consultas pendientes por médico
    @NamedQuery(name = "Medicalrecord.countPendingByMedic", query = "SELECT COUNT(m) FROM Medicalrecord m WHERE m.medicid.id = :medicId AND m.done = false AND m.canceled = false AND (m.deleted = false OR m.deleted IS NULL)")

})
public class Medicalrecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @Size(max = 200)
    @Column(name = "reason")
    private String reason;

    @Size(max = 2147483647)
    @Column(name = "currentillness")
    private String currentillness;

    @Column(name = "done")
    private Boolean done = false;

    @Column(name = "canceled")
    private Boolean canceled = false;

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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicalrecordid", fetch = FetchType.EAGER)
    private Collection<Rpe> rpeCollection;

    @JoinColumn(name = "medicid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Medic medicid;

    @JoinColumn(name = "patientid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Patient patientid;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicalrecordid", fetch = FetchType.EAGER)
    private Collection<Vitalsign> vitalsignCollection;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicalrecordid", fetch = FetchType.EAGER)
    private Collection<Medicalexam> medicalexamCollection;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicalrecordid", fetch = FetchType.EAGER)
    private Collection<Diagnostic> diagnosticCollection;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicalrecordid", fetch = FetchType.EAGER)
    private Collection<Cros> crosCollection;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicalrecordid")
    private Collection<Prescription> prescriptionCollection;

    // Lógica de Auditoría
    @PrePersist
    protected void onCreate() {
        this.createdat = LocalDateTime.now();
        this.deleted = false;
        this.done = false;
        this.canceled = false;
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.deleted != null && this.deleted) {
            this.deletedat = LocalDateTime.now();
        } else {
            this.editedat = LocalDateTime.now();
        }
    }

    public Medicalrecord() {
    }

    public Medicalrecord(Integer id) {
        this.id = id;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCurrentillness() {
        return currentillness;
    }

    public void setCurrentillness(String currentillness) {
        this.currentillness = currentillness;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public Boolean getCanceled() {
        return canceled;
    }

    public void setCanceled(Boolean canceled) {
        this.canceled = canceled;
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

    public Collection<Rpe> getRpeCollection() {
        return rpeCollection;
    }

    public void setRpeCollection(Collection<Rpe> rpeCollection) {
        this.rpeCollection = rpeCollection;
    }

    public Medic getMedicid() {
        return medicid;
    }

    public void setMedicid(Medic medicid) {
        this.medicid = medicid;
    }

    public Patient getPatientid() {
        return patientid;
    }

    public void setPatientid(Patient patientid) {
        this.patientid = patientid;
    }

    public Collection<Vitalsign> getVitalsignCollection() {
        return vitalsignCollection;
    }

    public void setVitalsignCollection(Collection<Vitalsign> vitalsignCollection) {
        this.vitalsignCollection = vitalsignCollection;
    }

    public Collection<Medicalexam> getMedicalexamCollection() {
        return medicalexamCollection;
    }

    public void setMedicalexamCollection(Collection<Medicalexam> medicalexamCollection) {
        this.medicalexamCollection = medicalexamCollection;
    }

    public Collection<Diagnostic> getDiagnosticCollection() {
        return diagnosticCollection;
    }

    public void setDiagnosticCollection(Collection<Diagnostic> diagnosticCollection) {
        this.diagnosticCollection = diagnosticCollection;
    }

    public Collection<Cros> getCrosCollection() {
        return crosCollection;
    }

    public void setCrosCollection(Collection<Cros> crosCollection) {
        this.crosCollection = crosCollection;
    }

    public Collection<Prescription> getPrescriptionCollection() {
        return prescriptionCollection;
    }

    public void setPrescriptionCollection(Collection<Prescription> prescriptionCollection) {
        this.prescriptionCollection = prescriptionCollection;
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
        Medicalrecord other = (Medicalrecord) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "Medicalrecord[ id=" + id + ", patient=" + (patientid != null ? patientid.getFirstname() : "null") + " ]";
    }
}
