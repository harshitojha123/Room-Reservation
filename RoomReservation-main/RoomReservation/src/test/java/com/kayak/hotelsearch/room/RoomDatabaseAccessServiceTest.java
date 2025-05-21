package com.kayak.hotelsearch.room;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoomDatabaseAccessServiceTest {

    private RoomDatabaseAccessService service;

    @BeforeEach
    public void setUp() {
        service = RoomDatabaseAccessService.getInstance();
    }

    @Test
    public void testSingletonInstance() {
        RoomDatabaseAccessService anotherInstance = RoomDatabaseAccessService.getInstance();
        assertSame(service, anotherInstance, "Expected singleton instance");
    }

    @Test
    public void testLoadRoomExists() {
        Room room = service.loadRoom(101);
        assertNotNull(room, "Room 101 should exist");
        assertEquals(101, room.getRoomNumber());
    }

    @Test
    public void testLoadRoomNotExists() {
        Room room = service.loadRoom(999);
        assertNull(room, "Room 999 should not exist");
    }

    @Test
    public void testRoomProperties() {
        Room room = service.loadRoom(104);
        assertNotNull(room);
        assertEquals(104, room.getRoomNumber());
        assertEquals(RoomType.DOUBLE, room.getRoomType());
        assertTrue(room.isAvailable());
        assertTrue(room.getPrice() > 0);
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        // Test multiple threads accessing loadRoom concurrently
        Runnable task = () -> {
            Room room = service.loadRoom(101);
            assertNotNull(room);
            assertEquals(101, room.getRoomNumber());
        };

        Thread[] threads = new Thread[10];
        for(int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(task);
            threads[i].start();
        }

        for(Thread t : threads) {
            t.join();
        }
    }
}
