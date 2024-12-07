package DAO;  

import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.SQLException;  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;

import utils.PoiMySQLUtils;  

public class MySQLConnection {  
    private static MySQLConnection instance;  
    private Connection connection;  
    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLConnection.class);  

    private MySQLConnection() {  
        try {  
            Class.forName("com.mysql.cj.jdbc.Driver");  
            String url = String.format("jdbc:mysql://%s:%d/%s",   
                PoiMySQLUtils.getMysqlDbHost(),   
                PoiMySQLUtils.getMysqlDbPort(),   
                PoiMySQLUtils.getMysqlDbName());  
            
            LOGGER.info("Attempting to connect to MySQL with URL: {}", url);  
            LOGGER.info("Using user: {}", PoiMySQLUtils.getMysqlDbUser());

            connection = DriverManager.getConnection(url,   
            		PoiMySQLUtils.getMysqlDbUser(),   
            		PoiMySQLUtils.getMysqlDbPassword());  

            LOGGER.info("MySQL connection established to database: {}", PoiMySQLUtils.getMysqlDbName());  
        } catch (ClassNotFoundException | SQLException e) {  
            LOGGER.error("Failed to initialize MySQL connection", e);  
            throw new RuntimeException("MySQL initialization error", e);  
        }  
    }  

    public static MySQLConnection getInstance() {  
        if (instance == null) {  
            synchronized (MySQLConnection.class) {  
                if (instance == null) {  
                    instance = new MySQLConnection();  
                }  
            }  
        }  
        return instance;  
    }  

    public Connection getConnection() {  
        if (connection == null) {  
            throw new IllegalStateException("MySQL Connection is not initialized.");  
        }  
        return connection;  
    }  

    public static synchronized void closeInstance() {  
        if (instance != null && instance.connection != null) {  
            try {  
                instance.connection.close();  
                LOGGER.info("MySQL connection closed.");  
            } catch (SQLException e) {  
                LOGGER.error("Error while closing MySQL connection", e);  
            } finally {  
                instance.connection = null;  
                instance = null;  
            }  
        }  
    }  
}  