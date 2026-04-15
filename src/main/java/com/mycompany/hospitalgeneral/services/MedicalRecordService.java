package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Vitalsign;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar MedicalRecord (Historias Clínicas / Consultas)
 */
@Stateless
public class MedicalRecordService {

    @PersistenceContext
    private EntityManager em;

    // ==================== BÚSQUEDAS BÁSICAS ====================
    /**
     * Busca un MedicalRecord por ID
     */
    public Medicalrecord findById(Integer id) {
        return em.find(Medicalrecord.class, id);
    }

    /**
     * Busca todos los registros médicos activos
     */
    public List<Medicalrecord> findAll() {
        return em.createNamedQuery("Medicalrecord.findAll", Medicalrecord.class)
                .getResultList();
    }

    /**
     * Busca todos los registros de un médico específico
     */
    public List<Medicalrecord> findByMedic(Integer medicId) {
        return em.createNamedQuery("Medicalrecord.findByMedic", Medicalrecord.class)
                .setParameter("medicId", medicId)
                .getResultList();
    }

    /**
     * Busca todos los registros de un paciente específico
     */
    public List<Medicalrecord> findByPatient(Integer patientId) {
        return em.createNamedQuery("Medicalrecord.findByPatient", Medicalrecord.class)
                .setParameter("patientId", patientId)
                .getResultList();
    }

    // ==================== DASHBOARD - CONSULTAS PENDIENTES ====================
    /**
     * Consultas pendientes del médico (done = false, canceled = false)
     */
    public List<Medicalrecord> findPendingByMedic(Integer medicId) {
        return em.createNamedQuery("Medicalrecord.findPendingByMedic", Medicalrecord.class)
                .setParameter("medicId", medicId)
                .getResultList();
    }

    /**
     * Cuenta consultas pendientes del médico
     */
    public Long countPendingByMedic(Integer medicId) {
        try {
            return em.createNamedQuery("Medicalrecord.countPendingByMedic", Long.class)
                    .setParameter("medicId", medicId)
                    .getSingleResult();
        } catch (Exception e) {
            return 0L;
        }
    }

    // ==================== DASHBOARD - CONSULTAS DEL DÍA ====================
    /**
     * Consultas del día para un médico
     */
    public List<Medicalrecord> findTodayByMedic(Integer medicId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();        // 00:00:00
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay(); // 00:00:00 mañana

        return em.createQuery(
                "SELECT m FROM Medicalrecord m "
                + "WHERE m.medicid.id = :medicId "
                + "AND m.createdat >= :start "
                + "AND m.createdat < :end "
                + "AND (m.deleted = false OR m.deleted IS NULL) "
                + "ORDER BY m.createdat DESC",
                Medicalrecord.class)
                .setParameter("medicId", medicId)
                .setParameter("start", startOfDay)
                .setParameter("end", endOfDay)
                .getResultList();
    }

