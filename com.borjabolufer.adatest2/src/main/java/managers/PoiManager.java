package managers;

import java.util.List;

import DAO.MongoConnection;
import DAO.PoiMongoDAO;
import DAO.PoiMySQLDAO;
import interfaces.IDAO;
import models.POI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase para la gestión de POIs con capacidades de sincronización entre bases
 * de datos MySQL y MongoDB.
 */
public class PoiManager {
	private static IDAO mysqlDAO;
	private static IDAO mongoDAO;
	
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoConnection.class);


	

	// Bloque estático para inicializar los DAOs
	static {
	    try {
	        // Inicializar los DAOs con las conexiones correspondientes
	        mysqlDAO = new PoiMySQLDAO();
	        mongoDAO = new PoiMongoDAO(MongoConnection.getInstance().getDatabase());
	    } catch (Exception e) {
	        // Usar un logger en lugar de System.err para un registro más robusto
	        LOGGER.error("Error al inicializar los DAOs", e);
	        
	        // Lanzar una excepción de inicialización para detener la aplicación
	        throw new RuntimeException("No se pudieron inicializar los DAOs", e);
	    }
	}
	/**
	 * Constructor que inicializa los DAOs de MySQL y MongoDB.
	 * 
	 * @param mysqlDAO El DAO para la base de datos MySQL.
	 * @param mongoDAO El DAO para la base de datos MongoDB.
	 */
	public PoiManager(IDAO mysqlDAO, IDAO mongoDAO) {
		PoiManager.mysqlDAO = mysqlDAO;
		PoiManager.mongoDAO = mongoDAO;
	}

	/**
	 * Obtiene la base de datos de origen según el valor de `toMongo`.
	 * 
	 * @param toMongo Si es true, la fuente es MySQL. Si es false, la fuente es
	 *                MongoDB.
	 * @return El DAO correspondiente a la base de datos de origen.
	 */
	public static IDAO getSourceDB(boolean toMongo) {
		return toMongo ? mysqlDAO : mongoDAO;
	}

	/**
	 * Obtiene la base de datos de destino según el valor de `toMongo`.
	 * 
	 * @param toMongo Si es true, el destino es MongoDB. Si es false, el destino es
	 *                MySQL.
	 * @return El DAO correspondiente a la base de datos de destino.
	 */
	private static IDAO getTargetDB(boolean toMongo) {
		return toMongo ? mongoDAO : mysqlDAO;
	}

	/**
	 * Sincroniza los datos entre las bases de datos de origen y destino dependiendo
	 * del valor de toMongo.
	 * 
	 * @param toMongo Si es true, sincroniza desde MySQL a MongoDB. Si es false,
	 *                sincroniza desde MongoDB a MySQL.
	 * @return true si la sincronización fue exitosa, false en caso contrario.
	 */
	public static boolean synchronizeDatabases(boolean toMongo) {
		try {
			IDAO sourceDAO = getSourceDB(toMongo);
			IDAO targetDAO = getTargetDB(toMongo);

			// Obtiene todos los POIs desde la base de datos fuente
			List<POI> sourcePois = sourceDAO.listAllPois();

			if (sourcePois == null) {
				return false;
			} else {
				// Itera sobre cada POI para sincronizarlo con la base de datos destino
				for (POI poi : sourcePois) {
					// Verifica si el POI ya existe en la base de datos destino
					POI existingPoi = targetDAO.listByID(poi.getId());

					if (existingPoi == null) {
						// Si no existe, agrega el POI al destino
						targetDAO.addPoi(poi);
					} else {
						// Si existe, actualiza el POI con los datos más recientes
						targetDAO.updateById(poi.getId(), poi);
					}
				}

			}

			return true;
		} catch (Exception e) {
			// Maneja cualquier excepción y devuelve false en caso de error
			System.err.println("Error during synchronization: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Añade un nuevo POI al DAO de origen.
	 * 
	 * @param poi El POI a añadir.
	 */
	public static void addPOI(POI poi) {
		try {
			// Verificar si el ID del POI ya está en uso
			if (isIDAvailable(poi.getId())) {
				getSourceDB(true).addPoi(poi);// Siempre añadimos al DAO de la base de datos de origen
			} else {
				System.out.println("Error: Ya existe un POI con este ID.");
			}
		} catch (Exception e) {
			System.err.println("Error adding POI: " + e.getMessage());
		}
	}

	/**
	 * Verifica si un ID de POI está disponible en la base de datos de origen.
	 * 
	 * @param id El ID del POI a verificar.
	 * @return true si el ID está disponible, false si ya está en uso.
	 */
	public static boolean isIDAvailable(int id) {
		try {
			// Verifica si el POI con el ID especificado ya existe en la base de datos de
			// origen
			POI existingPoi = mysqlDAO.listByID(id);
			return existingPoi == null; // Devuelve true si no existe, false si ya está en uso
		} catch (Exception e) {
			System.err.println("Error checking ID availability: " + e.getMessage());
			return false;
		}
	}
}