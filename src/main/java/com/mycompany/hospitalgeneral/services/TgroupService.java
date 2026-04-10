package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Tgroup;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@Stateless
public class TgroupService {

    @PersistenceContext
    private EntityManager em;

    public List<Tgroup> findAll() {
        return em.createNamedQuery("Tgroup.findAll", Tgroup.class)
                .getResultList();
    }

    public Tgroup findById(Integer id) {
        return em.find(Tgroup.class, id);
    }

    public Tgroup findByName(String name) {
        try {
            return em.createNamedQuery("Tgroup.findByName", Tgroup.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void save(Tgroup tgroup) {
        if (tgroup.getId() == null) {
            em.persist(tgroup);
        } else {
            em.merge(tgroup);
        }
    }

    @Transactional
    public void delete(Integer tgroupId) {
        Tgroup tgroup = findById(tgroupId);
        if (tgroup != null) {
            em.remove(tgroup);
        }
    }
}
