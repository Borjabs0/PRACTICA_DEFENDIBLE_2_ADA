package interfaces;

import java.util.Date;
import java.util.List;

import models.POI;

public interface IDAO{
	
	public void addPoi(POI poi);
	public void addPois(List<POI> pois);
	public POI removePoi(int id);
	
	// Opciones para listar 
	public List<POI> listAllPois();
	public POI listByID(int id);
	public List<POI> listByIDRange(int min, int max);
	public List<POI> listByMonth(Date date);
	public List<POI> listByCity(String city);
	
	//Update item
	public POI updateById(int id, POI poi);

	//Delete item 
	public int removeAllItemFilters();
	public POI removeByID(int id);
	public List<POI> removeByIDRange(int min, int max);
	public List<POI> removeByMonth(Date date);
	public List<POI> removeByCity(String city);
}
