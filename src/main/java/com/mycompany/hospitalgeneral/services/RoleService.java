/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Role;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author jhonatan
 */

@Stateless
public class RoleService {

    @PersistenceContext(unitName = "HospitalPU")
    private EntityManager em;

    /**
     * Busca todos los roles activos (no eliminados)
     */
    public List<Role> findAll() {
        return em.createNamedQuery("Role.findAll", Role.class)
                .getResultList();
    }

    /**
     * Busca por ID
     */
    public Role findById(Integer id) {
        return em.find(Role.class, id);
    }

    /**
     * Busca por nombre exacto
     */
    public Role findByName(String name) {
        try {
            return em.createNamedQuery("Role.findByName", Role.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Busca rol MÉDICO (comúnmente ID 3 o nombre "MEDICO")
     */
    public Role findMedicRole() {
        // Primero intenta por nombre
        Role role = findByName("MEDICO");
        if (role == null) {
            role = findByName("MEDIC");
        }
        if (role == null) {
            role = findByName("Doctor");
        }
        // Si no encuentra, usa ID 3 por defecto
        if (role == null) {
            role = findById(3);
        }
        return role;
    }

    /**
     * Guarda o actualiza un rol
     */
    @Transactional
    public void save(Role role, Integer currentUserId) {
        if (role.getId() == null) {
            // Nuevo
            role.setCreatedby(currentUserId);
            em.persist(role);
        } else {
            // Actualizar
            role.setEditedby(currentUserId);
            role.setEditedat(LocalDateTime.now());
            em.merge(role);
        }
    }

    /**
     * Eliminación lógica
     */
    @Transactional
    public void delete(Integer roleId, Integer deletedBy) {
        Role role = findById(roleId);
        if (role != null) {
            role.setDeleted(true);
            role.setDeletedat(LocalDateTime.now());
            role.setDeletedby(deletedBy);
            em.merge(role);
        }
    }
}
