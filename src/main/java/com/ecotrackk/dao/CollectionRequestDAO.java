package com.ecotrackk.dao;

import com.ecotrackk.entity.CollectionRequest;
import com.ecotrackk.entity.User;
import com.ecotrackk.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

public class CollectionRequestDAO {

    // 1. Add a new collection request
    public boolean addRequest(CollectionRequest request) {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            if (request.getUser() != null
                    && request.getUser().getUserId() != null) {

                User user = em.find(
                        User.class,
                        request.getUser().getUserId()
                );

                if (user == null) {
                    transaction.rollback();
                    return false;
                }

                request.setUser(user);
            }

            request.setStatus("PENDING");
            request.setCollectedAt(null);

            em.persist(request);
            transaction.commit();

            return true;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            System.out.println("Error adding request: "
                    + e.getMessage());

            return false;

        } finally {
            em.close();
        }
    }

    // 2. Get requests submitted by a user
    public List<CollectionRequest> getRequestsByUser(Long userId) {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        try {
            TypedQuery<CollectionRequest> query = em.createQuery(
                    "SELECT r FROM CollectionRequest r " +
                            "LEFT JOIN FETCH r.user " +
                            "LEFT JOIN FETCH r.collector " +
                            "WHERE r.user.userId = :userId " +
                            "ORDER BY r.createdAt DESC",
                    CollectionRequest.class
            );

            query.setParameter("userId", userId);

            return query.getResultList();

        } finally {
            em.close();
        }
    }

    // 3. Get all collection requests
    public List<CollectionRequest> getAllRequests() {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        try {
            return em.createQuery(
                    "SELECT r FROM CollectionRequest r " +
                            "LEFT JOIN FETCH r.user " +
                            "LEFT JOIN FETCH r.collector " +
                            "ORDER BY r.createdAt DESC",
                    CollectionRequest.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    // 4. Get requests assigned to a collector
    public List<CollectionRequest> getRequestsByCollector(
            Long collectorId) {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        try {
            TypedQuery<CollectionRequest> query = em.createQuery(
                    "SELECT r FROM CollectionRequest r " +
                            "LEFT JOIN FETCH r.user " +
                            "LEFT JOIN FETCH r.collector " +
                            "WHERE r.collector.userId = :collectorId " +
                            "ORDER BY r.createdAt DESC",
                    CollectionRequest.class
            );

            query.setParameter("collectorId", collectorId);

            return query.getResultList();

        } finally {
            em.close();
        }
    }

    // 5. Get all collectors
    public List<User> getAllCollectors() {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        try {
            return em.createQuery(
                    "SELECT u FROM User u " +
                            "WHERE UPPER(u.role) = 'COLLECTOR'",
                    User.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    // 6. Assign a collector to a request
    public boolean assignCollector(
            Long requestId, Long collectorId) {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            CollectionRequest request =
                    em.find(CollectionRequest.class, requestId);

            User collector = em.find(User.class, collectorId);

            if (request == null || collector == null) {
                transaction.rollback();
                return false;
            }

            if (!"COLLECTOR".equalsIgnoreCase(
                    collector.getRole())) {
                transaction.rollback();
                return false;
            }

            if (!"PENDING".equalsIgnoreCase(
                    request.getStatus())) {
                transaction.rollback();
                return false;
            }

            request.setCollector(collector);
            request.setStatus("ASSIGNED");
            request.setCollectedAt(null);

            em.flush();
            transaction.commit();

            return true;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            System.out.println("Error assigning collector: "
                    + e.getMessage());

            return false;

        } finally {
            em.close();
        }
    }

    // 7. Update request status
    public boolean updateRequestStatus(
            Long requestId, String newStatus) {

        if (newStatus == null || newStatus.isBlank()) {
            return false;
        }

        String status = newStatus.trim().toUpperCase();

        if (!List.of(
                "PENDING",
                "ASSIGNED",
                "COLLECTED",
                "CANCELLED"
        ).contains(status)) {
            System.out.println("Invalid status.");
            return false;
        }

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            CollectionRequest request =
                    em.find(CollectionRequest.class, requestId);

            if (request == null) {
                transaction.rollback();
                return false;
            }

            request.setStatus(status);

            if ("COLLECTED".equals(status)) {
                if (request.getCollectedAt() == null) {
                    request.setCollectedAt(
                            LocalDateTime.now());
                }
            } else {
                request.setCollectedAt(null);
            }

            em.flush();
            transaction.commit();

            return true;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            System.out.println("Error updating status: "
                    + e.getMessage());

            return false;

        } finally {
            em.close();
        }
    }

    // 8. Collector marks an assigned request as collected
    public boolean markAssignedRequestCollected(
            Long requestId, Long collectorId) {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            CollectionRequest request =
                    em.find(CollectionRequest.class, requestId);

            if (request == null) {
                transaction.rollback();
                System.out.println("Request not found.");
                return false;
            }

            if (request.getCollector() == null
                    || !collectorId.equals(
                    request.getCollector().getUserId())) {

                transaction.rollback();
                System.out.println(
                        "This request is not assigned to you.");
                return false;
            }

            if (!"ASSIGNED".equalsIgnoreCase(
                    request.getStatus())) {

                transaction.rollback();
                System.out.println(
                        "Request must be ASSIGNED first.");
                return false;
            }

            request.setStatus("COLLECTED");
            request.setCollectedAt(LocalDateTime.now());

            em.flush();
            transaction.commit();

            System.out.println(
                    "Collection completed successfully.");
            System.out.println(
                    "Collected at: " + request.getCollectedAt());

            return true;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            System.out.println("Error completing collection: "
                    + e.getMessage());

            return false;

        } finally {
            em.close();
        }
    }

    // 9. Cancel a pending request belonging to its owner
    public boolean cancelPendingRequest(
            Long requestId, Long userId) {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            CollectionRequest request =
                    em.createQuery(
                                    "SELECT r FROM CollectionRequest r " +
                                            "WHERE r.requestId = :requestId " +
                                            "AND r.user.userId = :userId",
                                    CollectionRequest.class
                            )
                            .setParameter("requestId", requestId)
                            .setParameter("userId", userId)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);

            if (request == null
                    || !"PENDING".equalsIgnoreCase(
                    request.getStatus())) {

                transaction.rollback();
                return false;
            }

            request.setStatus("CANCELLED");
            request.setCollectedAt(null);

            transaction.commit();

            return true;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            System.out.println("Error cancelling request: "
                    + e.getMessage());

            return false;

        } finally {
            em.close();
        }
    }

    // 10. Total number of requests
    public long getTotalRequests() {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        try {
            return em.createQuery(
                    "SELECT COUNT(r) FROM CollectionRequest r",
                    Long.class
            ).getSingleResult();

        } finally {
            em.close();
        }
    }

    // 11. Count requests by status
    public long getRequestCountByStatus(String status) {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        try {
            return em.createQuery(
                            "SELECT COUNT(r) FROM CollectionRequest r " +
                                    "WHERE UPPER(r.status) = :status",
                            Long.class
                    )
                    .setParameter("status", status.toUpperCase())
                    .getSingleResult();

        } finally {
            em.close();
        }
    }

    // 12. Total quantity of collected waste
    public long getTotalCollectedQuantity() {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        try {
            Long total = em.createQuery(
                    "SELECT SUM(r.quantity) " +
                            "FROM CollectionRequest r " +
                            "WHERE UPPER(r.status) = 'COLLECTED' " +
                            "AND r.collectedAt IS NOT NULL",
                    Long.class
            ).getSingleResult();

            return total == null ? 0L : total;

        } finally {
            em.close();
        }
    }

    // 13. Request counts by waste type
    public List<Object[]> getRequestCountsByWasteType() {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        try {
            return em.createQuery(
                    "SELECT r.wasteType, COUNT(r) " +
                            "FROM CollectionRequest r " +
                            "GROUP BY r.wasteType",
                    Object[].class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    // 14. Collection history
    public List<CollectionRequest> getCollectionHistory() {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        try {
            return em.createQuery(
                    "SELECT r FROM CollectionRequest r " +
                            "LEFT JOIN FETCH r.user " +
                            "LEFT JOIN FETCH r.collector " +
                            "WHERE UPPER(r.status) = 'COLLECTED' " +
                            "ORDER BY r.collectedAt DESC",
                    CollectionRequest.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    // 15. Monthly collection report
    public List<Object[]> getMonthlyCollectionReport() {

        EntityManager em = JPAUtil.getEntityManagerFactory()
                .createEntityManager();

        try {
            return em.createNativeQuery(
                    "SELECT YEAR(collected_at) AS report_year, " +
                            "MONTH(collected_at) AS report_month, " +
                            "COUNT(*) AS total_collections, " +
                            "SUM(quantity) AS total_quantity " +
                            "FROM collection_requests " +
                            "WHERE UPPER(status) = 'COLLECTED' " +
                            "AND collected_at IS NOT NULL " +
                            "GROUP BY YEAR(collected_at), " +
                            "MONTH(collected_at) " +
                            "ORDER BY report_year DESC, report_month DESC"
            ).getResultList();

        } finally {
            em.close();
        }
    }
}