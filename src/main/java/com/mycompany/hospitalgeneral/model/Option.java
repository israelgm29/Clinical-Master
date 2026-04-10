package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "\"option\"") // Palabra reservada en SQL
@NamedQueries({
    @NamedQuery(name = "Option.findAll", query = "SELECT o FROM Option o WHERE o.deleted = false OR o.deleted IS NULL ORDER BY o.name"),
    @NamedQuery(name = "Option.findById", query = "SELECT o FROM Option o WHERE o.id = :id"),
    @NamedQuery(name = "Option.findByName", query = "SELECT o FROM Option o WHERE o.name = :name AND (o.deleted = false OR o.deleted IS NULL)"),
    @NamedQuery(name = "Option.findByGroup", query = "SELECT o FROM Option o WHERE o.groupid.id = :groupId AND (o.deleted = false OR o.deleted IS NULL) ORDER BY o.name")
})
public class Option implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Size(min = 1, max = 75)
    @Column(name = "name")
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupid", referencedColumnName = "id")
    private Tgroup groupid;

    // Campos de Auditoría - SOLO EN OPTION
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

    // Lógica de Auditoría
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

    public Option() {
    }

    public Option(Integer id) {
        this.id = id;
    }

    // Getters y Setters
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

    public Tgroup getGroupid() {
        return groupid;
    }

    public void setGroupid(Tgroup groupid) {
        this.groupid = groupid;
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
        Option other = (Option) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return name;
    }
}
