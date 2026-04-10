package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Option;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class OptionService {

    @PersistenceContext
    private EntityManager em;

    public List<Option> findAll() {
        return em.createNamedQuery("Option.findAll", Option.class)
                .getResultList();
    }

    public List<Option> findByGroup(Integer groupId) {
        return em.createNamedQuery("Option.findByGroup", Option.class)
                .setParameter("groupId", groupId)
                .getResultList();
    }

    public Option findById(Integer id) {
        return em.find(Option.class, id);
    }

    public Option findByName(String name) {
        try {
            return em.createNamedQuery("Option.findByName", Option.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void save(Option option, Integer currentUserId) {
        if (option.getId() == null) {
            option.setCreatedby(currentUserId);
            em.persist(option);
        } else {
            option.setEditedby(currentUserId);
            em.merge(option);
        }
    }

    @Transactional
    public void delete(Integer optionId, Integer deletedBy) {
        Option option = findById(optionId);
        if (option != null) {
            option.setDeleted(true);
            option.setDeletedby(deletedBy);
            em.merge(option);
        }
    }
}
