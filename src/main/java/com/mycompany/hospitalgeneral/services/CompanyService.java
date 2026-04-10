package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Company;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@Stateless
public class CompanyService {

    @PersistenceContext
    private EntityManager em;

    public Company findFirst() {
        List<Company> companies = em.createNamedQuery("Company.findAll", Company.class)
                .setMaxResults(1)
                .getResultList();
        return companies.isEmpty() ? null : companies.get(0);
    }

    public Company findById(Integer id) {
        return em.find(Company.class, id);
    }

    @Transactional
    public void save(Company company) {
        if (company.getId() == null) {
            em.persist(company);
        } else {
            em.merge(company);
        }
    }
}