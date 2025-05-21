package com.kayak.hotelsearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayak.hotelsearch.booking.BookingRequest;
import com.kayak.hotelsearch.room.Room;
import com.kayak.hotelsearch.room.RoomDatabaseAccessService;

public class Main {

    // Thread-safe blocking queue for booking requests
    private static final BlockingQueue<BookingRequest> incomingRequests = new LinkedBlockingQueue<>();

    // List to keep references to worker threads so we can stop them later
    private static final List<Thread> workers = new ArrayList<>();

    public static void main(String[] args) {

        // Section 1 - Warming up: developer name to stderr, office preference to stdout
        System.err.println("Developer: Harshit Ojha");
        System.out.println("Preferred office: Cambridge");

        // Start worker threads
        for (int i = 0; i < 10; i++) {
            Thread worker = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        BookingRequest request = incomingRequests.take();

                        System.out.println("Thread " + Thread.currentThread().getName() +
                                " processing request for room " + request.getRoomNumber() +
                                " by " + request.getGuest());

                        bookRoom(request.getRoomNumber(), request.getGuest());

                        System.out.println("Thread " + Thread.currentThread().getName() +
                                " finished processing request for room " + request.getRoomNumber() +
                                " by " + request.getGuest());

                    } catch (InterruptedException e) {
                        // Restore interrupt status and exit loop to stop thread
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("Thread " + Thread.currentThread().getName() + " stopping.");
            }, "Worker-" + i);
            worker.start();
            workers.add(worker);
        }

        // Read and enqueue booking requests
        readBookingRequests("src/main/resources/booking_requests.json");

        // After all requests are queued and processed, stop workers gracefully
        stopAllWorkers();
    }

    // Gracefully stops all worker threads by interrupting them
    private static void stopAllWorkers() {
        System.out.println("Stopping all worker threads...");
        for (Thread worker : workers) {
            worker.interrupt();
        }
        // Optionally wait for threads to finish
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("All workers stopped.");
    }

    // Section 4 - Fix concurrency issue by synchronizing booking per Room instance
    public static void bookRoom(int roomNumber, String guest) {
        Room room = RoomDatabaseAccessService.getInstance().loadRoom(roomNumber);
        if (room != null) {
            // Synchronize on room object to avoid race conditions
            synchronized (room) {
                if (room.isAvailable()) {
                    room.bookRoom();
                    System.out.println("    Room " + roomNumber + " booked by " + guest);
                } else {
                    System.out.println("    Room " + roomNumber + " is not available for " + guest);
                }
            }
        } else {
            System.out.println("    Room " + roomNumber + " does not exist for " + guest);
        }
    }

    public static void readBookingRequests(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<BookingRequest> requests = mapper.readValue(new File(filename),
                    new TypeReference<List<BookingRequest>>() {
                    });

            for (BookingRequest request : requests) {
                incomingRequests.put(request); // blocks if queue full, but unbounded here
                Thread.sleep(2000); // simulate delay
            }

        } catch (IOException e) {
            System.out.println("Error reading booking requests: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
