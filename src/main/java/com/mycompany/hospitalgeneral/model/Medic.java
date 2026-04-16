package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;
import jakarta.persistence.Transient;

/**
 * @author Jhonatan Montenegro
 */
@Entity
@Table(name = "medic")
@NamedQueries({
    // 🔹 Buscar por ID
    @NamedQuery(
            name = "Medic.findById",
            query = "SELECT m FROM Medic m WHERE m.id = :id AND (m.deleted = false OR m.deleted IS NULL)"
    ),

    // 🔹 Buscar por usuario (🔥 ESTA ES LA CLAVE PARA TU BUG)
    @NamedQuery(
            name = "Medic.findByUserId",
            query = "SELECT m FROM Medic m WHERE m.userid.id = :userId AND (m.deleted = false OR m.deleted IS NULL)"
    ),

    // 🔹 Listar todos activos
    @NamedQuery(
            name = "Medic.findAllActive",
            query = "SELECT m FROM Medic m WHERE (m.deleted = false OR m.deleted IS NULL)"
    ),

    // 🔹 Buscar por DNI
    @NamedQuery(
            name = "Medic.findByDni",
            query = "SELECT m FROM Medic m WHERE m.dni = :dni AND (m.deleted = false OR m.deleted IS NULL)"
    ),

    // 🔹 Buscar por email
    @NamedQuery(
            name = "Medic.findByEmail",
            query = "SELECT m FROM Medic m WHERE m.email = :email AND (m.deleted = false OR m.deleted IS NULL)"
    ),

    // 🔹 Búsqueda por nombre (para filtros en UI)
    @NamedQuery(
            name = "Medic.searchByName",
            query = "SELECT m FROM Medic m WHERE "
            + "(LOWER(m.firstname) LIKE LOWER(CONCAT('%', :text, '%')) "
            + "OR LOWER(m.lastname) LIKE LOWER(CONCAT('%', :text, '%'))) "
            + "AND (m.deleted = false OR m.deleted IS NULL)"
    )

})
public class Medic implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Size(max = 10)
    @Column(name = "dni", unique = true)
    private String dni;

    @Size(max = 13)
    @Column(name = "passport")
    private String passport;

    @NotNull
    @Size(min = 1, max = 25)
    @Column(name = "lastname", nullable = false)
    private String lastname;

    @NotNull
    @Size(min = 1, max = 25)
    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Size(max = 75)
    @Column(name = "image")
    private String image;

    @Size(max = 13)
    @Column(name = "telephone")
    private String telephone;

    @Size(max = 13)
    @Column(name = "mobile")
    private String mobile;

    @Email
    @Size(max = 50)
    @Column(name = "email")
    private String email;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "address", nullable = false)
    private String address;

    @Size(max = 15)
    @Column(name = "regprofessional")
    private String regprofessional;

    // --- CAMPOS DE AUDITORÍA (Mantenidos y actualizados) ---
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

    // --- RELACIONES IMPORTANTES ---
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", referencedColumnName = "id")
    private Tuser userid;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicid")
    private List<Medicalrecord> medicalrecordList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicid")
    private List<Specialist> specialistList;

    // --- LÓGICA DE AUDITORÍA AUTOMÁTICA ---
    @PrePersist
    protected void onCreate() {
        this.createdat = LocalDateTime.now();
        this.deleted = false;
        // Nota: createdby debería setearse desde el Controller/Service con el usuario en sesión
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.deleted != null && this.deleted) {
            this.deletedat = LocalDateTime.now();
        } else {
            this.editedat = LocalDateTime.now();
        }
    }

    public Medic() {
    }

    // --- GETTERS Y SETTERS ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegprofessional() {
        return regprofessional;
    }

    public void setRegprofessional(String regprofessional) {
        this.regprofessional = regprofessional;
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

    public Tuser getUserid() {
        return userid;
    }

    public void setUserid(Tuser userid) {
        this.userid = userid;
    }

    public List<Medicalrecord> getMedicalrecordList() {
        return medicalrecordList;
    }

    public void setMedicalrecordList(List<Medicalrecord> medicalrecordList) {
        this.medicalrecordList = medicalrecordList;
    }

    public List<Specialist> getSpecialistList() {
        return specialistList;
    }

    public void setSpecialistList(List<Specialist> specialistList) {
        this.specialistList = specialistList;
    }

    // Helper profesional para la vista
    public String getFullName() {
        return (lastname != null ? lastname : "") + " " + (firstname != null ? firstname : "");
    }

    // Helper para obtener las especialidades como una sola cadena de texto
    @Transient
    public String getSpecialtiesNames() {
        if (specialistList == null || specialistList.isEmpty()) {
            return "General / Sin Especialidad";
        }

        return specialistList.stream()
                .map(s -> s.getMedicalspecialtyid()) // Obtener especialidad
                .filter(medicalSpecialty -> medicalSpecialty != null) // Filtrar nulls
                .map(medicalSpecialty -> medicalSpecialty.getName()) // Obtener nombre
                .filter(name -> name != null && !name.isEmpty()) // Seguridad extra
                .distinct()
                .collect(Collectors.joining(", "));
    }

    @Override
    public int hashCode() {
        return (id != null ? id.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Medic)) {
            return false;
        }
        Medic other = (Medic) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "Medic[ id=" + id + ", name=" + getFullName() + " ]";
    }
}
