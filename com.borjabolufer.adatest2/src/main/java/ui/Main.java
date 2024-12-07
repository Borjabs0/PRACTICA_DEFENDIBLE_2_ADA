package ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import DAO.MongoConnection;
import DAO.PoiMongoDAO;
import DAO.PoiMySQLDAO;
import lib.ConsoleMenu;
import lib.LibIO;
import managers.PoiManager;
import models.POI;

public class Main {
	private static boolean currentSourceIsMongo = true; // true = MongoDB, false = MySQL

	public static void main(String[] args) {
		System.out.println("Iniciando el programa...");
		try {
			int option;
			do {
				showDatabaseStatus();
				option = menuPrincipal();
				processMenuOption(option);
			} while (option != 0);
		} catch (Exception e) {
			System.err.println("An error occurred: " + e.getMessage());
			e.printStackTrace();
		} finally {
			System.out.println("Cerrando conexiones...");
			MongoConnection.closeInstance();
		}
	}

	private static void showDatabaseStatus() {
		System.out.println("\n=== DATABASE STATUS ===");
		System.out.println("Current data source: " + (currentSourceIsMongo ? "MongoDB" : "MySQL"));
		System.out.println("MongoDB items: " +
		PoiManager.getSourceDB(true).listAllPois().size());
		System.out.println("MySQL items: " +
		PoiManager.getSourceDB(false).listAllPois().size());
		System.out.println("=====================\n");
	}

	private static int menuPrincipal() {
		ConsoleMenu menu = new ConsoleMenu("POI Management System");
		menu.addOpcion("Switch Database Source");
		menu.addOpcion("Add POIs");
		menu.addOpcion("List POIs");
		menu.addOpcion("Update POI");
		menu.addOpcion("Remove POIs");
		menu.addOpcion("Sync Databases");
		return menu.mostrarMenu();
	}

	private static void processMenuOption(int option) {
		switch (option) {
		case 1:
			switchDataSource();
			break;
		case 2:
			processAddPOI();
			break;
		case 3:
			processListPOIs();
			break;
		case 4:
			processUpdatePoi();
			break;
		case 5:
			processRemovePois();
			break;
		case 6:
			processSyncDatabases();
			break;
		case 0:
			System.out.println("Exiting...");
			break;
		default:
			System.out.println("Invalid option");
		}
	}

	private static void switchDataSource() {
		currentSourceIsMongo = !currentSourceIsMongo;
		System.out.println("Switched to " + (currentSourceIsMongo ? "MongoDB" : "MySQL"));
	}

	private static void processAddPOI() {
		boolean continueAdding = true;

		while (continueAdding) {
			try {
				int id = LibIO.solicitarInt("Enter POI ID (0 to finish): ", 0, Integer.MAX_VALUE);
				if (id == 0)
					break;

				if (PoiManager.isIDAvailable(id)) {
					Double latitude = LibIO.solicitarDouble("Enter latitude: ", -90.0, 90.0);
					Double longitude = LibIO.solicitarDouble("Enter longitude: ", -180.0, 180.0);
					String city = LibIO.solicitarString("Enter city: ", 2, 100);
					String country = LibIO.solicitarString("Enter country: ", 2, 100);
					String description = LibIO.solicitarString("Enter description: ", 2, 500);
					Date updatedDate = LibIO.solicitarFechaDate("Enter update date (yyyy-MM-dd): ",
							new SimpleDateFormat("yyyy-MM-dd"));

					POI poi = new POI(id, latitude, longitude, country, city, description, updatedDate);
					PoiManager.addPOI(poi);
					System.out.println("POI added successfully.");
				} else {
					System.out.println("Error: POI with this ID already exists.");
				}
			} catch (Exception e) {
				System.err.println("Error adding POI: " + e.getMessage());
			}
		}
	}

