
package com.ecotrackk.app;

import com.ecotrackk.entity.CollectionRequest;
import com.ecotrackk.entity.User;
import com.ecotrackk.service.AdminDashboardService;
import com.ecotrackk.service.CollectionRequestService;
import com.ecotrackk.service.UserService;

import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final Scanner scanner = new Scanner(System.in);

    private final UserService userService = new UserService();
    private final CollectionRequestService requestService =
            new CollectionRequestService();
    private final AdminDashboardService dashboardService =
            new AdminDashboardService();

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n========== ECOTRACK ==========");
            System.out.println("1. Register User");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            switch (readInt()) {
                case 1 -> registerUser();
                case 2 -> login();
                case 3 -> {
                    running = false;
                    System.out.println("Thank you for using EcoTrack!");
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void registerUser() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            userService.registerUser(name, email, password);
        } catch (Exception e) {
            showError(e);
        }
    }

    private void login() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            User user = userService.loginUser(email, password);

            if (user == null) {
                System.out.println("Invalid email or password.");
                return;
            }

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                adminMenu(user);
            } else if ("COLLECTOR".equalsIgnoreCase(user.getRole())) {
                collectorMenu(user);
            } else {
                userMenu(user);
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    // ================= USER MENU =================

    private void userMenu(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n========== USER MENU ==========");
            System.out.println("1. View My Profile");
            System.out.println("2. Create Collection Request");
            System.out.println("3. View My Collection Requests");
            System.out.println("4. Cancel Collection Request");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            switch (readInt()) {
                case 1 -> displayUser(user);

                case 2 -> createRequest(user);

                case 3 -> {
                    try {
                        displayRequests(
                                requestService.getMyRequests(user)
                        );
                    } catch (Exception e) {
                        showError(e);
                    }
                }

                case 4 -> cancelRequest(user);

                case 5 -> loggedIn = false;

                default -> System.out.println("Invalid choice!");
            }
        }
    }

    // ================= COLLECTOR MENU =================

    private void collectorMenu(User collector) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n========== COLLECTOR MENU ==========");
            System.out.println("1. View My Assigned Requests");
            System.out.println("2. Mark Request as Collected");
            System.out.println("3. View My Profile");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");

            switch (readInt()) {
                case 1 -> {
                    try {
                        displayRequests(
                                requestService.getMyAssignedRequests(collector)
                        );
                    } catch (Exception e) {
                        showError(e);
                    }
                }

                case 2 -> markAsCollected(collector);

                case 3 -> displayUser(collector);

                case 4 -> loggedIn = false;

                default -> System.out.println("Invalid choice!");
            }
        }
    }

    // ================= ADMIN MENU =================

    private void adminMenu(User admin) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("1. View Dashboard Statistics");
            System.out.println("2. View Waste Type Report");
            System.out.println("3. View All Users");
            System.out.println("4. Update User Name");
            System.out.println("5. Delete User");
            System.out.println("6. View All Collection Requests");
            System.out.println("7. Update Collection Request Status");
            System.out.println("8. Assign Collector");
            System.out.println("9. Logout");
            System.out.print("Enter choice: ");

            switch (readInt()) {
                case 1 -> {
                    try {
                        dashboardService.displayDashboard(admin);
                    } catch (Exception e) {
                        showError(e);
                    }
                }

                case 2 -> {
                    try {
                        dashboardService.displayWasteTypeReport(admin);
                    } catch (Exception e) {
                        showError(e);
                    }
                }

                case 3 -> viewAllUsers(admin);

                case 4 -> updateUserName(admin);

                case 5 -> deleteUser(admin);

                case 6 -> viewAllRequests(admin);

                case 7 -> updateRequestStatus(admin);

                case 8 -> assignCollector(admin);

                case 9 -> loggedIn = false;

                default -> System.out.println("Invalid choice!");
            }
        }
    }

    // ================= USER MANAGEMENT =================

    private void viewAllUsers(User admin) {
        try {
            List<User> users = userService.getAllUsers(admin);

            if (users.isEmpty()) {
                System.out.println("No users found.");
                return;
            }

            System.out.println("\n========== ALL USERS ==========");

            for (User user : users) {
                System.out.println("--------------------------------");
                System.out.println("User ID : " + user.getUserId());
                System.out.println("Name    : " + user.getName());
                System.out.println("Email   : " + user.getEmail());
                System.out.println("Role    : " + user.getRole());
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    private void updateUserName(User admin) {
        System.out.print("Enter user ID: ");
        Long userId = readLong();

        System.out.print("Enter new name: ");
        String newName = scanner.nextLine().trim();

        if (newName.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        try {
            boolean updated =
                    userService.updateUserName(admin, userId, newName);

            System.out.println(updated
                    ? "User name updated successfully."
                    : "User not found or update failed.");

        } catch (Exception e) {
            showError(e);
        }
    }

    private void deleteUser(User admin) {
        System.out.print("Enter user ID to delete: ");
        Long userId = readLong();

        System.out.print("Confirm deletion? (yes/no): ");
        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        try {
            boolean deleted = userService.deleteUser(admin, userId);

            System.out.println(deleted
                    ? "User deleted successfully."
                    : "User not found or could not be deleted.");

        } catch (Exception e) {
            showError(e);
        }
    }

    // ================= REQUEST MANAGEMENT =================

    private void createRequest(User user) {
        System.out.print(
                "Enter waste type (E-Waste/Plastic/Metal/Paper/Other): "
        );
        String wasteType = scanner.nextLine().trim();

        System.out.print("Enter quantity (whole number): ");
        int quantity;

        try {
            quantity = Integer.parseInt(scanner.nextLine().trim());

            if (quantity <= 0) {
                System.out.println("Quantity must be greater than zero.");
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid whole number.");
            return;
        }

        System.out.print("Enter pickup address: ");
        String address = scanner.nextLine().trim();

        System.out.print("Enter description: ");
        String description = scanner.nextLine().trim();

        if (wasteType.isEmpty() || address.isEmpty()) {
            System.out.println(
                    "Waste type and pickup address cannot be empty."
            );
            return;
        }

        try {
            requestService.createRequest(
                    user,
                    wasteType,
                    quantity,
                    address,
                    description
            );

            System.out.println(
                    "Collection request submitted successfully!"
            );

        } catch (Exception e) {
            showError(e);
        }
    }

    private void cancelRequest(User user) {
        System.out.print("Enter request ID to cancel: ");
        Long requestId = readLong();

        try {
            requestService.cancelMyRequest(user, requestId);
            System.out.println("Cancellation request processed.");

        } catch (Exception e) {
            showError(e);
        }
    }

    private void viewAllRequests(User admin) {
        try {
            displayRequests(requestService.getAllRequests(admin));
        } catch (Exception e) {
            showError(e);
        }
    }

    private void updateRequestStatus(User admin) {
        System.out.print("Enter request ID: ");
        Long requestId = readLong();

        System.out.print(
                "Enter status (PENDING/ASSIGNED/COLLECTED/CANCELLED): "
        );
        String status = scanner.nextLine().trim().toUpperCase();

        if (!List.of(
                "PENDING", "ASSIGNED", "COLLECTED", "CANCELLED"
        ).contains(status)) {
            System.out.println("Invalid request status.");
            return;
        }

        try {
            requestService.updateRequestStatus(
                    admin, requestId, status
            );

            System.out.println("Request status update processed.");

        } catch (Exception e) {
            showError(e);
        }
    }

    private void assignCollector(User admin) {
        System.out.print("Enter request ID: ");
        Long requestId = readLong();

        try {
            List<User> collectors =
                    requestService.getAllCollectors(admin);

            if (collectors.isEmpty()) {
                System.out.println("No collectors are registered.");
                return;
            }

            System.out.println("\n========== AVAILABLE COLLECTORS ==========");

            for (User collector : collectors) {
                System.out.println(
                        "ID: " + collector.getUserId()
                                + " | Name: " + collector.getName()
                                + " | Email: " + collector.getEmail()
                );
            }

            System.out.print("Enter collector ID: ");
            Long collectorId = readLong();

            requestService.assignCollector(
                    admin, requestId, collectorId
            );

            System.out.println("Collector assignment processed.");

        } catch (Exception e) {
            showError(e);
        }
    }

    private void markAsCollected(User collector) {
        System.out.print("Enter request ID: ");
        Long requestId = readLong();

        try {
            requestService.markAsCollected(collector, requestId);
            System.out.println("Collection status update processed.");

        } catch (Exception e) {
            showError(e);
        }
    }

    // ================= DISPLAY METHODS =================

    private void displayUser(User user) {
        System.out.println("\n========== MY PROFILE ==========");
        System.out.println("User ID : " + user.getUserId());
        System.out.println("Name    : " + user.getName());
        System.out.println("Email   : " + user.getEmail());
        System.out.println("Role    : " + user.getRole());
    }

    private void displayRequests(List<CollectionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            System.out.println("No collection requests found.");
            return;
        }

        System.out.println("\n========== COLLECTION REQUESTS ==========");

        for (CollectionRequest request : requests) {
            System.out.println("------------------------------------------");
            System.out.println("Request ID     : " + request.getRequestId());
            System.out.println("Waste Type     : " + request.getWasteType());
            System.out.println("Quantity       : " + request.getQuantity());
            System.out.println("Pickup Address : " + request.getPickupAddress());
            System.out.println("Description    : " + request.getDescription());
            System.out.println("Status         : " + request.getStatus());

            if (request.getUser() != null) {
                System.out.println(
                        "User           : " + request.getUser().getName()
                );
            }

            if (request.getCollector() != null) {
                System.out.println(
                        "Collector      : "
                                + request.getCollector().getName()
                );
            }

            System.out.println("Created At     : " + request.getCreatedAt());
        }

        System.out.println("------------------------------------------");
    }

    // ================= INPUT AND ERROR HANDLING =================

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Long readLong() {
        while (true) {
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid numeric ID: ");
            }
        }
    }

    private void showError(Exception e) {
        System.out.println("Operation failed: " + e.getMessage());
    }
}
