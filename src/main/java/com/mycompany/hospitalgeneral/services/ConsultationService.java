package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.*;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar la Atención Médica / Consulta
 * Maneja: MedicalRecord, Cros, Rpe, Diagnostic, MedicalExam
 */
@Stateless
public class ConsultationService {

    @PersistenceContext(unitName = "HospitalPU")
    private EntityManager em;

    // ==================== CARGAR DATOS DE CONSULTA ====================

    /**
     * Carga un MedicalRecord completo con todas sus relaciones
     */
    public Medicalrecord loadMedicalRecord(Integer recordId) {
        Medicalrecord record = em.find(Medicalrecord.class, recordId);
        if (record != null) {
            // Forzar carga de relaciones lazy
            if (record.getPatientid() != null) {
                record.getPatientid().getFirstname(); // trigger load
            }
            if (record.getMedicid() != null) {
                record.getMedicid().getFirstname(); // trigger load
            }
            if (record.getVitalsignCollection() != null) {
                record.getVitalsignCollection().size(); // trigger load
            }
            if (record.getDiagnosticCollection() != null) {
                record.getDiagnosticCollection().size();
            }
            if (record.getMedicalexamCollection() != null) {
                record.getMedicalexamCollection().size();
            }
            if (record.getCrosCollection() != null) {
                record.getCrosCollection().size();
            }
            if (record.getRpeCollection() != null) {
                record.getRpeCollection().size();
            }
        }
        return record;
    }

