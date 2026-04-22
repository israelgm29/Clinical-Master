package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Profile;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar Profile (datos personales del usuario).
 *
 * @author jhonatan
 */
@Stateless
public class ProfileService {

    @PersistenceContext(unitName = "HospitalPU")
    private EntityManager em;

    // ==================== BÚSQUEDAS ====================
    public Profile findById(Integer id) {
        return em.find(Profile.class, id);
    }

    public List<Profile> findAll() {
        return em.createNamedQuery("Profile.findAll", Profile.class)
                .getResultList();
    }

    public Profile findByDni(String dni) {
        try {
            return em.createNamedQuery("Profile.findByDni", Profile.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public Profile findByEmail(String email) {
        try {
            return em.createNamedQuery("Profile.findByEmail", Profile.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== CRUD ====================
    /**
     * Guarda un Profile nuevo.
     */
    @Transactional
    public void save(Profile profile, Integer createdBy) {
        profile.setCreatedby(createdBy);
        em.persist(profile);
    }

    /**
     * Actualiza un Profile existente.
     */
    @Transactional
    public Profile update(Profile profile, Integer editedBy) {
        profile.setEditedby(editedBy);
        profile.setEditedat(LocalDateTime.now());
        return em.merge(profile);
    }

    /**
     * Guarda o actualiza según si tiene ID o no. Conveniente para el controller
     * cuando no distingue si es nuevo o existente.
     */
    @Transactional
    public Profile saveOrUpdate(Profile profile, Integer currentUserId) {
        if (profile.getId() == null) {
            save(profile, currentUserId);
            return profile;
        } else {
            return update(profile, currentUserId);
        }
    }

    /**
     * Eliminación lógica.
     */
    @Transactional
    public void delete(Integer profileId, Integer deletedBy) {
        Profile profile = findById(profileId);
        if (profile != null) {
            profile.setDeleted(true);
            profile.setDeletedat(LocalDateTime.now());
            profile.setDeletedby(deletedBy);
            em.merge(profile);
        }
    }
}
