package DAO;

import interfaces.IDAO;
import models.POI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementación de IDAO para interactuar con una base de datos MySQL.
 */
public class PoiMySQLDAO implements IDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoiMySQLDAO.class);
    private final Connection connection;

    /**
     * Constructor que inicializa la conexión y asegura la existencia de la tabla.
     */
    public PoiMySQLDAO() {
        this.connection = MySQLConnection.getInstance().getConnection();
        createTableIfNotExists();
    }

    /**
     * Crea la tabla 'pois' si no existe.
     */
    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS pois (
                id INT AUTO_INCREMENT PRIMARY KEY,
                latitude DOUBLE,
                longitude DOUBLE,
                country VARCHAR(100),
                city VARCHAR(100),
                description TEXT,
                updated_date DATE
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.error("Error creating table", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addPoi(POI poi) {
        String sql = "INSERT INTO pois (latitude, longitude, country, city, description, updated_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, poi.getLatitude());
            pstmt.setDouble(2, poi.getLongitude());
            pstmt.setString(3, poi.getCountry());
            pstmt.setString(4, poi.getCity());
            pstmt.setString(5, poi.getDescription());
            pstmt.setDate(6, new java.sql.Date(poi.getUpdatedDate().getTime()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error adding POI", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addPois(List<POI> pois) {
        String sql = "INSERT INTO pois (latitude, longitude, country, city, description, updated_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (POI poi : pois) {
                pstmt.setDouble(1, poi.getLatitude());
                pstmt.setDouble(2, poi.getLongitude());
                pstmt.setString(3, poi.getCountry());
                pstmt.setString(4, poi.getCity());
                pstmt.setString(5, poi.getDescription());
                pstmt.setDate(6, new java.sql.Date(poi.getUpdatedDate().getTime()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            LOGGER.error("Error adding multiple POIs", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public POI removePoi(int id) {
        String sql = "DELETE FROM pois WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0 ? new POI(id, null, null, null, null, null, null) : null;
        } catch (SQLException e) {
            LOGGER.error("Error removing POI", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<POI> listAllPois() {
        String sql = "SELECT * FROM pois";
        List<POI> pois = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pois.add(resultSetToPOI(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving all POIs", e);
            throw new RuntimeException(e);
        }
        return pois;
    }

    @Override
    public POI listByID(int id) {
        String sql = "SELECT * FROM pois WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToPOI(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving POI by ID", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<POI> listByIDRange(int min, int max) {
        String sql = "SELECT * FROM pois WHERE id BETWEEN ? AND ?";
        List<POI> pois = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, min);
            pstmt.setInt(2, max);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pois.add(resultSetToPOI(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving POIs by ID range", e);
            throw new RuntimeException(e);
        }
        return pois;
    }

    @Override
    public List<POI> listByMonth(Date date) {
        String sql = "SELECT * FROM pois WHERE MONTH(updated_date) = MONTH(?) AND YEAR(updated_date) = YEAR(?)";
        List<POI> pois = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            pstmt.setDate(1, sqlDate);
            pstmt.setDate(2, sqlDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pois.add(resultSetToPOI(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving POIs by month", e);
            throw new RuntimeException(e);
        }
        return pois;
    }

    @Override
    public List<POI> listByCity(String city) {
        String sql = "SELECT * FROM pois WHERE city = ?";
        List<POI> pois = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, city);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pois.add(resultSetToPOI(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving POIs by city", e);
            throw new RuntimeException(e);
        }
        return pois;
    }

    @Override
    public POI updateById(int id, POI newPoiData) {
        // SQL para actualizar los datos del POI
        String sql = "UPDATE pois SET latitude = ?, longitude = ?, country = ?, city = ?, description = ?, updated_date = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Establecer los valores para los parámetros
            pstmt.setDouble(1, newPoiData.getLatitude());
            pstmt.setDouble(2, newPoiData.getLongitude());
            pstmt.setString(3, newPoiData.getCountry());
            pstmt.setString(4, newPoiData.getCity());
            pstmt.setString(5, newPoiData.getDescription());
            pstmt.setDate(6, new java.sql.Date(newPoiData.getUpdatedDate().getTime()));
            pstmt.setInt(7, id);

            // Ejecutar la actualización
            int rowsAffected = pstmt.executeUpdate();

            // Verificar si la actualización fue exitosa
            if (rowsAffected > 0) {
                return listByID(id); // Retornar el POI actualizado desde la base de datos
            } else {
                return null; // Retornar null si no se encontró el POI
            }
        } catch (SQLException e) {
            LOGGER.error("Error updating POI with ID " + id, e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public int removeAllItemFilters() {
        String sql = "DELETE FROM pois";
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            LOGGER.error("Error removing all POIs", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public POI removeByID(int id) {
        return removePoi(id);
    }

    @Override
    public List<POI> removeByIDRange(int min, int max) {
        List<POI> pois = listByIDRange(min, max);
        for (POI poi : pois) {
            removePoi(poi.getId());
        }
        return pois;
    }

    @Override
    public List<POI> removeByMonth(Date date) {
        List<POI> pois = listByMonth(date);
        for (POI poi : pois) {
            removePoi(poi.getId());
        }
        return pois;
    }

    @Override
    public List<POI> removeByCity(String city) {
        List<POI> pois = listByCity(city);
        for (POI poi : pois) {
            removePoi(poi.getId());
        }
        return pois;
    }

    // Método auxiliar para transformar un ResultSet a un POI
    private POI resultSetToPOI(ResultSet rs) throws SQLException {
        return new POI(
                rs.getInt("id"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getString("country"),
                rs.getString("city"),
                rs.getString("description"),
                rs.getDate("updated_date")
        );
    }
}