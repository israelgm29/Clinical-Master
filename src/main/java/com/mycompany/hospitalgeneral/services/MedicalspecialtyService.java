package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Medicalspecialty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class MedicalspecialtyService {

    @PersistenceContext(unitName = "HospitalPU")
    private EntityManager em;

    public List<Medicalspecialty> findAll() {
        return em.createQuery(
                "SELECT m FROM Medicalspecialty m WHERE m.deleted = false OR m.deleted IS NULL ORDER BY m.name",
                Medicalspecialty.class)
                .getResultList();
    }

    public Medicalspecialty findById(Integer id) {
        return em.find(Medicalspecialty.class, id);
    }

    public Medicalspecialty findByName(String name) {
        try {
            return em.createQuery(
                    "SELECT m FROM Medicalspecialty m WHERE m.name = :name AND (m.deleted = false OR m.deleted IS NULL)",
                    Medicalspecialty.class)
                    .setParameter("name", name)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void save(Medicalspecialty specialty, Integer userId) {
        if (specialty.getId() == null) {
            specialty.setCreatedby(userId);
            em.persist(specialty);
        } else {
            specialty.setEditedat(LocalDateTime.now());
            specialty.setEditedby(userId);
            em.merge(specialty);
        }
    }

    @Transactional
    public void delete(Integer id, Integer userId) {
        Medicalspecialty specialty = em.find(Medicalspecialty.class, id);
        if (specialty != null) {
            specialty.setDeleted(true);
            specialty.setDeletedat(LocalDateTime.now());
            specialty.setDeletedby(userId);
            em.merge(specialty);
        }
    }

    public long countActive() {
        return em.createQuery(
                "SELECT COUNT(m) FROM Medicalspecialty m WHERE m.deleted = false OR m.deleted IS NULL",
                Long.class)
                .getSingleResult();
    }
}
