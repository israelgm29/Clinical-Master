package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Exam;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class ExamService {

    @PersistenceContext
    private EntityManager em;

    public List<Exam> findAll() {
        return em.createNamedQuery("Exam.findAll", Exam.class)
                .getResultList();
    }

    public List<Exam> findByExamtype(Integer examtypeId) {
        return em.createNamedQuery("Exam.findByExamtype", Exam.class)
                .setParameter("examtypeId", examtypeId)
                .getResultList();
    }

    public Exam findById(Integer id) {
        return em.find(Exam.class, id);
    }

    @Transactional
    public void save(Exam exam, Integer currentUserId) {
        if (exam.getId() == null) {
            exam.setCreatedby(currentUserId);
            em.persist(exam);
        } else {
            exam.setEditedby(currentUserId);
            em.merge(exam);
        }
    }

    @Transactional
    public void delete(Integer examId, Integer deletedBy) {
        Exam exam = findById(examId);
        if (exam != null) {
            exam.setDeleted(true);
            exam.setDeletedby(deletedBy);
            em.merge(exam);
        }
    }
}