	private static void processListPOIs() {
		ConsoleMenu menu = new ConsoleMenu("List POIs");
		menu.addOpcion("List all POIs");
		menu.addOpcion("Filter by ID");
		menu.addOpcion("Filter by ID range");
		menu.addOpcion("Filter by month");
		menu.addOpcion("Filter by city");

		int option = menu.mostrarMenu();
		switch (option) {
		case 1:
			listAllPois();
			break;
		case 2:
			listPoiById();
			break;
		case 3:
			listPoiByIdRange();
			break;
		case 4:
			listPoiByMonth();
			break;
		case 5:
			listPoiByCity();
			break;
		}
	}

	private static void listAllPois() {
		List<POI> pois = PoiManager.getSourceDB(currentSourceIsMongo).listAllPois();
		if (pois.isEmpty()) {
			System.out.println("No POIs found.");
		} else {
			pois.forEach(System.out::println);
		}
	}

	private static void listPoiById() {
		int id = LibIO.solicitarInt("Enter POI ID: ", 1, Integer.MAX_VALUE);
		POI poi = PoiManager.getSourceDB(currentSourceIsMongo).listByID(id);
		if (poi != null) {
			System.out.println(poi);
		} else {
			System.out.println("POI not found.");
		}
	}

	private static void listPoiByIdRange() {
		int min = LibIO.solicitarInt("Enter minimum ID: ", 1, Integer.MAX_VALUE);
		int max = LibIO.solicitarInt("Enter maximum ID: ", min, Integer.MAX_VALUE);
		List<POI> pois = PoiManager.getSourceDB(currentSourceIsMongo).listByIDRange(min, max);
		if (pois.isEmpty()) {
			System.out.println("No POIs found in this range.");
		} else {
			pois.forEach(System.out::println);
		}
	}

	private static void listPoiByMonth() {
		Date date = LibIO.solicitarFechaDate("Enter date (yyyy-MM-dd): ", new SimpleDateFormat("yyyy-MM-dd"));
		List<POI> pois = PoiManager.getSourceDB(currentSourceIsMongo).listByMonth(date);
		if (pois.isEmpty()) {
			System.out.println("No POIs found for this month.");
		} else {
			pois.forEach(System.out::println);
		}
	}

	private static void listPoiByCity() {
		String city = LibIO.solicitarString("Enter city: ", 2, 100);
		List<POI> pois = PoiManager.getSourceDB(currentSourceIsMongo).listByCity(city);
		if (pois.isEmpty()) {
			System.out.println("No POIs found in this city.");
		} else {
			pois.forEach(System.out::println);
		}
	}

	private static void processUpdatePoi() {
		int id = LibIO.solicitarInt("Enter POI ID to update: ", 1, Integer.MAX_VALUE);
		POI existingPoi = PoiManager.getSourceDB(currentSourceIsMongo).listByID(id);

		if (existingPoi != null) {
			System.out.println("Current POI: " + existingPoi);
			if (confirmAction("Do you want to update this POI? (y/n): ")) {
				try {
					Double latitude = LibIO.solicitarDouble("Enter new latitude: ", -90.0, 90.0);
					Double longitude = LibIO.solicitarDouble("Enter new longitude: ", -180.0, 180.0);
					String city = LibIO.solicitarString("Enter new city: ", 2, 100);
					String country = LibIO.solicitarString("Enter new country: ", 2, 100);
					String description = LibIO.solicitarString("Enter new description: ", 2, 500);
					Date updatedDate = LibIO.solicitarFechaDate("Enter new update date (yyyy-MM-dd): ",
							new SimpleDateFormat("yyyy-MM-dd"));

					POI newPoi = new POI(id, latitude, longitude, country, city, description, updatedDate);
					PoiManager.getSourceDB(currentSourceIsMongo).updateById(id, newPoi);
					System.out.println("POI updated successfully.");
				} catch (Exception e) {
					System.err.println("Error updating POI: " + e.getMessage());
				}
			}
		} else {
			System.out.println("POI not found.");
		}
	}

