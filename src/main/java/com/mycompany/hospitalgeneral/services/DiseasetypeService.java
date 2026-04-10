package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Diseasetype;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class DiseasetypeService {

    @PersistenceContext
    private EntityManager em;

    public List<Diseasetype> findAll() {
        return em.createNamedQuery("Diseasetype.findAll", Diseasetype.class)
                .getResultList();
    }

    public Diseasetype findById(Integer id) {
        return em.find(Diseasetype.class, id);
    }

    public Diseasetype findByName(String name) {
        try {
            return em.createNamedQuery("Diseasetype.findByName", Diseasetype.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void save(Diseasetype diseasetype, Integer currentUserId) {
        if (diseasetype.getId() == null) {
            diseasetype.setCreatedby(currentUserId);
            em.persist(diseasetype);
        } else {
            diseasetype.setEditedby(currentUserId);
            em.merge(diseasetype);
        }
    }

    @Transactional
    public void delete(Integer diseasetypeId, Integer deletedBy) {
        Diseasetype diseasetype = findById(diseasetypeId);
        if (diseasetype != null) {
            diseasetype.setDeleted(true);
            diseasetype.setDeletedby(deletedBy);
            em.merge(diseasetype);
        }
    }
}