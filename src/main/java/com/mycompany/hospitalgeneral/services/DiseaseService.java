package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Disease;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class DiseaseService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Busca todas las enfermedades activas
     */
    public List<Disease> findAll() {
        return em.createNamedQuery("Disease.findAll", Disease.class)
                .getResultList();
    }

    /**
     * Busca por ID
     */
    public Disease findById(Integer id) {
        return em.find(Disease.class, id);
    }

    /**
     * Busca por código CIE-10 exacto
     */
    public Disease findByCode(String code) {
        try {
            return em.createNamedQuery("Disease.findByCode", Disease.class)
                    .setParameter("code", code.toUpperCase())
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Busca por nombre (contiene)
     */
    public List<Disease> findByName(String name) {
        return em.createNamedQuery("Disease.findByName", Disease.class)
                .setParameter("name", "%" + name.toLowerCase() + "%")
                .getResultList();
    }

    /**
     * Busca por tipo de enfermedad
     */
    public List<Disease> findByDiseasetype(Integer typeId) {
        return em.createNamedQuery("Disease.findByDiseasetype", Disease.class)
                .setParameter("typeId", typeId)
                .getResultList();
    }

    /**
     * BÚSQUEDA AVANZADA: Por código o nombre
     */
    public List<Disease> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        
        String search = "%" + searchTerm.toLowerCase() + "%";
        return em.createNamedQuery("Disease.search", Disease.class)
                .setParameter("search", search)
                .getResultList();
    }

    /**
     * BÚSQUEDA AVANZADA: Con múltiples filtros
     */
    public List<Disease> searchAdvanced(String searchTerm, Integer typeId, String codePrefix) {
        StringBuilder jpql = new StringBuilder(
            "SELECT d FROM Disease d WHERE (d.deleted = false OR d.deleted IS NULL)");
        
        // Filtro por término de búsqueda (código o nombre)
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            jpql.append(" AND (LOWER(d.code) LIKE :search OR LOWER(d.name) LIKE :search)");
        }
        
        // Filtro por tipo de enfermedad
        if (typeId != null) {
            jpql.append(" AND d.diseasetypeid.id = :typeId");
        }
        
        // Filtro por prefijo de código (ej: "A00" para enfermedades infecciosas intestinales)
        if (codePrefix != null && !codePrefix.trim().isEmpty()) {
            jpql.append(" AND UPPER(d.code) LIKE :codePrefix");
        }
        
        jpql.append(" ORDER BY d.code, d.name");
        
        TypedQuery<Disease> query = em.createQuery(jpql.toString(), Disease.class);
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            query.setParameter("search", "%" + searchTerm.toLowerCase() + "%");
        }
        if (typeId != null) {
            query.setParameter("typeId", typeId);
        }
        if (codePrefix != null && !codePrefix.trim().isEmpty()) {
            query.setParameter("codePrefix", codePrefix.toUpperCase() + "%");
        }
        
        return query.getResultList();
    }

    /**
     * Verifica si existe una enfermedad con el mismo código
     */
    public boolean existsByCode(String code, Integer excludeId) {
        String jpql = "SELECT COUNT(d) FROM Disease d WHERE UPPER(d.code) = :code AND (d.deleted = false OR d.deleted IS NULL)";
        
        if (excludeId != null) {
            jpql += " AND d.id != :excludeId";
        }
        
        var query = em.createQuery(jpql, Long.class)
                .setParameter("code", code.toUpperCase());
        
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }
        
        return query.getSingleResult() > 0;
    }

    @Transactional
    public void save(Disease disease, Integer currentUserId) {
        // Validar código único
        if (disease.getCode() != null && !disease.getCode().trim().isEmpty()) {
            if (existsByCode(disease.getCode(), disease.getId())) {
                throw new IllegalArgumentException("Ya existe una enfermedad con el código " + disease.getCode());
            }
            disease.setCode(disease.getCode().toUpperCase());
        }

        if (disease.getId() == null) {
            disease.setCreatedby(currentUserId);
            em.persist(disease);
        } else {
            disease.setEditedby(currentUserId);
            em.merge(disease);
        }
    }

    @Transactional
    public void delete(Integer diseaseId, Integer deletedBy) {
        Disease disease = findById(diseaseId);
        if (disease != null) {
            disease.setDeleted(true);
            disease.setDeletedby(deletedBy);
            em.merge(disease);
        }
    }
}