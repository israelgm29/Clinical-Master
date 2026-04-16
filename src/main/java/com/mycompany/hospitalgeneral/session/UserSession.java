package com.mycompany.hospitalgeneral.session;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.services.interfaces.DisplayUser;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 *
 * @author jhonatan
 */
@Named
@SessionScoped
public class UserSession implements Serializable {

    private Tuser user;
    private Medic medic;

    public DisplayUser getDisplayProfile() {
        if (this.medic != null) {
            // Si hay un médico cargado, él tiene prioridad para mostrar datos
            return this.medic;
        }
        // Si no hay médico (es Admin, Analista, etc.), devolvemos el Tuser
        return this.user;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public String getRole() {
        return user != null ? user.getRoleid().getName() : null;
    }

    public void clear() {
        user = null;
        medic = null;
    }

    public Tuser getUser() {
        return user;
    }

    public void setUser(Tuser user) {
        this.user = user;
    }

    public Medic getMedic() {
        return medic;
    }

    public void setMedic(Medic medic) {
        this.medic = medic;
    }

}
