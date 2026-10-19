package persistance;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Administrador centralizado de EntityManagerFactory para evitar fugas de conexiones
 * y permitir pruebas aisladas.
 */
public final class PersistenceManager {

    private static final String PERSISTENCE_UNIT_NAME = "persisventas";
    private static volatile EntityManagerFactory emf;
    private static volatile DatabaseConfig activeConfig;
    private static volatile String customStatusDescription;

    private PersistenceManager() {
    }

    public static synchronized void initialize(DatabaseConfig customConfig) {
        close();
        activeConfig = customConfig != null ? customConfig : DatabaseConfig.resolve();
        customStatusDescription = null;
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, activeConfig.toJpaProperties());
    }

    public static synchronized void initializeWithOverrides(Map<String, Object> jpaOverrides, String statusDescription) {
        close();
        activeConfig = DatabaseConfig.forInMemoryTest("serviserc_test_db");
        customStatusDescription = statusDescription;
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, jpaOverrides);
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            synchronized (PersistenceManager.class) {
                if (emf == null || !emf.isOpen()) {
                    if (activeConfig == null) {
                        activeConfig = DatabaseConfig.resolve();
                    }
                    emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, activeConfig.toJpaProperties());
                }
            }
        }
        return emf;
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static DatabaseConfig getActiveConfig() {
        getEntityManagerFactory();
        return activeConfig;
    }

    public static String getActiveStatusDescription() {
        if (customStatusDescription != null && !customStatusDescription.isEmpty()) {
            return customStatusDescription;
        }
        DatabaseConfig cfg = getActiveConfig();
        return cfg != null ? cfg.getDisplayLabel() : "Persistencia Activa";
    }

    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            try {
                emf.close();
            } catch (Exception ignored) {
            }
        }
        emf = null;
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Throwable ignored) {
        }
    }

    public static synchronized void shutdown() {
        close();
    }
}
