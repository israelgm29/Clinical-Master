package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "permission")
@NamedQueries({
    @NamedQuery(name = "Permission.findAll", query = "SELECT p FROM Permission p WHERE p.deleted = false OR p.deleted IS NULL"),
    @NamedQuery(name = "Permission.findById", query = "SELECT p FROM Permission p WHERE p.id = :id"),
    @NamedQuery(name = "Permission.findByRole", query = "SELECT p FROM Permission p WHERE p.roleid.id = :roleId AND (p.deleted = false OR p.deleted IS NULL)"),
    @NamedQuery(name = "Permission.findByModule", query = "SELECT p FROM Permission p WHERE p.moduleid.id = :moduleId AND (p.deleted = false OR p.deleted IS NULL)"),
    @NamedQuery(name = "Permission.findByRoleAndModule", query = "SELECT p FROM Permission p WHERE p.roleid.id = :roleId AND p.moduleid.id = :moduleId AND (p.deleted = false OR p.deleted IS NULL)")
})
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    // Campos CRUD originales (c, r, u, d) - NO CAMBIAR NOMBRES PARA BD
    @Basic(optional = false)
    @NotNull
    @Column(name = "c")
    private boolean c;

    @Basic(optional = false)
    @NotNull
    @Column(name = "r")
    private boolean r;

    @Basic(optional = false)
    @NotNull
    @Column(name = "u")
    private boolean u;

    @Basic(optional = false)
    @NotNull
    @Column(name = "d")
    private boolean d;

    // Campos de Auditoría con LocalDateTime - MAPEO A TIMESTAMP
    @Basic(optional = false)
    @NotNull
    @Column(name = "createdat")
    private LocalDateTime createdat;

    @Basic(optional = false)
    @NotNull
    @Column(name = "createdby")
    private int createdby;

    @Column(name = "editedat")
    private LocalDateTime editedat;

    @Column(name = "editedby")
    private Integer editedby;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "deletedat")
    private LocalDateTime deletedat;

    @Column(name = "deletedby")
    private Integer deletedby;

    // Relaciones
    @JoinColumn(name = "moduleid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Module moduleid;

    @JoinColumn(name = "roleid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Role roleid;

    // Lógica de Auditoría automática
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

    // Métodos de utilidad con nombres descriptivos
    public boolean hasAnyPermission() {
        return c || r || u || d;
    }

    public void grantAll() {
        this.c = true;
        this.r = true;
        this.u = true;
        this.d = true;
    }

    public void revokeAll() {
        this.c = false;
        this.r = false;
        this.u = false;
        this.d = false;
    }

    // Getters/Setters descriptivos para código (mapean a c, r, u, d)
    public boolean getCanCreate() { return c; }
    public void setCanCreate(boolean canCreate) { this.c = canCreate; }

    public boolean getCanRead() { return r; }
    public void setCanRead(boolean canRead) { this.r = canRead; }

    public boolean getCanUpdate() { return u; }
    public void setCanUpdate(boolean canUpdate) { this.u = canUpdate; }

    public boolean getCanDelete() { return d; }
    public void setCanDelete(boolean canDelete) { this.d = canDelete; }

    // Getters/Setters originales
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean getC() {
        return c;
    }

    public void setC(boolean c) {
        this.c = c;
    }

    public boolean getR() {
        return r;
    }

    public void setR(boolean r) {
        this.r = r;
    }

    public boolean getU() {
        return u;
    }

    public void setU(boolean u) {
        this.u = u;
    }

    public boolean getD() {
        return d;
    }

    public void setD(boolean d) {
        this.d = d;
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

    public Module getModuleid() {
        return moduleid;
    }

    public void setModuleid(Module moduleid) {
        this.moduleid = moduleid;
    }

    public Role getRoleid() {
        return roleid;
    }

    public void setRoleid(Role roleid) {
        this.roleid = roleid;
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
        Permission other = (Permission) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "Permission[" + (moduleid != null ? moduleid.getName() : "null") + 
               ": C=" + c + " R=" + r + " U=" + u + " D=" + d + "]";
    }
}