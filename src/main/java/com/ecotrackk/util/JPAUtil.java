package com.ecotrackk.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

    private static final EntityManagerFactory factory =
            Persistence.createEntityManagerFactory("EcoTrackkPU");

    private JPAUtil() {
        // Prevent object creation
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return factory;
    }

    public static void shutdown() {
        if (factory.isOpen()) {
            factory.close();
        }
    }
}