    /**
     * Busca antecedentes de un paciente
     */
    public Antecedent findAntecedentsByPatient(Integer patientId) {
        try {
            List<Antecedent> result = em.createQuery(
                "SELECT a FROM Antecedent a WHERE a.patientid.id = :patientId " +
                "AND (a.deleted = false OR a.deleted IS NULL) " +
                "ORDER BY a.createdat DESC", Antecedent.class)
                .setParameter("patientId", patientId)
                .setMaxResults(1)
                .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Busca signos vitales de un medical record
     */
    public List<Vitalsign> findVitalSignsByRecord(Integer recordId) {
        return em.createNamedQuery("Vitalsign.findByMedicalrecord", Vitalsign.class)
                .setParameter("medicalRecordId", recordId)
                .getResultList();
    }

    // ==================== GUARDAR/ACTUALIZAR ====================

    /**
     * Actualiza el motivo y enfermedad actual del MedicalRecord
     */
    @Transactional
    public void updateMedicalRecordInfo(Integer recordId, String reason, String currentIllness, Integer medicId) {
        Medicalrecord record = em.find(Medicalrecord.class, recordId);
        if (record != null) {
            record.setReason(reason);
            record.setCurrentillness(currentIllness);
            record.setEditedby(medicId);
            em.merge(record);
        }
    }

    /**
     * Guarda o actualiza la Revisión por Sistemas (Cros)
     */
    @Transactional
    public Cros saveCros(Cros cros, Integer recordId, Integer medicId) {
        Medicalrecord record = em.find(Medicalrecord.class, recordId);
        if (record == null) return null;

        cros.setMedicalrecordid(record);
        
        if (cros.getId() == null) {
            // Nuevo
            cros.setCreatedby(medicId);
            em.persist(cros);
        } else {
            // Actualizar
            cros.setEditedby(medicId);
            em.merge(cros);
        }
        return cros;
    }

    /**
     * Guarda o actualiza la Revisión Física por Regiones (Rpe)
     */
    @Transactional
    public Rpe saveRpe(Rpe rpe, Integer recordId, Integer medicId) {
        Medicalrecord record = em.find(Medicalrecord.class, recordId);
        if (record == null) return null;

        rpe.setMedicalrecordid(record);
        
        if (rpe.getId() == null) {
            // Nuevo
            rpe.setCreatedby(medicId);
            em.persist(rpe);
        } else {
            // Actualizar
            rpe.setEditedby(medicId);
            em.merge(rpe);
        }
        return rpe;
    }

    /**
     * Agrega un diagnóstico a la consulta
     */
    @Transactional
    public Diagnostic addDiagnostic(Integer recordId, Integer diseaseId, Integer medicId) {
        Medicalrecord record = em.find(Medicalrecord.class, recordId);
        if (record == null) return null;

        Disease disease = em.find(Disease.class, diseaseId);
        if (disease == null) return null;

        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setMedicalrecordid(record);
        diagnostic.setDiseaseid(disease);
        diagnostic.setCreatedby(medicId);
        
        em.persist(diagnostic);
        return diagnostic;
    }

    /**
     * Elimina un diagnóstico (soft delete)
     */
    @Transactional
    public void removeDiagnostic(Integer diagnosticId, Integer medicId) {
        Diagnostic diagnostic = em.find(Diagnostic.class, diagnosticId);
        if (diagnostic != null) {
            diagnostic.setDeleted(true);
            diagnostic.setDeletedby(medicId);
            em.merge(diagnostic);
        }
    }

    /**
     * Solicita un examen médico
     */
    @Transactional
    public Medicalexam requestExam(Integer recordId, Integer examId, Integer medicId) {
        Medicalrecord record = em.find(Medicalrecord.class, recordId);
        if (record == null) return null;

        Exam exam = em.find(Exam.class, examId);
        if (exam == null) return null;

        Medicalexam medicalExam = new Medicalexam();
        medicalExam.setMedicalrecordid(record);
        medicalExam.setExamid(exam);
        medicalExam.setCreatedby(medicId);
        
        em.persist(medicalExam);
        return medicalExam;
    }

    /**
     * Cancela solicitud de examen (soft delete)
     */
    @Transactional
    public void cancelExamRequest(Integer medicalExamId, Integer medicId) {
        Medicalexam medicalExam = em.find(Medicalexam.class, medicalExamId);
        if (medicalExam != null) {
            medicalExam.setDeleted(true);
            medicalExam.setDeletedby(medicId);
            em.merge(medicalExam);
        }
    }

    /**
     * Marca la consulta como completada
     */
    @Transactional
    public void completeConsultation(Integer recordId, Integer medicId) {
        Medicalrecord record = em.find(Medicalrecord.class, recordId);
        if (record != null) {
            record.setDone(true);
            record.setEditedby(medicId);
            em.merge(record);
        }
    }

    // ==================== BÚSQUEDAS PARA SELECTS ====================

    /**
     * Busca todas las enfermedades activas para el diagnóstico
     */
    public List<Disease> findAllDiseases() {
        return em.createQuery(
            "SELECT d FROM Disease d WHERE d.deleted = false OR d.deleted IS NULL ORDER BY d.name", 
            Disease.class)
            .getResultList();
    }

    /**
     * Busca todos los exámenes disponibles
     */
    public List<Exam> findAllExams() {
        return em.createQuery(
            "SELECT e FROM Exam e WHERE e.deleted = false OR e.deleted IS NULL ORDER BY e.name", 
            Exam.class)
            .getResultList();
    }

    // ==================== HISTORIAL DEL PACIENTE ====================

    /**
     * Busca consultas anteriores del paciente (excluyendo la actual)
     */
    public List<Medicalrecord> findPatientHistory(Integer patientId, Integer excludeRecordId) {
        return em.createQuery(
            "SELECT m FROM Medicalrecord m " +
            "WHERE m.patientid.id = :patientId " +
            "AND m.id != :excludeRecordId " +
            "AND m.done = true " +
            "AND (m.deleted = false OR m.deleted IS NULL) " +
            "ORDER BY m.createdat DESC", Medicalrecord.class)
            .setParameter("patientId", patientId)
            .setParameter("excludeRecordId", excludeRecordId)
            .setMaxResults(10)
            .getResultList();
    }
}