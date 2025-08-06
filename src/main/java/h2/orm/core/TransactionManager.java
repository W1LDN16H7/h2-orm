package h2.orm.core;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core transaction manager for H2-ORM
 * Handles transaction lifecycle and ensures proper rollback on errors
 */
public class TransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    /**
     * Execute operation in transaction with automatic rollback on error
     */
    public static <T> T executeInTransaction(EntityManagerProvider.TransactionCallback<T> callback) {
        return EntityManagerProvider.executeInTransaction(callback);
    }

    /**
     * Execute operation in transaction with named configuration
     */
    public static <T> T executeInTransaction(String configName, EntityManagerProvider.TransactionCallback<T> callback) {
        return EntityManagerProvider.executeInTransaction(configName, callback);
    }

    /**
     * Execute void operation in transaction
     */
    public static void executeInTransaction(VoidTransactionCallback callback) {
        EntityManagerProvider.executeInTransaction(em -> {
            callback.execute(em);
            return null;
        });
    }

    /**
     * Check if current thread has active transaction
     */
    public static boolean hasActiveTransaction() {
        try {
            EntityManager em = EntityManagerProvider.getEntityManager();
            return em.getTransaction().isActive();
        } catch (Exception e) {
            return false;
        } finally {
            EntityManagerProvider.closeEntityManager();
        }
    }

    /**
     * Begin transaction manually (advanced usage)
     */
    public static void beginTransaction() {
        EntityManager em = EntityManagerProvider.getEntityManager();
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            logger.debug("Transaction started manually");
        }
    }

    /**
     * Commit transaction manually (advanced usage)
     */
    public static void commitTransaction() {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
                logger.debug("Transaction committed manually");
            }
        } finally {
            EntityManagerProvider.closeEntityManager();
        }
    }

    /**
     * Rollback transaction manually (advanced usage)
     */
    public static void rollbackTransaction() {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                logger.debug("Transaction rolled back manually");
            }
        } finally {
            EntityManagerProvider.closeEntityManager();
        }
    }

    @FunctionalInterface
    public interface VoidTransactionCallback {
        void execute(EntityManager entityManager) throws Exception;
    }
}
