package DAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import interfaces.IDAO;
import models.POI;

public class PoiMongoDAO implements IDAO {

    private class PoiDocumentDefinitions {
        private static final String MONGODB__POI__PROPERTYNAME__ID = "id";
        private static final String MONGODB__POI__PROPERTYNAME__LATITUDE = "latitude";
        private static final String MONGODB__POI__PROPERTYNAME__LONGITUDE = "longitude";
        private static final String MONGODB__POI__PROPERTYNAME__COUNTRY = "country";
        private static final String MONGODB__POI__PROPERTYNAME__CITY = "city";
        private static final String MONGODB__POI__PROPERTYNAME__DESCRIPTION = "description";
        private static final String MONGODB__POI__PROPERTYNAME__UPDATED_DATE = "updatedDate";

        private static final String COLLECTION_NAME = "POIs";
    }

    private final MongoCollection<Document> collection;

    /**
     * Constructor de la clase PoiMongoDAO.
     * Inicializa la colección "pois" en la base de datos MongoDB si no existe.
     * 
     * @param database La instancia de la base de datos MongoDB.
     */
    public PoiMongoDAO(MongoDatabase database) {
        if (!collectionExists(PoiDocumentDefinitions.COLLECTION_NAME, database)) {
            database.createCollection(PoiDocumentDefinitions.COLLECTION_NAME);
        }
        this.collection = database.getCollection(PoiDocumentDefinitions.COLLECTION_NAME);
    }

    /**
     * Comprueba si una colección existe en la base de datos MongoDB.
     * 
     * @param collectionName El nombre de la colección a verificar.
     * @param database       La instancia de la base de datos MongoDB.
     * @return true si la colección existe, false en caso contrario.
     */
    private boolean collectionExists(String collectionName, MongoDatabase database) {
        for (String name : database.listCollectionNames()) {
            if (name.equals(collectionName)) {
                return true;
            }
        }
        return false;
    }

    // **************************** MÉTODOS DE LA INTERFAZ ****************************

    /**
     * Agrega un nuevo POI a la base de datos.
     * 
     * @param poi El POI que se desea agregar.
     */
    @Override
    public void addPoi(POI poi) {
        validatePoi(poi);
        Document document = poiToDocument(poi);
        collection.insertOne(document);
    }
    
    /**
     * Agregar varios POI a la base de datos
     * 
     * @param pois los Pois que se desean agregar 
     */
    @Override
    public void addPois(List<POI> pois) {
    	// Validar todos los poi
    	pois.forEach(poi -> validatePoi(poi));
    	
    	List<Document> documents = new ArrayList<Document>();
    	for(POI poi : pois) {
    		Document document = poiToDocument(poi);
    		documents.add(document);
    	}
    	collection.insertMany(documents);
    }

    /**
     * Elimina un POI de la base de datos usando su ID.
     * 
     * @param id El ID del POI a eliminar.
     * @return El POI eliminado o null si no se encontró.
     */
    @Override
    public POI removePoi(int id) {
        Bson filter = Filters.eq(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__ID, id);
        Document document = collection.findOneAndDelete(filter);
        return document != null ? documentToPoi(document) : null;
    }

    /**
     * Obtiene todos los POIs de la base de datos.
     * 
     * @return Una lista de todos los POIs.
     */
    @Override
    public List<POI> listAllPois() {
        List<POI> pois = new ArrayList<>();
        for (Document document : collection.find()) {
            pois.add(documentToPoi(document));
        }
        return pois;
    }

    /**
     * Busca un POI en la base de datos por su ID.
     * 
     * @param id El ID del POI a buscar.
     * @return El POI encontrado o null si no existe.
     */
    @Override
    public POI listByID(int id) {
        Bson filter = Filters.eq(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__ID, id);
        Document document = collection.find(filter).first();
        return document != null ? documentToPoi(document) : null;
    }

    /**
     * Busca POIs en un rango de IDs.
     * 
     * @param min El ID mínimo del rango.
     * @param max El ID máximo del rango.
     * @return Una lista de POIs en el rango especificado.
     */
    @Override
    public List<POI> listByIDRange(int min, int max) {
        List<POI> pois = new ArrayList<>();
        Bson filter = Filters.and(
                Filters.gte(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__ID, min),
                Filters.lte(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__ID, max)
        );
        for (Document document : collection.find(filter)) {
            pois.add(documentToPoi(document));
        }
        return pois;
    }