	private static void processRemovePois() {
		ConsoleMenu menu = new ConsoleMenu("Remove POIs");
		menu.addOpcion("Remove by ID");
		menu.addOpcion("Remove by ID range");
		menu.addOpcion("Remove by month");
		menu.addOpcion("Remove by city");
		menu.addOpcion("Remove all");

		int option = menu.mostrarMenu();
		switch (option) {
		case 1:
			removePoiById();
			break;
		case 2:
			removePoiByIdRange();
			break;
		case 3:
			removePoiByMonth();
			break;
		case 4:
			removePoiByCity();
			break;
		case 5:
			removeAllPois();
			break;
		}
	}

	private static void removePoiById() {
		int id = LibIO.solicitarInt("Enter POI ID to remove: ", 1, Integer.MAX_VALUE);
		if (confirmAction("Are you sure you want to remove this POI? (y/n): ")) {
			POI removedPoi = PoiManager.getSourceDB(currentSourceIsMongo).removeByID(id);
			if (removedPoi != null) {
				System.out.println("POI removed successfully.");
			} else {
				System.out.println("POI not found.");
			}
		}
	}

	private static void removePoiByIdRange() {
		int min = LibIO.solicitarInt("Enter minimum ID: ", 1, Integer.MAX_VALUE);
		int max = LibIO.solicitarInt("Enter maximum ID: ", min, Integer.MAX_VALUE);
		List<POI> poisToRemove = PoiManager.getSourceDB(currentSourceIsMongo).listByIDRange(min, max);

		if (!poisToRemove.isEmpty()) {
			System.out.println("POIs to be removed:");
			poisToRemove.forEach(System.out::println);

			if (confirmAction("Are you sure you want to remove these POIs? (y/n): ")) {
				PoiManager.getSourceDB(currentSourceIsMongo).removeByIDRange(min, max);
				System.out.println("POIs removed successfully.");
			}
		} else {
			System.out.println("No POIs found in this range.");
		}
	}

	private static void removePoiByMonth() {
		Date date = LibIO.solicitarFechaDate("Enter date (yyyy-MM-dd): ", new SimpleDateFormat("yyyy-MM-dd"));
		List<POI> poisToRemove = PoiManager.getSourceDB(currentSourceIsMongo).listByMonth(date);

		if (!poisToRemove.isEmpty()) {
			System.out.println("POIs to be removed:");
			poisToRemove.forEach(System.out::println);

			if (confirmAction("Are you sure you want to remove these POIs? (y/n): ")) {
				PoiManager.getSourceDB(currentSourceIsMongo).removeByMonth(date);
				System.out.println("POIs removed successfully.");
			}
		} else {
			System.out.println("No POIs found for this month.");
		}
	}

	private static void removePoiByCity() {
		String city = LibIO.solicitarString("Enter city: ", 2, 100);
		List<POI> poisToRemove = PoiManager.getSourceDB(currentSourceIsMongo).listByCity(city);

		if (!poisToRemove.isEmpty()) {
			System.out.println("POIs to be removed:");
			poisToRemove.forEach(System.out::println);

			if (confirmAction("Are you sure you want to remove these POIs? (y/n): ")) {
				PoiManager.getSourceDB(currentSourceIsMongo).removeByCity(city);
				System.out.println("POIs removed successfully.");
			}
		} else {
			System.out.println("No POIs found in this city.");
		}
	}

	private static void removeAllPois() {
		if (confirmAction("Are you sure you want to remove ALL POIs? (y/n): ")) {
			int count = PoiManager.getSourceDB(currentSourceIsMongo).removeAllItemFilters();
			System.out.println(count + " POIs removed successfully.");
		}
	}

	private static void processSyncDatabases() {
		System.out.println("Synchronizing databases...");
		if (confirmAction("Do you want to sync from " + (currentSourceIsMongo ? "MongoDB to MySQL" : "MySQL to MongoDB")
				+ "? (y/n): ")) {
			boolean success = PoiManager.synchronizeDatabases(currentSourceIsMongo);
			if (success) {
				System.out.println("Databases synchronized successfully.");
			} else {
				System.out.println("Error synchronizing databases.");
			}
		}
	}

	private static boolean confirmAction(String message) {
		String response = LibIO.solicitarString(message, 1, 1).toLowerCase();
		return response.equals("y");
	}
}