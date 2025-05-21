package com.kayak.hotelsearch.room;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton service to access room data.
 * Thread-safe with synchronized map and methods.
 */
public class RoomDatabaseAccessService {
    private static RoomDatabaseAccessService instance;

    // Thread-safe map for caching rooms
    private static Map<Integer, Room> roomCache = Collections.synchronizedMap(new HashMap<>());

    // Private constructor to prevent external instantiation
    private RoomDatabaseAccessService() {
    }

    /**
     * Get the singleton instance of RoomDatabaseAccessService.
     * 
     * @return singleton instance
     */
    public static synchronized RoomDatabaseAccessService getInstance() {
        if (instance == null) {
            instance = new RoomDatabaseAccessService();
            System.out.println("RoomDatabaseAccessService instance = " + instance);
            instance.initializeRooms();
        }
        return instance;
    }

    /**
     * Initialize room data.
     */
    private void initializeRooms() {
        Room room1 = new Room(101, RoomType.SINGLE, 100.0, true);
        Room room2 = new Room(102, RoomType.DOUBLE, 200.0, true);
        Room room3 = new Room(103, RoomType.SINGLE, 100.0, false);
        Room room4 = new Room(104, RoomType.DOUBLE, 200.0, true);
        Room room5 = new Room(105, RoomType.DELUXE, 400.0, false);
        Room room6 = new Room(106, RoomType.STANDARD, 200.0, true);
        Room room7 = new Room(107, RoomType.SUITE, 600.0, false);

        roomCache.put(room1.getRoomNumber(), room1);
        roomCache.put(room2.getRoomNumber(), room2);
        roomCache.put(room3.getRoomNumber(), room3);
        roomCache.put(room4.getRoomNumber(), room4);
        roomCache.put(room5.getRoomNumber(), room5);
        roomCache.put(room6.getRoomNumber(), room6);
        roomCache.put(room7.getRoomNumber(), room7);
    }

    /**
     * Loads a room by its room number.
     * 
     * @param roomNumber room number
     * @return Room object or null if not found
     */
    public Room loadRoom(int roomNumber) {
        System.out.println("Loading room " + roomNumber + " from database");
        return roomCache.get(roomNumber);
    }
}
