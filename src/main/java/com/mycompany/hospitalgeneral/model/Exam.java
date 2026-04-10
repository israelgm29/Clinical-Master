package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Table(name = "exam")
@NamedQueries({
    @NamedQuery(name = "Exam.findAll", query = "SELECT e FROM Exam e WHERE e.deleted = false OR e.deleted IS NULL ORDER BY e.name"),
    @NamedQuery(name = "Exam.findById", query = "SELECT e FROM Exam e WHERE e.id = :id"),
    @NamedQuery(name = "Exam.findByName", query = "SELECT e FROM Exam e WHERE e.name = :name AND (e.deleted = false OR e.deleted IS NULL)"),
    @NamedQuery(name = "Exam.findByExamtype", query = "SELECT e FROM Exam e WHERE e.examtypeid.id = :examtypeId AND (e.deleted = false OR e.deleted IS NULL) ORDER BY e.name")
})
public class Exam implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- CAMPOS DE AUDITORÍA ---
    @NotNull
    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdat;

    @NotNull
    @Column(name = "createdby", updatable = false)
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

    // --- CAMPOS PRINCIPALES ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "name")
    private String name;

    // --- RELACIONES ---
    @JoinColumn(name = "examtypeid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Examtype examtypeid;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "examid")
    private Collection<Medicalexam> medicalexamCollection;

    // --- LÓGICA DE AUDITORÍA ---
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

    public Exam() {
    }

    public Exam(Integer id) {
        this.id = id;
    }

    // --- GETTERS Y SETTERS ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Examtype getExamtypeid() {
        return examtypeid;
    }

    public void setExamtypeid(Examtype examtypeid) {
        this.examtypeid = examtypeid;
    }

    public Collection<Medicalexam> getMedicalexamCollection() {
        return medicalexamCollection;
    }

    public void setMedicalexamCollection(Collection<Medicalexam> medicalexamCollection) {
        this.medicalexamCollection = medicalexamCollection;
    }

    @Override
    public int hashCode() {
        return (id != null ? id.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Exam)) {
            return false;
        }
        Exam other = (Exam) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "Exam[ id=" + id + ", name=" + name + " ]";
    }
}