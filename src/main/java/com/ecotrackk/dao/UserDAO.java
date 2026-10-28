
package com.ecotrackk.dao;

import com.ecotrackk.entity.User;
import com.ecotrackk.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class UserDAO {

    // Register User
    public void addUser(User user) {
        EntityManager em =
                JPAUtil.getEntityManagerFactory().createEntityManager();

        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.persist(user);
            transaction.commit();

            System.out.println("User registered successfully!");

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException("Registration failed", e);

        } finally {
            em.close();
        }
    }

    // Find user by email
    public User findUserByEmail(String email) {
        EntityManager em =
                JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            List<User> users = em.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email",
                            User.class
                    )
                    .setParameter("email", email)
                    .setMaxResults(1)
                    .getResultList();

            return users.isEmpty() ? null : users.get(0);

        } finally {
            em.close();
        }
    }

    // Check duplicate email
    public boolean emailExists(String email) {
        EntityManager em =
                JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            Long count = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.email = :email",
                            Long.class
                    )
                    .setParameter("email", email)
                    .getSingleResult();

            return count > 0;

        } finally {
            em.close();
        }
    }

    // View all users
    public List<User> getAllUsers() {
        EntityManager em =
                JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            return em.createQuery(
                    "SELECT u FROM User u ORDER BY u.userId",
                    User.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    // Count all collectors
    public long getTotalCollectors() {
        EntityManager em =
                JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            return em.createQuery(
                            "SELECT COUNT(u) FROM User u " +
                                    "WHERE UPPER(u.role) = :role",
                            Long.class
                    )
                    .setParameter("role", "COLLECTOR")
                    .getSingleResult();

        } finally {
            em.close();
        }
    }

    // Update user name
    public boolean updateUserName(Long userId, String newName) {
        EntityManager em =
                JPAUtil.getEntityManagerFactory().createEntityManager();

        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            User user = em.find(User.class, userId);

            if (user == null) {
                transaction.rollback();
                return false;
            }

            user.setName(newName);
            transaction.commit();

            return true;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException("Update failed", e);

        } finally {
            em.close();
        }
    }

    // Delete user
    public boolean deleteUser(Long userId) {
        EntityManager em =
                JPAUtil.getEntityManagerFactory().createEntityManager();

        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            User user = em.find(User.class, userId);

            if (user == null) {
                transaction.rollback();
                return false;
            }

            em.remove(user);
            transaction.commit();

            return true;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw new RuntimeException("Delete failed", e);

        } finally {
            em.close();
        }
    }
}
