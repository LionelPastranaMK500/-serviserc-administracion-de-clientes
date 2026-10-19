package persistance;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Gestiona la configuración de conexión a base de datos separando las credenciales
 * del código fuente y soportando MySQL así como modo local embebido (H2 en modo MySQL).
 */
public class DatabaseConfig {

    public static final String DEFAULT_MYSQL_URL =
            "jdbc:mysql://127.0.0.1:3306/serviserc?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    public static final String DEFAULT_MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DEFAULT_MYSQL_USER = "root";

    public static final String DEFAULT_H2_FILE_URL =
            "jdbc:h2:file:./data/serviserc_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;AUTO_SERVER=TRUE";
    public static final String DEFAULT_H2_DRIVER = "org.h2.Driver";
    public static final String DEFAULT_H2_USER = "sa";
    public static final String DEFAULT_H2_PASSWORD = "";

    private final String mode;
    private final String jdbcUrl;
    private final String jdbcUser;
    private final String jdbcPassword;
    private final String jdbcDriver;
    private final boolean mySqlActive;

    public DatabaseConfig(String mode, String jdbcUrl, String jdbcUser, String jdbcPassword, String jdbcDriver, boolean mySqlActive) {
        this.mode = mode;
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;
        this.jdbcPassword = jdbcPassword;
        this.jdbcDriver = jdbcDriver;
        this.mySqlActive = mySqlActive;
    }

    public static DatabaseConfig resolve() {
        Properties fileProps = loadOptionalPropertiesFile();

        String mode = readSetting("serviserc.db.mode", "SERVISERC_DB_MODE", fileProps, "auto").trim().toLowerCase();
        String configuredUrl = readSetting("serviserc.db.url", "SERVISERC_DB_URL", fileProps, "");
        String configuredUser = readSetting("serviserc.db.user", "SERVISERC_DB_USER", fileProps, "");
        String configuredPassword = readSetting("serviserc.db.password", "SERVISERC_DB_PASSWORD", fileProps, "");
        String configuredDriver = readSetting("serviserc.db.driver", "SERVISERC_DB_DRIVER", fileProps, "");

        if ("embedded".equals(mode) || "h2".equals(mode)) {
            String url = configuredUrl.isEmpty() ? DEFAULT_H2_FILE_URL : configuredUrl;
            String user = configuredUser.isEmpty() ? DEFAULT_H2_USER : configuredUser;
            String pass = configuredPassword;
            String driver = configuredDriver.isEmpty() ? DEFAULT_H2_DRIVER : configuredDriver;
            return new DatabaseConfig("embedded", url, user, pass, driver, false);
        }

        String mysqlUrl = configuredUrl.isEmpty() ? DEFAULT_MYSQL_URL : configuredUrl;
        String mysqlUser = configuredUser.isEmpty() ? DEFAULT_MYSQL_USER : configuredUser;
        String mysqlPass = configuredPassword;
        String mysqlDriver = configuredDriver.isEmpty() ? DEFAULT_MYSQL_DRIVER : configuredDriver;

        if ("mysql".equals(mode)) {
            return new DatabaseConfig("mysql", mysqlUrl, mysqlUser, mysqlPass, mysqlDriver, true);
        }

        // Modo "auto": verifica si MySQL está accesible; de lo contrario usa H2 local en modo compatible MySQL
        if (isMySqlReachable(mysqlUrl, mysqlUser, mysqlPass)) {
            return new DatabaseConfig("mysql", mysqlUrl, mysqlUser, mysqlPass, mysqlDriver, true);
        }

        return new DatabaseConfig(
                "embedded",
                DEFAULT_H2_FILE_URL,
                DEFAULT_H2_USER,
                DEFAULT_H2_PASSWORD,
                DEFAULT_H2_DRIVER,
                false
        );
    }

    public static DatabaseConfig forInMemoryTest(String dbName) {
        String url = "jdbc:h2:mem:" + dbName + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1";
        return new DatabaseConfig("test-mem", url, DEFAULT_H2_USER, DEFAULT_H2_PASSWORD, DEFAULT_H2_DRIVER, false);
    }

    public Map<String, String> toJpaProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("javax.persistence.jdbc.url", jdbcUrl);
        props.put("javax.persistence.jdbc.user", jdbcUser);
        props.put("javax.persistence.jdbc.password", jdbcPassword);
        props.put("javax.persistence.jdbc.driver", jdbcDriver);
        props.put("eclipselink.ddl-generation", "create-or-extend-tables");
        props.put("eclipselink.target-database", "MySQL");
        props.put("eclipselink.logging.level", "SEVERE");
        return props;
    }

    private static Properties loadOptionalPropertiesFile() {
        Properties props = new Properties();
        Path configPath = Paths.get("config", "database.properties");
        if (Files.isRegularFile(configPath)) {
            try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                props.load(fis);
            } catch (IOException ignored) {
                // Se continúa con valores por defecto o variables de entorno
            }
        }
        return props;
    }

    private static String readSetting(String sysProp, String envVar, Properties fileProps, String defaultValue) {
        String val = System.getProperty(sysProp);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        val = System.getenv(envVar);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        val = fileProps.getProperty(sysProp);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        return defaultValue;
    }

    private static boolean isMySqlReachable(String url, String user, String password) {
        try {
            // Comprobación rápida de puerto TCP antes de invocar DriverManager
            String host = "127.0.0.1";
            int port = 3306;
            if (url != null && url.startsWith("jdbc:mysql://")) {
                String remainder = url.substring("jdbc:mysql://".length());
                int slashIdx = remainder.indexOf('/');
                String hostPort = slashIdx >= 0 ? remainder.substring(0, slashIdx) : remainder;
                int colonIdx = hostPort.indexOf(':');
                if (colonIdx > 0) {
                    host = hostPort.substring(0, colonIdx);
                    port = Integer.parseInt(hostPort.substring(colonIdx + 1));
                } else if (!hostPort.isEmpty()) {
                    host = hostPort;
                }
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 350);
            }
            DriverManager.setLoginTimeout(1);
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                return conn != null && !conn.isClosed();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public String getMode() {
        return mode;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getJdbcUser() {
        return jdbcUser;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public boolean isMySqlActive() {
        return mySqlActive;
    }

    public String getDisplayLabel() {
        if (mySqlActive) {
            return "MySQL Server Activo (" + sanitizeUrl(jdbcUrl) + ")";
        }
        if ("test-mem".equals(mode)) {
            return "Base de Datos de Pruebas en Memoria (H2 Modo MySQL)";
        }
        return "Persistencia Local Activa (H2 Modo Compatible MySQL - ./data/serviserc_db)";
    }

    private static String sanitizeUrl(String url) {
        if (url == null) return "";
        int qIdx = url.indexOf('?');
        return qIdx > 0 ? url.substring(0, qIdx) : url;
    }
}
