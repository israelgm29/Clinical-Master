package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Medicalspecialty;
import com.mycompany.hospitalgeneral.model.Specialist;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class SpecialistService {

    @PersistenceContext(unitName = "HospitalPU")
    private EntityManager em;

    public List<Specialist> findByMedic(Integer medicId) {
        return em.createQuery(
                "SELECT s FROM Specialist s WHERE s.medicid.id = :medicId AND (s.deleted = false OR s.deleted IS NULL)",
                Specialist.class)
                .setParameter("medicId", medicId)
                .getResultList();
    }

    public List<Medicalspecialty> findSpecialtiesByMedic(Integer medicId) {
        return em.createQuery(
                "SELECT s.medicalspecialtyid FROM Specialist s WHERE s.medicid.id = :medicId AND (s.deleted = false OR s.deleted IS NULL) ORDER BY s.medicalspecialtyid.name",
                Medicalspecialty.class)
                .setParameter("medicId", medicId)
                .getResultList();
    }

    public Specialist findByMedicAndSpecialty(Integer medicId, Integer specialtyId) {
        try {
            return em.createQuery(
                    "SELECT s FROM Specialist s WHERE s.medicid.id = :medicId AND s.medicalspecialtyid.id = :specialtyId AND (s.deleted = false OR s.deleted IS NULL)",
                    Specialist.class)
                    .setParameter("medicId", medicId)
                    .setParameter("specialtyId", specialtyId)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void assign(Integer medicId, Integer specialtyId, Integer userId) {
        Specialist existing = findByMedicAndSpecialty(medicId, specialtyId);
        if (existing != null) {
            if (existing.getDeleted()) {
                existing.setDeleted(false);
                existing.setEditedat(LocalDateTime.now());
                existing.setEditedby(userId);
                em.merge(existing);
            }
            return;
        }

        Specialist specialist = new Specialist();
        specialist.setMedicid(em.find(Medic.class, medicId));
        specialist.setMedicalspecialtyid(em.find(Medicalspecialty.class, specialtyId));
        specialist.setCreatedby(userId);
        em.persist(specialist);
    }

    @Transactional
    public void remove(Integer specialistId, Integer userId) {
        Specialist specialist = em.find(Specialist.class, specialistId);
        if (specialist != null) {
            specialist.setDeleted(true);
            specialist.setDeletedat(LocalDateTime.now());
            specialist.setDeletedby(userId);
            em.merge(specialist);
        }
    }

    @Transactional
    public void removeByMedicAndSpecialty(Integer medicId, Integer specialtyId, Integer userId) {
        Specialist specialist = findByMedicAndSpecialty(medicId, specialtyId);
        if (specialist != null) {
            remove(specialist.getId(), userId);
        }
    }
}
