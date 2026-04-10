package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Module;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@Stateless
public class ModuleService {

    @PersistenceContext
    private EntityManager em;

    public List<Module> findAll() {
        return em.createNamedQuery("Module.findAll", Module.class)
                .getResultList();
    }

    public Module findById(Integer id) {
        return em.find(Module.class, id);
    }

    public Module findByName(String name) {
        try {
            return em.createNamedQuery("Module.findByName", Module.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void save(Module module) {
        if (module.getId() == null) {
            em.persist(module);
        } else {
            em.merge(module);
        }
    }

    @Transactional
    public void delete(Integer moduleId) {
        Module module = findById(moduleId);
        if (module != null) {
            em.remove(module);
        }
    }
}
