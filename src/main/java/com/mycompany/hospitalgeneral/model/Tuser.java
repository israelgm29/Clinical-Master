package com.mycompany.hospitalgeneral.model;

import com.mycompany.hospitalgeneral.services.interfaces.DisplayUser;
import com.mycompany.hospitalgeneral.services.interfaces.ProfileData;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity

@Table(name = "tuser")
@NamedQueries({
    @NamedQuery(name = "Tuser.findAll", query = "SELECT t FROM Tuser t WHERE t.deleted = false OR t.deleted IS NULL"),
    @NamedQuery(name = "Tuser.findById", query = "SELECT t FROM Tuser t WHERE t.id = :id"),
    @NamedQuery(name = "Tuser.findByEmail", query = "SELECT t FROM Tuser t WHERE t.email = :email AND (t.deleted = false OR t.deleted IS NULL)"),
    @NamedQuery(name = "Tuser.findByIsactive", query = "SELECT t FROM Tuser t WHERE t.isactive = :isactive AND (t.deleted = false OR t.deleted IS NULL)")
})
public class Tuser implements Serializable, DisplayUser, ProfileData {

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
    @Size(min = 1, max = 50)
    @Column(name = "email")
    private String email;

    @Size(max = 75)
    @Column(name = "password")
    private String password;

    @Basic(optional = false)
    @NotNull
    @Column(name = "isactive")
    private boolean isactive;

    @Column(name = "emailverified")
    private Boolean emailverified;

    @Size(max = 200)
    @Column(name = "verificationtoken")
    private String verificationtoken;

    @Size(max = 200)
    @Column(name = "passresettoken")
    private String passresettoken;

    // --- RELACIONES ---
    @JoinColumn(name = "profileid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Profile profileid;

    @JoinColumn(name = "roleid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Role roleid;

    @OneToOne(mappedBy = "userid")
    private Medic medic;

    // --- LÓGICA DE AUDITORÍA AUTOMÁTICA ---
    @PrePersist
    protected void onCreate() {
        this.createdat = LocalDateTime.now();
        this.deleted = false;
        // Nota: createdby debe setearse desde el Controller/Service
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.deleted != null && this.deleted) {
            // Si está marcado como eliminado, actualizar fecha de eliminación
            this.deletedat = LocalDateTime.now();
        } else {
            // Actualización normal
            this.editedat = LocalDateTime.now();
        }
    }

    // --- CONSTRUCTORES ---
    public Tuser() {
    }

    public Tuser(Integer id) {
        this.id = id;
    }

    public Tuser(Integer id, LocalDateTime createdat, int createdby, String email, boolean isactive) {
        this.id = id;
        this.createdat = createdat;
        this.createdby = createdby;
        this.email = email;
        this.isactive = isactive;
    }

    // --- GETTERS Y SETTERS ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean getIsactive() {
        return isactive;
    }

    @Override
    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }

    public Boolean getEmailverified() {
        return emailverified;
    }

    public void setEmailverified(Boolean emailverified) {
        this.emailverified = emailverified;
    }

    public String getVerificationtoken() {
        return verificationtoken;
    }

    public void setVerificationtoken(String verificationtoken) {
        this.verificationtoken = verificationtoken;
    }

    public String getPassresettoken() {
        return passresettoken;
    }

    public void setPassresettoken(String passresettoken) {
        this.passresettoken = passresettoken;
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

    public Profile getProfileid() {
        return profileid;
    }

    public void setProfileid(Profile profileid) {
        this.profileid = profileid;
    }

    public Role getRoleid() {
        return roleid;
    }

    public void setRoleid(Role roleid) {
        this.roleid = roleid;
    }

    public Medic getMedic() {
        return medic;
    }

    public void setMedic(Medic medic) {
        this.medic = medic;
    }

    @Override
    public int hashCode() {
        return (id != null ? id.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Tuser)) {
            return false;
        }
        Tuser other = (Tuser) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "Tuser[ id=" + id + ", email=" + email + " ]";
    }

    @Override
    public String getDisplayName() {
        return this.profileid.getFirstname() + this.profileid.getLastname();
    }

    @Override
    public String getRoleName() {
        return this.roleid.getName();
    }

    @Override
    public String getFullName() {
        if (this.profileid != null) {
            return this.profileid.getFirstname() + " " + this.profileid.getLastname();
        }
        return "Usuario sin Perfil"; // O un valor por defecto
    }

    @Override
    public String getFirstName() {
        return this.profileid != null ? this.profileid.getFirstname() : null;
    }

    @Override
    public void setFirstName(String firstName) {
        if (this.profileid != null) {
            this.profileid.setFirstname(firstName);
        }
    }

    @Override
    public String getLastName() {
        return this.profileid != null ? this.profileid.getLastname() : null;
    }

    @Override
    public void setLastName(String lastName) {
        if (this.profileid != null) {
            this.profileid.setLastname(lastName);
        }
    }

    @Override
    public String getDni() {
        return this.profileid != null ? this.profileid.getDni() : null;
    }

    @Override
    public void setDni(String dni) {
        if (this.profileid != null) {
            this.profileid.setDni(dni);
        }
    }

    @Override
    public String getPassport() {
        return this.profileid != null ? this.profileid.getPassport() : null;
    }

    @Override
    public void setPassport(String passport) {
        if (this.profileid != null) {
            this.profileid.setPassport(passport);
        }
    }

    @Override
    public String getTelephone() {
        return this.profileid != null ? this.profileid.getTelephone() : null;
    }

    @Override
    public void setTelephone(String telephone) {
        if (this.profileid != null) {
            this.profileid.setTelephone(telephone);
        }
    }

    @Override
    public String getMobile() {
        return this.profileid != null ? this.profileid.getMobile() : null;
    }

    @Override
    public void setMobile(String mobile) {
        if (this.profileid != null) {
            this.profileid.setMobile(mobile);
        }
    }

    @Override
    public String getAddress() {
        return this.profileid != null ? this.profileid.getAddress() : null;
    }

    @Override
    public void setAddress(String address) {
        if (this.profileid != null) {
            this.profileid.setAddress(address);
        }
    }

    @Override
    public String getImageUrl() {
        return this.profileid != null ? this.profileid.getImage() : null;
    }

    @Override
    public void setImageUrl(String imageUrl) {
        if (this.profileid != null) {
            this.profileid.setImage(imageUrl);
        }
    }

    @Override
    public boolean isMedic() {
        return "Médico".equals(getRoleName()) && this.medic != null;
    }

    @Override
    public String getProfessionalId() {
        if (isMedic() && this.medic != null) {
            return this.medic.getRegprofessional();
        }
        return null;
    }

    @Override
    public void setProfessionalId(String professionalId) {
        if (isMedic() && this.medic != null) {
            this.medic.setRegprofessional(professionalId);
        }
    }

    @Override
    public List<String> getSpecialties() {
        if (isMedic() && this.medic != null) {
            String specialties = this.medic.getSpecialtiesNames();
            if (specialties != null && !specialties.isEmpty()) {
                return Arrays.asList(specialties.split(",\\s*"));
            }
        }
        return Collections.emptyList();
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return this.createdat;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        if (this.editedat != null) {
            return this.editedat;
        }
        if (this.profileid != null && this.profileid.getEditedat() != null) {
            return this.profileid.getEditedat();
        }
        return this.createdat;

    }

    @Override
    public String getInitials() {
        return ProfileData.super.getInitials(); 
    }

    @Override
    public boolean hasProfileImage() {
        return ProfileData.super.hasProfileImage();
    }
}
