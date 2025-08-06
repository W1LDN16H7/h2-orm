package h2.orm.core.repository;

import h2.orm.core.EntityManagerProvider;
import jakarta.persistence.EntityManager;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Repository factory that creates JPA repositories automatically
 * Works exactly like Spring Data JPA - just call Repositories.of(EntityClass.class)
 * Now uses core EntityManagerProvider for better performance
 */
public class Repositories {

    private static final Map<Class<?>, JpaRepository<?, ?>> repositoryCache = new ConcurrentHashMap<>();

    /**
     * Get repository for entity class - creates it automatically if needed
     * Usage: UserRepository userRepo = Repositories.of(User.class);
     */
    @SuppressWarnings("unchecked")
    public static <T, ID extends Serializable> JpaRepository<T, ID> of(Class<T> entityClass) {
        return (JpaRepository<T, ID>) repositoryCache.computeIfAbsent(entityClass,
            clazz -> new SimpleJpaRepository<>((Class<T>) clazz));
    }

    /**
     * Clear repository cache
     */
    public static void clearCache() {
        repositoryCache.clear();
    }

    /**
     * Simple JPA Repository implementation using core EntityManagerProvider
     */
    private static class SimpleJpaRepository<T, ID extends Serializable> extends AbstractRepository<T, ID> {

        public SimpleJpaRepository(Class<T> entityClass) {
            super(entityClass);
        }

        @Override
        protected EntityManager getEntityManager() {
            return EntityManagerProvider.getEntityManager();
        }
    }
}