    /**
     * Busca POIs por mes basado en la fecha de actualización.
     * 
     * @param date La fecha del mes a filtrar.
     * @return Una lista de POIs actualizados en el mes especificado.
     */
    @Override
    public List<POI> listByMonth(Date date) {
        List<POI> pois = new ArrayList<>();
        Bson filter = Filters.gte(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__UPDATED_DATE, date);
        for (Document document : collection.find(filter)) {
            pois.add(documentToPoi(document));
        }
        return pois;
    }

    /**
     * Busca POIs por ciudad.
     * 
     * @param city El nombre de la ciudad a filtrar.
     * @return Una lista de POIs ubicados en la ciudad especificada.
     */
    @Override
    public List<POI> listByCity(String city) {
        List<POI> pois = new ArrayList<>();
        Bson filter = Filters.eq(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__CITY, city);
        for (Document document : collection.find(filter)) {
            pois.add(documentToPoi(document));
        }
        return pois;
    }

    /**
     * Actualiza un POI existente en la base de datos usando su ID.
     * 
     * @param id El ID del POI a actualizar.
     * @return El POI actualizado.
     */
    @Override
    public POI updateById(int id, POI poi) {
        POI existingPoi = listByID(id);
        if (existingPoi == null) {
            throw new IllegalArgumentException("POI with ID " + id + " does not exist.");
        }
        Document updatedDocument = poiToDocument(existingPoi);
        Bson filter = Filters.eq(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__ID, id);
        collection.replaceOne(filter, updatedDocument);
        return existingPoi;
    }

    /**
     * Elimina todos los POIs de la base de datos.
     * 
     * @return El número de POIs eliminados.
     */
    @Override
    public int removeAllItemFilters() {
        long deletedCount = collection.deleteMany(new Document()).getDeletedCount();
        return (int) deletedCount;
    }

    /**
     * Elimina un POI de la base de datos por su ID.
     * 
     * @param id El ID del POI a eliminar.
     * @return El POI eliminado o null si no se encontró.
     */
    @Override
    public POI removeByID(int id) {
        return removePoi(id);
    }

    /**
     * Elimina POIs en un rango de IDs.
     * 
     * @param min El ID mínimo del rango.
     * @param max El ID máximo del rango.
     * @return Una lista de POIs eliminados.
     */
    @Override
    public List<POI> removeByIDRange(int min, int max) {
        List<POI> pois = listByIDRange(min, max);
        pois.forEach(poi -> removePoi(poi.getId()));
        return pois;
    }

    /**
     * Elimina POIs por fecha de actualización.
     * 
     * @param date La fecha para filtrar los POIs a eliminar.
     * @return Una lista de POIs eliminados.
     */
    @Override
    public List<POI> removeByMonth(Date date) {
        List<POI> pois = listByMonth(date);
        pois.forEach(poi -> removePoi(poi.getId()));
        return pois;
    }

    /**
     * Elimina POIs por ciudad.
     * 
     * @param city El nombre de la ciudad de los POIs a eliminar.
     * @return Una lista de POIs eliminados.
     */
    @Override
    public List<POI> removeByCity(String city) {
        List<POI> pois = listByCity(city);
        pois.forEach(poi -> removePoi(poi.getId()));
        return pois;
    }
    

  
   

    
    // **************************** MÉTODOS AUXILIARES ****************************

    private POI documentToPoi(Document document) {
        return new POI(
                document.getInteger(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__ID),
                document.getDouble(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__LATITUDE),
                document.getDouble(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__LONGITUDE),
                document.getString(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__COUNTRY),
                document.getString(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__CITY),
                document.getString(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__DESCRIPTION),
                document.getDate(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__UPDATED_DATE)
        );
    }

    private Document poiToDocument(POI poi) {
        return new Document(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__ID, poi.getId())
                .append(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__LATITUDE, poi.getLatitude())
                .append(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__LONGITUDE, poi.getLongitude())
                .append(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__COUNTRY, poi.getCountry())
                .append(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__CITY, poi.getCity())
                .append(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__DESCRIPTION, poi.getDescription())
                .append(PoiDocumentDefinitions.MONGODB__POI__PROPERTYNAME__UPDATED_DATE, poi.getUpdatedDate());
    }

    private void validatePoi(POI poi) {
        if (poi == null) {
            throw new IllegalArgumentException("POI cannot be null.");
        }
        if (poi.getId() == null) {
            throw new IllegalArgumentException("POI ID is required.");
        }
        if (poi.getCity() == null || poi.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("POI city is required.");
        }
        if (poi.getDescription() == null || poi.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("POI description is required.");
        }
    }

	
}
