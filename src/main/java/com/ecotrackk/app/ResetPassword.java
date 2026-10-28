
package com.ecotrackk.app;

import com.ecotrackk.entity.User;
import com.ecotrackk.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.mindrot.jbcrypt.BCrypt;

public class ResetPassword {

    public static void main(String[] args) {

        EntityManager em = JPAUtil
                .getEntityManagerFactory()
                .createEntityManager();

        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            User user = em.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email",
                            User.class
                    )
                    .setParameter("email", "shrikant@gmail.com")
                    .getSingleResult();

            String newPassword = "Shrikant@123";

            String hashedPassword = BCrypt.hashpw(
                    newPassword,
                    BCrypt.gensalt(12)
            );

            user.setPasswordHash(hashedPassword);

            tx.commit();

            System.out.println("Password reset successfully!");
            System.out.println("Email: shrikant@gmail.com");
            System.out.println("New password: Shrikant@123");

        } catch (Exception e) {

            if (tx.isActive()) {
                tx.rollback();
            }

            System.out.println(
                    "Password reset failed: " + e.getMessage()
            );

        } finally {
            em.close();
        }
    }
}
