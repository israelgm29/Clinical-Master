package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Tuser;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class TuserService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Busca usuario por email (para login)
     */
    public Tuser findByEmail(String email) {
        try {
            TypedQuery<Tuser> query = em.createNamedQuery("Tuser.findByEmail", Tuser.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (Exception e) {
            return null; // No encontrado
        }
    }

    /**
     * Busca por ID
     */
    public Tuser findById(Integer id) {
        return em.find(Tuser.class, id);
    }
}
