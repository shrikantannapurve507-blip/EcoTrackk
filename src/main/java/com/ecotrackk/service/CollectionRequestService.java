package com.ecotrackk.service;

import com.ecotrackk.dao.CollectionRequestDAO;
import com.ecotrackk.entity.CollectionRequest;
import com.ecotrackk.entity.User;

import java.util.List;
import java.util.Set;

public class CollectionRequestService {

    private final CollectionRequestDAO requestDAO =
            new CollectionRequestDAO();

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("PENDING", "ASSIGNED", "COLLECTED", "CANCELLED");

    // 1. Create collection request
    public void createRequest(
            User user,
            String wasteType,
            int quantity,
            String address,
            String description) {

        checkLoggedIn(user);

        if (wasteType == null || wasteType.isBlank()) {
            throw new IllegalArgumentException("Waste type is required.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero.");
        }

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException(
                    "Pickup address is required.");
        }

        CollectionRequest request = new CollectionRequest();
        request.setUser(user);
        request.setWasteType(wasteType.trim());
        request.setQuantity(quantity);
        request.setPickupAddress(address.trim());
        request.setDescription(description);
        request.setStatus("PENDING");

        boolean created = requestDAO.addRequest(request);

        if (!created) {
            throw new IllegalStateException(
                    "Failed to create collection request.");
        }

        System.out.println("Collection request created successfully.");
    }

    // 2. View my requests
    public List<CollectionRequest> getMyRequests(User user) {
        checkLoggedIn(user);

        return requestDAO.getRequestsByUser(user.getUserId());
    }

    // 3. View all requests
    public List<CollectionRequest> getAllRequests(User admin) {
        checkAdmin(admin);

        return requestDAO.getAllRequests();
    }

    // 4. Get all collectors
    public List<User> getAllCollectors(User admin) {
        checkAdmin(admin);

        return requestDAO.getAllCollectors();
    }

    // 5. Assign collector
    public void assignCollector(
            User admin, Long requestId, Long collectorId) {

        checkAdmin(admin);
        checkPositiveId(requestId, "Request ID");
        checkPositiveId(collectorId, "Collector ID");

        boolean assigned =
                requestDAO.assignCollector(requestId, collectorId);

        if (!assigned) {
            throw new IllegalStateException(
                    "Could not assign collector. Check request and collector IDs.");
        }

        System.out.println("Collector assigned successfully.");
    }

    // 6. Update request status
    public void updateRequestStatus(
            User admin, Long requestId, String newStatus) {

        checkAdmin(admin);
        checkPositiveId(requestId, "Request ID");

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("Status is required.");
        }

        String status = newStatus.trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid request status.");
        }

        List<CollectionRequest> requests = requestDAO.getAllRequests();

        CollectionRequest selected = requests.stream()
                .filter(request ->
                        request.getRequestId() != null
                                && request.getRequestId().equals(requestId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Request not found."));

        String currentStatus = selected.getStatus();

        if ("CANCELLED".equalsIgnoreCase(currentStatus)
                || "COLLECTED".equalsIgnoreCase(currentStatus)) {
            throw new IllegalStateException(
                    "This request can no longer be updated.");
        }

        boolean validTransition =
                ("PENDING".equalsIgnoreCase(currentStatus)
                        && ("ASSIGNED".equals(status)
                        || "CANCELLED".equals(status)))
                        || ("ASSIGNED".equalsIgnoreCase(currentStatus)
                        && ("COLLECTED".equals(status)
                        || "CANCELLED".equals(status)));

        if (!validTransition) {
            throw new IllegalStateException(
                    "Invalid status transition.");
        }

        boolean updated =
                requestDAO.updateRequestStatus(requestId, status);

        if (!updated) {
            throw new IllegalStateException(
                    "Failed to update request status.");
        }

        System.out.println("Request status updated successfully.");
    }

    // 7. Cancel my pending request
    public void cancelMyRequest(User user, Long requestId) {

        checkLoggedIn(user);
        checkPositiveId(requestId, "Request ID");

        boolean cancelled = requestDAO.cancelPendingRequest(
                requestId,
                user.getUserId()
        );

        if (cancelled) {
            System.out.println(
                    "Collection request cancelled successfully.");
        } else {
            System.out.println(
                    "Request could not be cancelled. It may not exist, " +
                            "belong to another user, or may not have PENDING status.");
        }
    }

    // 8. View requests assigned to collector
    public List<CollectionRequest> getMyAssignedRequests(User collector) {
        checkCollector(collector);

        return requestDAO.getRequestsByCollector(
                collector.getUserId());
    }

    // 9. Mark assigned request as collected
    public void markAsCollected(User collector, Long requestId) {

        checkCollector(collector);
        checkPositiveId(requestId, "Request ID");

        boolean collected = requestDAO.markAssignedRequestCollected(
                requestId,
                collector.getUserId()
        );

        if (!collected) {
            throw new IllegalStateException(
                    "Could not complete collection. Check request assignment and status.");
        }
    }

    // Check login
    private void checkLoggedIn(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("Please login first.");
        }
    }

    // Check admin role
    private void checkAdmin(User user) {
        checkLoggedIn(user);

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException(
                    "Only admin can perform this action.");
        }
    }

    // Check collector role
    private void checkCollector(User user) {
        checkLoggedIn(user);

        if (!"COLLECTOR".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException(
                    "Only collectors can perform this action.");
        }
    }

    // Validate positive IDs
    private void checkPositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be a positive number.");
        }
    }
}