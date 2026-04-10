package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.Basic;
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
@Table(name = "disease")
@NamedQueries({
    @NamedQuery(name = "Disease.findAll", query = "SELECT d FROM Disease d WHERE d.deleted = false OR d.deleted IS NULL ORDER BY d.name"),
    @NamedQuery(name = "Disease.findById", query = "SELECT d FROM Disease d WHERE d.id = :id"),
    @NamedQuery(name = "Disease.findByCode", query = "SELECT d FROM Disease d WHERE d.code = :code AND (d.deleted = false OR d.deleted IS NULL)"),
    @NamedQuery(name = "Disease.findByName", query = "SELECT d FROM Disease d WHERE d.name LIKE :name AND (d.deleted = false OR d.deleted IS NULL) ORDER BY d.name"),
    @NamedQuery(name = "Disease.findByDiseasetype", query = "SELECT d FROM Disease d WHERE d.diseasetypeid.id = :typeId AND (d.deleted = false OR d.deleted IS NULL) ORDER BY d.name"),
    @NamedQuery(name = "Disease.search", query = "SELECT d FROM Disease d WHERE (d.code LIKE :search OR d.name LIKE :search) AND (d.deleted = false OR d.deleted IS NULL) ORDER BY d.name")
})
public class Disease implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- CAMPOS DE AUDITORÍA ---
    @Basic(optional = false)
    @NotNull
    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdat;

    @Basic(optional = false)
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

    @Size(max = 10)
    @Column(name = "code")
    private String code; // Código CIE-10

    @Size(max = 2147483647)
    @Column(name = "name")
    private String name;

    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;

    @Size(max = 2147483647)
    @Column(name = "actions")
    private String actions; // Acciones terapéuticas

    // --- RELACIONES ---
    @JoinColumn(name = "diseasetypeid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Diseasetype diseasetypeid;

    @OneToMany(mappedBy = "diseaseid")
    private Collection<Diagnostic> diagnosticCollection;

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

    public Disease() {
    }

    public Disease(Integer id) {
        this.id = id;
    }

    // --- GETTERS Y SETTERS ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
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

    public Diseasetype getDiseasetypeid() {
        return diseasetypeid;
    }

    public void setDiseasetypeid(Diseasetype diseasetypeid) {
        this.diseasetypeid = diseasetypeid;
    }

    public Collection<Diagnostic> getDiagnosticCollection() {
        return diagnosticCollection;
    }

    public void setDiagnosticCollection(Collection<Diagnostic> diagnosticCollection) {
        this.diagnosticCollection = diagnosticCollection;
    }

    @Override
    public int hashCode() {
        return (id != null ? id.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Disease)) {
            return false;
        }
        Disease other = (Disease) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "Disease[ id=" + id + ", code=" + code + ", name=" + name + " ]";
    }
}