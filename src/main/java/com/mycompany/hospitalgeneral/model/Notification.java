package com.mycompany.hospitalgeneral.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@NamedQueries({
    // Últimas 20 notificaciones del usuario
    @NamedQuery(
            name = "Notification.findByUser",
            query = "SELECT n FROM Notification n WHERE n.userid.id = :userId "
            + "ORDER BY n.createdat DESC"
    ),
    // Conteo de no leídas para el badge
    @NamedQuery(
            name = "Notification.countUnread",
            query = "SELECT COUNT(n) FROM Notification n "
            + "WHERE n.userid.id = :userId AND n.isread = false"
    ),
    // Marcar todas como leídas
    @NamedQuery(
            name = "Notification.markAllRead",
            query = "UPDATE Notification n SET n.isread = true "
            + "WHERE n.userid.id = :userId AND n.isread = false"
    ),
 
})
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @JoinColumn(name = "userid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Tuser userid;

    @NotNull
    @Size(max = 100)
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Size(max = 255)
    @Column(name = "message", nullable = false)
    private String message;

    @NotNull
    @Size(max = 30)
    @Column(name = "type", nullable = false)
    private String type;

    @Size(max = 50)
    @Column(name = "icon")
    private String icon;

    @Column(name = "isread", nullable = false)
    private boolean isread = false;

    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdat;

    @PrePersist
    protected void onCreate() {
        this.createdat = LocalDateTime.now();
    }

    // ── Constructores ────────────────────────────────────────────────
    public Notification() {
    }

    public Notification(Tuser user, String title, String message,
            String type, String icon) {
        this.userid = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.icon = icon;
    }

    // ── Getters y Setters ────────────────────────────────────────────
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Tuser getUserid() {
        return userid;
    }

    public void setUserid(Tuser u) {
        this.userid = u;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String m) {
        this.message = m;
    }

    public String getType() {
        return type;
    }

    public void setType(String t) {
        this.type = t;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String i) {
        this.icon = i;
    }

    public boolean isIsread() {
        return isread;
    }

    public void setIsread(boolean r) {
        this.isread = r;
    }

    public LocalDateTime getCreatedat() {
        return createdat;
    }
}
