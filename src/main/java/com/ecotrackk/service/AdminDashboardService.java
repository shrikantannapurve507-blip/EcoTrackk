
package com.ecotrackk.service;

import com.ecotrackk.dao.CollectionRequestDAO;
import com.ecotrackk.dao.UserDAO;
import com.ecotrackk.entity.CollectionRequest;
import com.ecotrackk.entity.User;

import java.util.List;

public class AdminDashboardService {

    private final UserDAO userDAO = new UserDAO();
    private final CollectionRequestDAO requestDAO =
            new CollectionRequestDAO();

    // ==================== ADMIN DASHBOARD ====================

    public void displayDashboard(User admin) {
        validateAdmin(admin);

        long totalUsers = userDAO.getAllUsers().size();
        long totalCollectors = userDAO.getTotalCollectors();

        long totalRequests = requestDAO.getTotalRequests();
        long pending = requestDAO.getRequestCountByStatus("PENDING");
        long assigned = requestDAO.getRequestCountByStatus("ASSIGNED");
        long collected = requestDAO.getRequestCountByStatus("COLLECTED");
        long cancelled = requestDAO.getRequestCountByStatus("CANCELLED");

        long collectedQuantity = requestDAO.getTotalCollectedQuantity();

        System.out.println("\n==============================================");
        System.out.println("          ECOTRACKK ADMIN DASHBOARD");
        System.out.println("==============================================");

        System.out.printf("%-30s %d%n", "Total Registered Users:", totalUsers);
        System.out.printf("%-30s %d%n", "Total Collectors:", totalCollectors);
        System.out.printf("%-30s %d%n", "Total Collection Requests:", totalRequests);

        System.out.println("\n--------------- REQUEST STATUS ---------------");
        System.out.printf("%-30s %d%n", "Pending Requests:", pending);
        System.out.printf("%-30s %d%n", "Assigned Requests:", assigned);
        System.out.printf("%-30s %d%n", "Collected Requests:", collected);
        System.out.printf("%-30s %d%n", "Cancelled Requests:", cancelled);

        System.out.println("\n---------------- WASTE SUMMARY ----------------");
        System.out.printf("%-30s %d%n", "Collected Quantity:", collectedQuantity);

        System.out.println("==============================================");
    }

    // ==================== WASTE TYPE REPORT ====================

    public void displayWasteTypeReport(User admin) {
        validateAdmin(admin);

        List<Object[]> results = requestDAO.getRequestCountsByWasteType();

        System.out.println("\n==============================================");
        System.out.println("             WASTE TYPE REPORT");
        System.out.println("==============================================");

        if (results == null || results.isEmpty()) {
            System.out.println("No collection requests found.");
            System.out.println("==============================================");
            return;
        }

        System.out.printf("%-25s %15s%n", "WASTE TYPE", "REQUEST COUNT");
        System.out.println("----------------------------------------------");

        for (Object[] row : results) {
            String wasteType = String.valueOf(row[0]);
            long count = ((Number) row[1]).longValue();

            System.out.printf("%-25s %15d%n", wasteType, count);
        }

        System.out.println("==============================================");
    }

    // ==================== COLLECTION HISTORY ====================

    public void displayCollectionHistory(User admin) {
        validateAdmin(admin);

        List<CollectionRequest> history =
                requestDAO.getCollectionHistory();

        System.out.println("\n==============================================================");
        System.out.println("                    COLLECTION HISTORY");
        System.out.println("==============================================================");

        if (history == null || history.isEmpty()) {
            System.out.println("No completed collections found.");
            System.out.println("==============================================================");
            return;
        }

        System.out.printf(
                "%-5s %-15s %-8s %-20s %-20s%n",
                "ID", "WASTE TYPE", "QTY", "USER", "COLLECTOR"
        );

        System.out.println("--------------------------------------------------------------");

        for (CollectionRequest request : history) {
            String userName = request.getUser() != null
                    ? request.getUser().getName()
                    : "N/A";

            String collectorName = request.getCollector() != null
                    ? request.getCollector().getName()
                    : "N/A";

            System.out.printf(
                    "%-5s %-15s %-8d %-20s %-20s%n",
                    request.getRequestId(),
                    request.getWasteType(),
                    request.getQuantity(),
                    userName,
                    collectorName
            );

            System.out.println("  Pickup Address: " + request.getPickupAddress());
            System.out.println("  Request Date:   " + request.getCreatedAt());
            System.out.println("--------------------------------------------------------------");
        }

        System.out.println("==============================================================");
    }

    // ==================== MONTHLY COLLECTION REPORT ====================

    public void displayMonthlyCollectionReport(User admin) {
        validateAdmin(admin);

        List<Object[]> results =
                requestDAO.getMonthlyCollectionReport();

        System.out.println("\n====================================================");
        System.out.println("             MONTHLY COLLECTION REPORT");
        System.out.println("====================================================");

        if (results == null || results.isEmpty()) {
            System.out.println("No completed collections found.");
            System.out.println("====================================================");
            return;
        }

        System.out.printf(
                "%-10s %-15s %-20s %-15s%n",
                "YEAR", "MONTH", "COMPLETED REQUESTS", "TOTAL QTY"
        );

        System.out.println("----------------------------------------------------");

        for (Object[] row : results) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            long completedRequests = ((Number) row[2]).longValue();

            long totalQuantity = row[3] == null
                    ? 0L
                    : ((Number) row[3]).longValue();

            System.out.printf(
                    "%-10d %-15d %-20d %-15d%n",
                    year,
                    month,
                    completedRequests,
                    totalQuantity
            );
        }

        System.out.println("====================================================");
        System.out.println("Note: Monthly grouping currently uses request creation date.");
    }

    // ==================== ADMIN VALIDATION ====================

    private void validateAdmin(User admin) {
        if (admin == null
                || admin.getRole() == null
                || !"ADMIN".equalsIgnoreCase(admin.getRole())) {

            throw new SecurityException(
                    "Access denied! Only administrators can view reports."
            );
        }
    }
}
