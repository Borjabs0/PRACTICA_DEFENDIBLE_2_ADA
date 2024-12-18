package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;

import DAO.MongoConnection;

/**
 * La clase PoiUtils proporciona utilidades relacionadas con la configuración de
 * conexiones a bases de datos MongoDB, incluyendo la encriptación y
 * desencriptación de contraseñas.
 */
public class PoiMongoUtils {
	
	private static final PasswordEncryptionUtil PASSWORD_ENCRYPTION_UTIL= new PasswordEncryptionUtil();

	/**
	 * Formato de fecha predeterminado
	 */
	public static final String DATE_FORMAT = "yyyy-mm-dd";

	// Configuracion preedeterminada y propiedades clave
	private static final String CONFIG_FILE_PATH = "mongo_connection_properties/mongo_data_connection.properties";

	private static final String DEFAULT_DB_HOST = "localhost";
	private static final int DEFAULT_DB_PORT = 27017;
	private static final String DEFAULT_DB_NAME = "root";
	private static final String DEFAULT_DB_USER = "root";
	private static final String DEFAULT_DB_PASSWORD = "1234";

	private static final String DB_HOST_KEY = "mongo.db.host";
	private static final String DB_PORT_KEY = "mongo.db.port";
	private static final String DB_NAME_KEY = "mongo.db.name";
	private static final String DB_USER_KEY = "mongo.db.user";
	private static final String DB_PASSWORD_KEY = "mongo.db.password";

	// Variables para almacenar los valores actuales de configuración
	private static String dbHost = DEFAULT_DB_HOST;
	private static int dbPort = DEFAULT_DB_PORT;
	private static String dbName = DEFAULT_DB_NAME;
	private static String dbUser = DEFAULT_DB_USER;
	private static String dbPassword = DEFAULT_DB_PASSWORD;

	/*
	 * Obtiene el host de la base de datos configurada.
	 * 
	 * @return el host de la base de datos
	 */
	public static String getDbHost() {
		return dbHost;
	}

	/**
	 * Obtiene el puerto de la base de datos configurada.
	 * 
	 * @return el puerto de la base de datos.
	 */
	public static int getDbPort() {
		return dbPort;
	}

	/**
	 * Obtiene el nombre de la base de datos configurada.
	 * 
	 * @return el nombre de la base de datos.
	 */
	public static String getDbName() {
		return dbName;
	}

	/**
	 * Obtiene el usuario de la base de datos configurada.
	 * 
	 * @return el usuario de la base de datos.
	 */
	public static String getDbUser() {
		return dbUser;
	}

	/**
	 * Obtiene la contraseña de la base de datos configurada.
	 * 
	 * @return la contraseña de la base de datos.
	 */
	public static String getDbPassword() {
		return dbPassword;
	}

	// Bloque estático para cargar la configuración desde un archivo de propiedades
	static {
		Properties properties = new Properties();
		try (InputStream input = PoiMongoUtils.class.getClassLoader().getResourceAsStream(CONFIG_FILE_PATH)) {
			if (input == null) {
				throw new IOException("Unable to find configuration file: " + CONFIG_FILE_PATH);
			}
			properties.load(input);

			dbHost = properties.getProperty(DB_HOST_KEY, DEFAULT_DB_HOST);
			dbPort = Integer.parseInt(properties.getProperty(DB_PORT_KEY, String.valueOf(DEFAULT_DB_PORT)));
			dbName = properties.getProperty(DB_NAME_KEY, DEFAULT_DB_NAME);
			dbUser = properties.getProperty(DB_USER_KEY, DEFAULT_DB_USER);
			String encryptedPassword = properties.getProperty(DB_PASSWORD_KEY, "");
			dbPassword = encryptedPassword.isEmpty() ? DEFAULT_DB_PASSWORD : PASSWORD_ENCRYPTION_UTIL.decryptPassword(encryptedPassword);
		} catch (IOException e) {
			// En caso de excepcion se usan valores predeterminados
			System.err.println("Failed to load properties file. Using default configuration. " + e.getMessage());
			dbHost = DEFAULT_DB_HOST;
			dbPort = DEFAULT_DB_PORT;
			dbName = DEFAULT_DB_NAME;
			dbUser = DEFAULT_DB_USER;
			dbPassword = DEFAULT_DB_PASSWORD;
		}
	}
}
