package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "patient", schema = "public")
public class Patient implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "createdat", nullable = false)
    private LocalDateTime createdat;

    @Column(name = "createdby")
    private Integer createdby;

    @Column(name = "editedat")
    private LocalDateTime editedat;

    @Column(name = "editedby")
    private Integer editedby;

    @Column(name = "deleted", columnDefinition = "boolean default false")
    private Boolean deleted = false;

    @Column(name = "deletedat")
    private LocalDateTime deletedat;

    @Column(name = "deletedby")
    private Integer deletedby;

    @Size(min = 10, max = 10, message = "La cédula debe tener 10 dígitos")
    @Pattern(regexp = "^[0-9]*$", message = "La cédula solo permite números")
    @Column(name = "dni", unique = true)
    private String dni;

    @Size(max = 13)
    @Column(name = "passport", unique = true)
    private String passport;

    @NotNull(message = "La Historia Clínica es obligatoria")
    @Size(max = 10)
    @Column(name = "hc", nullable = false, unique = true)
    private String hc;

    @NotNull(message = "Los apellidos son obligatorios")
    @Size(max = 25)
    @Column(name = "lastname", nullable = false)
    private String lastname;

    @NotNull(message = "Los nombres son obligatorios")
    @Size(max = 25)
    @Column(name = "firstname", nullable = false)
    private String firstname;

    @NotNull
    @Size(max = 100, message = "La ocupacion es obligatoria")
    @Column(name = "ocupation", nullable = false) // Ajustado a tu SQL (una sola 'c')
    private String ocupation;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Size(max = 75)
    @Column(name = "image")
    private String image;

    @Size(max = 13, message = "El numero de celular no debe exceder los 13 digitos")
    @Column(name = "telephone")
    private String telephone;

    @Size(max = 13, message = "El numero de celular no debe exceder los 13 digitos")
    @Column(name = "mobile")
    private String mobile;

    @Size(max = 50)
    @Column(name = "email")
    private String email;

    @NotNull
    @Size(max = 100)
    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "blootype")
    @NotNull(message = "Debe seleccionar el tipo de sangre")
    private Integer blootype;

    @NotNull
    @Column(name = "sex", nullable = false)
    private Integer sex;

    @NotNull
    @Column(name = "civilstatus", nullable = false)
    private Integer civilstatus;

    // --- Constructor ---
    public Patient() {
        this.deleted = false; // Por defecto no está borrado
    }

    @PrePersist
    protected void onCreate() {
        this.deleted = false;           // Siempre false al crear
        this.createdat = LocalDateTime.now();  // Fecha/hora automática
    }

    @PreUpdate
    protected void onUpdate() {
        this.editedat = LocalDateTime.now();
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

    public String getHc() {
        return hc;
    }

    public void setHc(String hc) {
        this.hc = hc;
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

    public String getOcupation() {
        return ocupation;
    }

    public void setOcupation(String ocupation) {
        this.ocupation = ocupation;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
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

    public Integer getBlootype() {
        return blootype;
    }

    public void setBlootype(Integer blootype) {
        this.blootype = blootype;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getCivilstatus() {
        return civilstatus;
    }

    public void setCivilstatus(Integer civilstatus) {
        this.civilstatus = civilstatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Patient patient = (Patient) o;
        return Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
