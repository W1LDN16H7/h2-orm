package h2.orm.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Core query executor for H2-ORM
 * Provides optimized query execution with proper resource management
 */
public class QueryExecutor {

    private static final Logger logger = LoggerFactory.getLogger(QueryExecutor.class);

    /**
     * Execute JPQL query and return results
     */
    public static <T> List<T> executeQuery(String jpql, Class<T> resultClass) {
        return executeQuery(jpql, resultClass, null);
    }

    /**
     * Execute JPQL query with parameters and return results
     */
    public static <T> List<T> executeQuery(String jpql, Class<T> resultClass, Map<String, Object> parameters) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            TypedQuery<T> query = em.createQuery(jpql, resultClass);

            if (parameters != null) {
                parameters.forEach(query::setParameter);
            }

            List<T> results = query.getResultList();
            logger.debug("JPQL query executed: {} rows returned", results.size());
            return results;

        } finally {
            EntityManagerProvider.closeEntityManager();
        }
    }

    /**
     * Execute native SQL query and return results
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> executeNativeQuery(String sql, Class<T> resultClass) {
        return executeNativeQuery(sql, resultClass, null);
    }

    /**
     * Execute native SQL query with parameters and return results
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> executeNativeQuery(String sql, Class<T> resultClass, Map<String, Object> parameters) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            Query query = em.createNativeQuery(sql, resultClass);

            if (parameters != null) {
                parameters.forEach(query::setParameter);
            }

            List<T> results = query.getResultList();
            logger.debug("Native SQL query executed: {} rows returned", results.size());
            return results;

        } finally {
            EntityManagerProvider.closeEntityManager();
        }
    }

    /**
     * Execute update/delete JPQL query
     */
    public static int executeUpdate(String jpql) {
        return executeUpdate(jpql, null);
    }

    /**
     * Execute update/delete JPQL query with parameters
     */
    public static int executeUpdate(String jpql, Map<String, Object> parameters) {
        return TransactionManager.executeInTransaction(em -> {
            Query query = em.createQuery(jpql);

            if (parameters != null) {
                parameters.forEach(query::setParameter);
            }

            int rowsAffected = query.executeUpdate();
            logger.debug("Update query executed: {} rows affected", rowsAffected);
            return rowsAffected;
        });
    }

    /**
     * Execute native update/delete SQL
     */
    public static int executeNativeUpdate(String sql) {
        return executeNativeUpdate(sql, null);
    }

    /**
     * Execute native update/delete SQL with parameters
     */
    public static int executeNativeUpdate(String sql, Map<String, Object> parameters) {
        return TransactionManager.executeInTransaction(em -> {
            Query query = em.createNativeQuery(sql);

            if (parameters != null) {
                parameters.forEach(query::setParameter);
            }

            int rowsAffected = query.executeUpdate();
            logger.debug("Native update query executed: {} rows affected", rowsAffected);
            return rowsAffected;
        });
    }

    /**
     * Execute query and return single result
     */
    public static <T> T executeSingleResultQuery(String jpql, Class<T> resultClass) {
        return executeSingleResultQuery(jpql, resultClass, null);
    }

    /**
     * Execute query with parameters and return single result
     */
    public static <T> T executeSingleResultQuery(String jpql, Class<T> resultClass, Map<String, Object> parameters) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            TypedQuery<T> query = em.createQuery(jpql, resultClass);

            if (parameters != null) {
                parameters.forEach(query::setParameter);
            }

            return query.getSingleResult();

        } finally {
            EntityManagerProvider.closeEntityManager();
        }
    }

    /**
     * Execute paginated query
     */
    public static <T> List<T> executePaginatedQuery(String jpql, Class<T> resultClass, int firstResult, int maxResults) {
        return executePaginatedQuery(jpql, resultClass, null, firstResult, maxResults);
    }

    /**
     * Execute paginated query with parameters
     */
    public static <T> List<T> executePaginatedQuery(String jpql, Class<T> resultClass, Map<String, Object> parameters, int firstResult, int maxResults) {
        EntityManager em = EntityManagerProvider.getEntityManager();
        try {
            TypedQuery<T> query = em.createQuery(jpql, resultClass);

            if (parameters != null) {
                parameters.forEach(query::setParameter);
            }

            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);

            List<T> results = query.getResultList();
            logger.debug("Paginated query executed: {} rows returned (offset: {}, limit: {})",
                        results.size(), firstResult, maxResults);
            return results;

        } finally {
            EntityManagerProvider.closeEntityManager();
        }
    }
}
