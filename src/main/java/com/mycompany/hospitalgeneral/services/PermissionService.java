package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Module;
import com.mycompany.hospitalgeneral.model.Permission;
import com.mycompany.hospitalgeneral.model.Role;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class PermissionService {

    @PersistenceContext
    private EntityManager em;

    // ==================== CRUD BÁSICO ====================
    public List<Permission> findAll() {
        return em.createNamedQuery("Permission.findAll", Permission.class)
                .getResultList();
    }

    public Permission findById(Integer id) {
        return em.find(Permission.class, id);
    }

    public List<Permission> findByRole(Integer roleId) {
        return em.createNamedQuery("Permission.findByRole", Permission.class)
                .setParameter("roleId", roleId)
                .getResultList();
    }

    public List<Permission> findByModule(Integer moduleId) {
        return em.createNamedQuery("Permission.findByModule", Permission.class)
                .setParameter("moduleId", moduleId)
                .getResultList();
    }

    public Permission findByRoleAndModule(Integer roleId, Integer moduleId) {
        try {
            return em.createNamedQuery("Permission.findByRoleAndModule", Permission.class)
                    .setParameter("roleId", roleId)
                    .setParameter("moduleId", moduleId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public void save(Permission permission, Integer currentUserId) {
        if (permission.getId() == null) {
            permission.setCreatedby(currentUserId);
            em.persist(permission);
        } else {
            permission.setEditedby(currentUserId);
            em.merge(permission);
        }
    }

    @Transactional
    public void delete(Integer permissionId, Integer deletedBy) {
        Permission permission = findById(permissionId);
        if (permission != null) {
            permission.setDeleted(true);
            permission.setDeletedby(deletedBy);
            permission.setDeletedat(LocalDateTime.now());
            em.merge(permission);
        }
    }

    // ==================== MÉTODOS ESPECÍFICOS DE PERMISOS ====================
    /**
     * Obtiene o crea un permiso para un rol y módulo específicos. Si no existe,
     * crea uno nuevo con todos los permisos en false.
     */
    @Transactional
    public Permission getOrCreatePermission(Integer roleId, Integer moduleId, Integer currentUserId) {
        Permission permission = findByRoleAndModule(roleId, moduleId);

        if (permission == null) {
            permission = new Permission();
            permission.setRoleid(em.find(Role.class, roleId));
            permission.setModuleid(em.find(Module.class, moduleId));
            permission.setCanCreate(false);
            permission.setCanRead(false);
            permission.setCanUpdate(false);
            permission.setCanDelete(false);
            permission.setCreatedby(currentUserId);
            em.persist(permission);
        }

        return permission;
    }

    /**
     * Actualiza un permiso específico (C, R, U, D) para un rol y módulo.
     */
    @Transactional
    public void updatePermission(Integer roleId, Integer moduleId, String permissionType,
            Boolean value, Integer currentUserId) {
        Permission permission = getOrCreatePermission(roleId, moduleId, currentUserId);

        switch (permissionType.toUpperCase()) {
            case "CREATE" ->
                permission.setCanCreate(value);
            case "READ" ->
                permission.setCanRead(value);
            case "UPDATE" ->
                permission.setCanUpdate(value);
            case "DELETE" ->
                permission.setCanDelete(value);
        }

        permission.setEditedby(currentUserId);
        em.merge(permission);
    }

    /**
     * Concede todos los permisos (CRUD completo) a un rol para un módulo.
     */
    @Transactional
    public void grantFullPermission(Integer roleId, Integer moduleId, Integer currentUserId) {
        Permission permission = getOrCreatePermission(roleId, moduleId, currentUserId);
        permission.grantAll();
        permission.setEditedby(currentUserId);
        em.merge(permission);
    }

    /**
     * Revoca todos los permisos de un rol para un módulo.
     */
    @Transactional
    public void revokeAllPermissions(Integer roleId, Integer moduleId, Integer currentUserId) {
        Permission permission = findByRoleAndModule(roleId, moduleId);
        if (permission != null) {
            permission.revokeAll();
            permission.setEditedby(currentUserId);
            em.merge(permission);
        }
    }

    /**
     * Verifica si un rol tiene permiso de lectura en un módulo.
     */
    public boolean canRead(Integer roleId, Integer moduleId) {
        Permission p = findByRoleAndModule(roleId, moduleId);
        return p != null && Boolean.TRUE.equals(p.getCanRead()) && !Boolean.TRUE.equals(p.getDeleted());
    }

    /**
     * Verifica si un rol tiene permiso de escritura (create/update/delete) en
     * un módulo.
     */
    public boolean canWrite(Integer roleId, Integer moduleId) {
        Permission p = findByRoleAndModule(roleId, moduleId);
        return p != null && (Boolean.TRUE.equals(p.getCanCreate())
                || Boolean.TRUE.equals(p.getCanUpdate())
                || Boolean.TRUE.equals(p.getCanDelete()))
                && !Boolean.TRUE.equals(p.getDeleted());
    }

    /**
     * Copia todos los permisos de un rol origen a un rol destino.
     */
    @Transactional
    public void copyPermissionsFromRole(Integer sourceRoleId, Integer targetRoleId, Integer currentUserId) {
        List<Permission> sourcePermissions = findByRole(sourceRoleId);

        for (Permission sourcePerm : sourcePermissions) {
            if (Boolean.TRUE.equals(sourcePerm.getDeleted())) {
                continue;
            }

            Permission targetPerm = getOrCreatePermission(targetRoleId, sourcePerm.getModuleid().getId(), currentUserId);
            targetPerm.setCanCreate(sourcePerm.getCanCreate());
            targetPerm.setCanRead(sourcePerm.getCanRead());
            targetPerm.setCanUpdate(sourcePerm.getCanUpdate());
            targetPerm.setCanDelete(sourcePerm.getCanDelete());
            targetPerm.setEditedby(currentUserId);
            em.merge(targetPerm);
        }
    }

    /**
     * Obtiene el mapa de permisos para un rol específico. Key: moduleId, Value:
     * Permission
     */
    public Map<Integer, Permission> getPermissionMapForRole(Integer roleId) {
        Map<Integer, Permission> map = new HashMap<>();
        List<Permission> permissions = findByRole(roleId);

        for (Permission p : permissions) {
            if (!Boolean.TRUE.equals(p.getDeleted())) {
                map.put(p.getModuleid().getId(), p);
            }
        }

        return map;
    }

    /**
     * Inicializa permisos vacíos para un nuevo rol (todos los módulos, sin
     * permisos).
     */
    @Transactional
    public void initializePermissionsForNewRole(Integer roleId, Integer currentUserId) {
        List<Module> allModules = em.createNamedQuery("Module.findAll", Module.class).getResultList();

        for (Module module : allModules) {
            getOrCreatePermission(roleId, module.getId(), currentUserId);
        }
    }

    /**
     * Elimina lógicamente todos los permisos de un rol.
     */
    @Transactional
    public void deleteAllPermissionsForRole(Integer roleId, Integer deletedBy) {
        List<Permission> permissions = findByRole(roleId);
        LocalDateTime now = LocalDateTime.now();

        for (Permission p : permissions) {
            p.setDeleted(true);
            p.setDeletedby(deletedBy);
            p.setDeletedat(now);
            em.merge(p);
        }
    }
}
