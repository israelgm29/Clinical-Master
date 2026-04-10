package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Patient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class PatientService {

    @PersistenceContext(unitName = "HospitalPU") // Asegúrate que coincida con tu persistence.xml
    private EntityManager em;

    /**
     * Obtiene todos los pacientes que no han sido borrados (Soft Delete).
     *
     * @return
     */
    public List<Patient> findAll() {
        return em.createQuery(
                "SELECT p FROM Patient p WHERE p.deleted = :deleted ORDER BY p.id DESC",
                Patient.class)
                .setParameter("deleted", false)
                .getResultList();
    }

    /**
     * Guarda un nuevo paciente o actualiza uno existente.
     */
    @Transactional
    public void save(Patient patient) {
        if (patient.getId() == null) {
            em.persist(patient);
        } else {
            em.merge(patient);
        }
    }

    /**
     * Aplicamos Borrado Lógico (Soft Delete) según las buenas prácticas.
     */
    @Transactional
    public void delete(Patient patient) {
        Patient toRemove = em.find(Patient.class, patient.getId());
        if (toRemove != null) {
            // En lugar de em.remove(), marcamos como eliminado
            // Esto es vital en sistemas médicos para auditoría
            // toRemove.setDeleted(true); 
            // em.merge(toRemove);

            // Si prefieres borrado físico por ahora:
            em.remove(toRemove);
        }
    }

    public Patient findById(Integer id) {
        return em.find(Patient.class, id);
    }
}
