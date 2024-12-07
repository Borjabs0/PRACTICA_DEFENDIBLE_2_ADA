package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PoiMySQLUtils {
    private static final PasswordEncryptionUtil PASSWORD_ENCRYPTION_UTIL = new PasswordEncryptionUtil();

    private static final String CONFIG_FILE_PATH = "src" + File.separator + "main" + File.separator + "resources"
            + File.separator + "mysql_connection_properties" + File.separator + "mysql_data_connection.properties";

    // Configuración predeterminada y propiedades clave para MySQL
    private static final String MYSQL_DEFAULT_DB_HOST = "localhost";
    private static final int MYSQL_DEFAULT_DB_PORT = 3306;
    private static final String MYSQL_DEFAULT_DB_NAME = "poi_db";
    private static final String MYSQL_DEFAULT_DB_USER = "root";
    private static final String MYSQL_DEFAULT_DB_PASSWORD = "1234";

    private static final String MYSQL_DB_HOST_KEY = "mysql.db.host";
    private static final String MYSQL_DB_PORT_KEY = "mysql.db.port";
    private static final String MYSQL_DB_NAME_KEY = "mysql.db.name";
    private static final String MYSQL_DB_USER_KEY = "mysql.db.user";
    private static final String MYSQL_DB_PASSWORD_KEY = "mysql.db.password";

    // Variables para almacenar los valores actuales de configuración
    private static String mysqlDbHost = MYSQL_DEFAULT_DB_HOST;
    private static int mysqlDbPort = MYSQL_DEFAULT_DB_PORT;
    private static String mysqlDbName = MYSQL_DEFAULT_DB_NAME;
    private static String mysqlDbUser = MYSQL_DEFAULT_DB_USER;
    private static String mysqlDbPassword = MYSQL_DEFAULT_DB_PASSWORD;

    // Clave y algoritmo para la encriptación de contraseñas
    private static final String ENCRYPTION_KEY = "MySuperSecretKey"; // Clave de 16 bytes para AES
    private static final String ALGORITHM = "AES";

    // Getters para MySQL
    public static String getMysqlDbHost() {
        return mysqlDbHost;
    }

    public static int getMysqlDbPort() {
        return mysqlDbPort;
    }

    public static String getMysqlDbName() {
        return mysqlDbName;
    }

    public static String getMysqlDbUser() {
        return mysqlDbUser;
    }

    public static String getMysqlDbPassword() {
        return mysqlDbPassword;
    }

    static {
        Properties properties = new Properties();
        try (InputStream input = PoiMySQLUtils.class.getClassLoader().getResourceAsStream(CONFIG_FILE_PATH)) {
            if (input == null) {
                throw new IOException("Unable to find configuration file: " + CONFIG_FILE_PATH);
            }
            properties.load(input);

            mysqlDbHost = properties.getProperty(MYSQL_DB_HOST_KEY, MYSQL_DEFAULT_DB_HOST);
            mysqlDbPort = Integer.parseInt(properties.getProperty(MYSQL_DB_PORT_KEY, String.valueOf(MYSQL_DEFAULT_DB_PORT)));
            mysqlDbName = properties.getProperty(MYSQL_DB_NAME_KEY, MYSQL_DEFAULT_DB_NAME);
            mysqlDbUser = properties.getProperty(MYSQL_DB_USER_KEY, MYSQL_DEFAULT_DB_USER);

            // Decodificación y desencriptación de la contraseña
            String encryptedPassword = properties.getProperty(MYSQL_DB_PASSWORD_KEY, "");
            mysqlDbPassword = encryptedPassword.isEmpty() ? MYSQL_DEFAULT_DB_PASSWORD
                    : PASSWORD_ENCRYPTION_UTIL.decryptPassword(encryptedPassword);

        } catch (IOException e) {
            // En caso de error al cargar las propiedades, se usan valores predeterminados
            System.err.println("Failed to load properties file. Using default configuration. " + e.getMessage());
            mysqlDbHost = MYSQL_DEFAULT_DB_HOST;
            mysqlDbPort = MYSQL_DEFAULT_DB_PORT;
            mysqlDbName = MYSQL_DEFAULT_DB_NAME;
            mysqlDbUser = MYSQL_DEFAULT_DB_USER;
            mysqlDbPassword = MYSQL_DEFAULT_DB_PASSWORD;
        }
    }
}