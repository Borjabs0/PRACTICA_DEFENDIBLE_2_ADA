package DAO;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import utils.PoiMongoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Level;

import java.util.Collections;

/**
 * Singleton para manejar la conexión a MongoDB.
 */
public class MongoConnection {

    // Instancia única de la clase
    private static MongoConnection instance;

    // Cliente y base de datos
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // Logger para mensajes
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoConnection.class);

    static {
        // Reducir nivel de log para el driver de MongoDB usando Logback
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("org.mongodb.driver").setLevel(Level.ERROR);
    }

    /**
     * Constructor privado para evitar instanciación externa.
     */
    private MongoConnection() {
        try {
            mongoClient = createMongoClient();
            database = mongoClient.getDatabase(PoiMongoUtils.getDbName());
            LOGGER.info("MongoDB connection established to database: {}", PoiMongoUtils.getDbName());
        } catch (Exception e) {
            LOGGER.error("Failed to initialize MongoDB connection", e);
            throw new RuntimeException("MongoDB initialization error", e);
        }
    }

    /**
     * Obtiene la instancia única del Singleton.
     *
     * @return instancia de `MongoConnection`.
     */
    public static MongoConnection getInstance() {
        if (instance == null) {
            synchronized (MongoConnection.class) {
                if (instance == null) {
                    instance = new MongoConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Crea el cliente MongoDB usando las configuraciones de `PoiUtils`.
     *
     * @return una instancia configurada de `MongoClient`.
     */
    private static MongoClient createMongoClient() {
        String host = PoiMongoUtils.getDbHost();
        int port = PoiMongoUtils.getDbPort();
        String username = PoiMongoUtils.getDbUser();
        String password = PoiMongoUtils.getDbPassword();
        String dbName = PoiMongoUtils.getDbName();

        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            MongoCredential credential = MongoCredential.createCredential(username, dbName, password.toCharArray());
            LOGGER.info("Using authenticated MongoDB connection.");
            return MongoClients.create(MongoClientSettings.builder()
                    .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(host, port))))
                    .credential(credential)
                    .build());
        } else {
            LOGGER.info("Using unauthenticated MongoDB connection.");
            return MongoClients.create(MongoClientSettings.builder()
                    .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(host, port))))
                    .build());
        }
    }

    /**
     * Devuelve la instancia de la base de datos.
     *
     * @return instancia de `MongoDatabase`.
     */
    public MongoDatabase getDatabase() {
        if (database == null) {
            throw new IllegalStateException("MongoDatabase is not initialized.");
        }
        return database;
    }

    /**
     * Cierra la conexión con MongoDB y limpia las referencias.
     */
    public static synchronized void closeInstance() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                LOGGER.info("MongoDB connection closed.");
            } catch (Exception e) {
                LOGGER.error("Error while closing MongoDB connection", e);
            } finally {
                mongoClient = null;
                database = null;
                instance = null;
            }
        }
    }
}