    /**
     * Cuenta consultas del día para un médico
     */
    public Long countTodayByMedic(Integer medicId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            return em.createQuery(
                    "SELECT COUNT(m) FROM Medicalrecord m "
                    + "WHERE m.medicid.id = :medicId "
                    + "AND m.createdat >= :start "
                    + "AND m.createdat < :end "
                    + "AND (m.deleted = false OR m.deleted IS NULL)",
                    Long.class)
                    .setParameter("medicId", medicId)
                    .setParameter("start", startOfDay)
                    .setParameter("end", endOfDay)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace(); // Para debug
            return 0L;
        }
    }

    // ==================== DASHBOARD - PACIENTES EN ESPERA ====================
    /**
     * Pacientes en espera: tienen signos vitales pero consulta no realizada
     */
    public List<Medicalrecord> findWaitingPatients(Integer medicId) {
        System.out.println(">>> findWaitingPatients - medicId: " + medicId);

        List<Medicalrecord> result = em.createNamedQuery("Medicalrecord.findWaitingPatients", Medicalrecord.class)
                .setParameter("medicId", medicId)
                .getResultList();

        System.out.println(">>> findWaitingPatients - resultado: " + result.size() + " registros");
        return result;
    }

    /**
     * Cuenta pacientes en espera
     */
    public Long countWaitingPatients(Integer medicId) {
        try {
            // JPQL dinámico porque no podemos usar SIZE() en SELECT COUNT con named queries fácilmente
            Long count = em.createQuery(
                    "SELECT COUNT(m) FROM Medicalrecord m "
                    + "WHERE m.medicid.id = :medicId "
                    + "AND m.done = false "
                    + "AND m.canceled = false "
                    + "AND (m.deleted = false OR m.deleted IS NULL) "
                    + "AND EXISTS (SELECT v FROM Vitalsign v WHERE v.medicalrecordid = m "
                    + "AND (v.deleted = false OR v.deleted IS NULL))",
                    Long.class)
                    .setParameter("medicId", medicId)
                    .getSingleResult();
            System.out.println(">>> countWaitingPatients - medicId: " + medicId + " → count: " + count);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * NUEVO: Encuentra MedicalRecords que: - No están done (done = false o
     * null) - No están canceled - Tienen signos vitales (para médico) - medicid
     * puede ser null o asignado
     */
    public List<Medicalrecord> findWaitingForMedic() {
        return em.createQuery(
                "SELECT DISTINCT m FROM Medicalrecord m "
                + "JOIN m.vitalsignCollection v "
                + "WHERE (m.done = false OR m.done IS NULL) "
                + "AND (m.canceled = false OR m.canceled IS NULL) "
                + "AND (m.deleted = false OR m.deleted IS NULL) "
                + "AND (v.deleted = false OR v.deleted IS NULL) "
                + "ORDER BY m.createdat ASC",
                Medicalrecord.class)
                .getResultList();
    }

    /**
     * NUEVO: Encuentra MedicalRecords pendientes de signos vitales (no tienen
     * signos vitales aún, o todos están eliminados)
     */
    public List<Medicalrecord> findPendingForVitalsigns() {
        return em.createQuery(
                "SELECT m FROM Medicalrecord m "
                + "WHERE (m.done = false OR m.done IS NULL) "
                + "AND (m.canceled = false OR m.canceled IS NULL) "
                + "AND (m.deleted = false OR m.deleted IS NULL) "
                + "AND (m.vitalsignCollection IS EMPTY OR "
                + "     NOT EXISTS (SELECT v FROM m.vitalsignCollection v WHERE v.deleted = false OR v.deleted IS NULL)) "
                + "ORDER BY m.createdat ASC",
                Medicalrecord.class)
                .getResultList();
    }

    /**
     * NUEVO: Guarda un MedicalRecord (para enfermería)
     */
    @Transactional
    public void saveForNurse(Medicalrecord record, Integer nurseId) {
        record.setCreatedby(nurseId);
      
        // medicid puede ser null - se asignará cuando el médico tome la consulta
        if (record.getId() == null) {
            em.persist(record);
        } else {
            em.merge(record);
        }
    }

    // ==================== OPERACIONES CRUD ====================
    /**
     * Guarda o actualiza un MedicalRecord
     */
    @Transactional
    public void save(Medicalrecord record, Integer currentUserId) {
        if (record.getId() == null) {
            record.setCreatedby(currentUserId);
            em.persist(record);
        } else {
            record.setEditedby(currentUserId);
            em.merge(record);
        }
    }

    @Transactional
    public void markAsDone(Integer recordId, Integer currentUserId) {
        Medicalrecord record = findById(recordId);
        if (record != null) {
            record.setDone(true);
            record.setEditedby(currentUserId);
            em.merge(record);
        }
    }

    @Transactional
    public void cancel(Integer recordId, Integer currentUserId) {
        Medicalrecord record = findById(recordId);
        if (record != null) {
            record.setCanceled(true);
            record.setEditedby(currentUserId);
            em.merge(record);
        }
    }

    @Transactional
    public void delete(Integer recordId, Integer deletedBy) {
        Medicalrecord record = findById(recordId);
        if (record != null) {
            record.setDeleted(true);
            record.setDeletedby(deletedBy);
            em.merge(record);
        }
    }

    // ==================== SIGNOS VITALES ====================
    /**
     * Obtiene los signos vitales de un registro médico
     */
    public List<Vitalsign> findVitalSignsByRecord(Integer recordId) {
        return em.createNamedQuery("Vitalsign.findByMedicalrecord", Vitalsign.class)
                .setParameter("medicalRecordId", recordId)
                .getResultList();
    }

    public boolean hasVitalSigns(Integer recordId) {
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(v) FROM Vitalsign v "
                    + "WHERE v.medicalrecordid.id = :recordId "
                    + "AND (v.deleted = false OR v.deleted IS NULL)",
                    Long.class)
                    .setParameter("recordId", recordId)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== BÚSQUEDAS POR FECHA ====================
    public List<Medicalrecord> findByDateRange(Integer medicId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        return em.createQuery(
                "SELECT m FROM Medicalrecord m "
                + "WHERE m.medicid.id = :medicId "
                + "AND m.createdat >= :startDate "
                + "AND m.createdat < :endDate "
                + "AND (m.deleted = false OR m.deleted IS NULL) "
                + "ORDER BY m.createdat DESC",
                Medicalrecord.class)
                .setParameter("medicId", medicId)
                .setParameter("startDate", startDateTime)
                .setParameter("endDate", endDateTime)
                .getResultList();
    }

    public List<Medicalrecord> findByStatus(Integer medicId, Boolean done, Boolean canceled) {
        StringBuilder jpql = new StringBuilder(
                "SELECT m FROM Medicalrecord m "
                + "WHERE m.medicid.id = :medicId "
                + "AND (m.deleted = false OR m.deleted IS NULL) "
        );

        if (done != null) {
            jpql.append("AND m.done = :done ");
        }
        if (canceled != null) {
            jpql.append("AND m.canceled = :canceled ");
        }

        jpql.append("ORDER BY m.createdat DESC");

        TypedQuery<Medicalrecord> query = em.createQuery(jpql.toString(), Medicalrecord.class)
                .setParameter("medicId", medicId);

        if (done != null) {
            query.setParameter("done", done);
        }
        if (canceled != null) {
            query.setParameter("canceled", canceled);
        }

        return query.getResultList();
    }
}
