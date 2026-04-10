package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "tgroup")
@NamedQueries({
    @NamedQuery(name = "Tgroup.findAll", query = "SELECT g FROM Tgroup g ORDER BY g.name"),
    @NamedQuery(name = "Tgroup.findById", query = "SELECT g FROM Tgroup g WHERE g.id = :id"),
    @NamedQuery(name = "Tgroup.findByName", query = "SELECT g FROM Tgroup g WHERE g.name = :name")
})
public class Tgroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Size(min = 1, max = 50)
    private String name;

    // Relaciones - SIN auditoría
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "groupid")
    private Collection<Option> optionCollection;

    public Tgroup() {
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

    public Collection<Option> getOptionCollection() {
        return optionCollection;
    }

    public void setOptionCollection(Collection<Option> optionCollection) {
        this.optionCollection = optionCollection;
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
        Tgroup other = (Tgroup) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "Tgroup[ id=" + id + ", name=" + name + " ]";
    }
}
