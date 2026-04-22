package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Antecedent;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author jhonatan
 */
@Stateless
public class AntecedentService {

    @PersistenceContext
    private EntityManager em;

    public Antecedent findByPatientId(Integer patientId) {
        try {
            return em.createQuery(
                    "SELECT a FROM Antecedent a "
                    + "WHERE a.patientid.id = :patientId "
                    + "AND (a.deleted = false OR a.deleted IS NULL)",
                    Antecedent.class)
                    .setParameter("patientId", patientId)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

}
