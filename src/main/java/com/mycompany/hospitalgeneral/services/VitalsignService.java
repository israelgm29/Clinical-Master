package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Vitalsign;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@Stateless
public class VitalsignService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Guarda un nuevo registro de signos vitales
     */
    @Transactional
    public void save(Vitalsign vitalsign, Integer currentUserId) {
        vitalsign.setCreatedby(currentUserId);
        em.persist(vitalsign);
    }

    /**
     * Actualiza un registro existente
     */
    @Transactional
    public void update(Vitalsign vitalsign, Integer currentUserId) {
        vitalsign.setEditedby(currentUserId);
        em.merge(vitalsign);
    }

    /**
     * Obtiene todos los signos vitales de una historia clínica
     */
    public List<Vitalsign> findByMedicalRecord(Integer medicalRecordId) {
        return em.createNamedQuery("Vitalsign.findByMedicalrecord", Vitalsign.class)
                .setParameter("medicalRecordId", medicalRecordId)
                .getResultList();
    }

    /**
     * Obtiene el último registro de signos vitales de una historia clínica
     */
    public Vitalsign getLastVitalSign(Medicalrecord record) {
        List<Vitalsign> list = em.createQuery(
                "SELECT v FROM Vitalsign v WHERE v.medicalrecordid = :record AND (v.deleted = false OR v.deleted IS NULL) ORDER BY v.createdat DESC",
                Vitalsign.class)
                .setParameter("record", record)
                .setMaxResults(1)
                .getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    public Vitalsign findLastByPatientId(Integer patientId) {
        try {
            return em.createQuery(
                    "SELECT v FROM Vitalsign v "
                    + "WHERE v.medicalrecordid.patientid.id = :patientId "
                    + "AND (v.deleted = false OR v.deleted IS NULL) "
                    + "ORDER BY v.createdat DESC",
                    Vitalsign.class)
                    .setParameter("patientId", patientId)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
