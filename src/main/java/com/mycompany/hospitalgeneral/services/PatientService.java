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
     * Busca paciente por DNI exacto
     */
    public Patient findByDni(String dni) {
        try {
            return em.createQuery(
                    "SELECT p FROM Patient p WHERE p.dni = :dni AND (p.deleted = false OR p.deleted IS NULL)",
                    Patient.class)
                    .setParameter("dni", dni)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Busca Pacientes por Historia CLinica
     */
    public Patient findByHc(String hc) {
        try {
            return em.createQuery(
                    "SELECT p FROM Patient p WHERE p.hc = :hc AND (p.deleted = false OR p.deleted IS NULL)",
                    Patient.class)
                    .setParameter("hc", hc)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * NUEVO: Búsqueda flexible por nombre, apellido, DNI o HC
     */
    public List<Patient> searchFlexible(String query) {
        String search = "%" + query.toLowerCase() + "%";
        return em.createQuery(
                "SELECT p FROM Patient p WHERE "
                + "(LOWER(p.firstname) LIKE :search OR "
                + "LOWER(p.lastname) LIKE :search OR "
                + "LOWER(p.dni) LIKE :search OR "
                + "LOWER(p.hc) LIKE :search) AND "
                + "(p.deleted = false OR p.deleted IS NULL) "
                + "ORDER BY p.lastname, p.firstname",
                Patient.class)
                .setParameter("search", search)
                .setMaxResults(10)
                .getResultList();
    }

    /**
     * NUEVO: Genera siguiente número de HC Formato: HC-000001, HC-000002, etc.
     */
    public String generateNextHc() {
        try {
            String lastHc = em.createQuery(
                    "SELECT MAX(p.hc) FROM Patient p WHERE p.hc LIKE 'HC-%'",
                    String.class)
                    .getSingleResult();

            if (lastHc == null) {
                return "HC-000001";
            }

            // Extraer número y sumar 1
            String numberPart = lastHc.replace("HC-", "");
            int nextNumber = Integer.parseInt(numberPart) + 1;
            return String.format("HC-%06d", nextNumber);

        } catch (Exception e) {
            return "HC-" + System.currentTimeMillis(); // Fallback
        }
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
