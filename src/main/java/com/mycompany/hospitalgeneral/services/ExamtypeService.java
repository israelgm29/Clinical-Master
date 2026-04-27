package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Examtype;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@Stateless
public class ExamtypeService {

    @PersistenceContext
    private EntityManager em;

    public List<Examtype> findAll() {
        return em.createNamedQuery("Examtype.findAll", Examtype.class)
                .getResultList();
    }

    public Examtype findById(Integer id) {
        return em.find(Examtype.class, id);
    }

    public Examtype findByName(String name) {
        try {
            return em.createNamedQuery("Examtype.findByName", Examtype.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void save(Examtype examtype, Integer currentUserId) {
        if (examtype.getId() == null) {
            examtype.setCreatedby(currentUserId);
            em.persist(examtype);
        } else {
            examtype.setEditedby(currentUserId);
            em.merge(examtype);
        }
    }

    @Transactional
    public void delete(Integer examtypeId, Integer deletedBy) {
        Examtype examtype = findById(examtypeId);
        if (examtype != null) {
            examtype.setDeleted(true);
            examtype.setDeletedby(deletedBy);
            em.merge(examtype);
        }
    }
}