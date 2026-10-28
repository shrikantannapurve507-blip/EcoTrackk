package com.ecotrackk.service;

import com.ecotrackk.dao.UserDAO;
import com.ecotrackk.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    // Register a normal user
    public void registerUser(
            String name,
            String email,
            String password
    ) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Name cannot be empty!");
            return;
        }

        if (email == null || !email.trim().contains("@")) {
            System.out.println("Enter a valid email!");
            return;
        }

        if (password == null || password.length() < 8) {
            System.out.println(
                    "Password must be at least 8 characters!"
            );
            return;
        }

        String cleanEmail = email.trim().toLowerCase();

        if (userDAO.emailExists(cleanEmail)) {
            System.out.println("Email already registered!");
            return;
        }

        String hashedPassword = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        User user = new User();
        user.setName(name.trim());
        user.setEmail(cleanEmail);

        // Store the hashed password, never plain text
        user.setPasswordHash(hashedPassword);

        // Public registration can create USER accounts only
        user.setRole("USER");

        userDAO.addUser(user);
    }

    // Login
    public User loginUser(String email, String password) {
        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            System.out.println("Email and password are required!");
            return null;
        }

        String cleanEmail = email.trim().toLowerCase();
        User user = userDAO.findUserByEmail(cleanEmail);

        if (user == null) {
            System.out.println("Invalid email or password!");
            return null;
        }

        String storedHash = user.getPasswordHash();

        if (storedHash == null || !storedHash.startsWith("$2")) {
            System.out.println(
                    "This account needs a password reset before login."
            );
            return null;
        }

        if (BCrypt.checkpw(password, storedHash)) {
            return user;
        }

        System.out.println("Invalid email or password!");
        return null;
    }

    // Check whether the logged-in account is an admin
    public boolean isAdmin(User currentUser) {
        return currentUser != null
                && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    // Admin-only: view all users
    public List<User> getAllUsers(User currentUser) {
        if (!isAdmin(currentUser)) {
            throw new SecurityException(
                    "Access denied! Admin permission required."
            );
        }

        return userDAO.getAllUsers();
    }

    // Admin-only: update a user's name
    public boolean updateUserName(
            User currentUser,
            Long userId,
            String newName
    ) {
        if (!isAdmin(currentUser)) {
            throw new SecurityException(
                    "Access denied! Admin permission required."
            );
        }

        if (userId == null || userId <= 0
                || newName == null || newName.trim().isEmpty()) {
            System.out.println("Invalid user ID or name!");
            return false;
        }

        return userDAO.updateUserName(userId, newName.trim());
    }

    // Admin-only: delete a user
    public boolean deleteUser(User currentUser, Long userId) {
        if (!isAdmin(currentUser)) {
            throw new SecurityException(
                    "Access denied! Admin permission required."
            );
        }

        if (userId == null || userId <= 0) {
            System.out.println("Invalid user ID!");
            return false;
        }

        // Prevent an admin from deleting their own account
        if (currentUser.getUserId().equals(userId)) {
            System.out.println("You cannot delete your own account!");
            return false;
        }

        return userDAO.deleteUser(userId);
    }
}
