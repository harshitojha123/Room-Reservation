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

    // Thread-safe queue holding booking requests
    private static final BlockingQueue<BookingRequest> bookingQueue = new LinkedBlockingQueue<>();

    // Store worker threads to manage lifecycle (start/stop)
    private static final List<Thread> workerThreads = new ArrayList<>();

    public static void main(String[] args) {
        // Developer and environment info
        System.err.println("Developer: Harshit Ojha");
        System.out.println("Preferred office: Cambridge");

        // Start worker threads to process booking requests
        startWorkerThreads(10);

        // Load booking requests from JSON and add them to queue
        enqueueBookingRequests("src/main/resources/booking_requests.json");

        // After all requests have been enqueued, stop workers gracefully
        stopWorkerThreads();

        System.out.println("Room booking system shutdown complete.");
    }

    /**
     * Starts the specified number of worker threads.
     * Each worker takes booking requests from the queue and processes them.
     */
    private static void startWorkerThreads(int numberOfWorkers) {
        for (int i = 0; i < numberOfWorkers; i++) {
            Thread worker = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        BookingRequest request = bookingQueue.take();

                        System.out.println("[Thread " + Thread.currentThread().getName() + "] Processing request: Room "
                                + request.getRoomNumber() + ", Guest: " + request.getGuest());

                        processBooking(request.getRoomNumber(), request.getGuest());

                        System.out.println("[Thread " + Thread.currentThread().getName() + "] Finished request: Room "
                                + request.getRoomNumber() + ", Guest: " + request.getGuest());

                    } catch (InterruptedException e) {
                        // Preserve interrupt status and exit thread loop for graceful shutdown
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("[Thread " + Thread.currentThread().getName() + "] Stopped.");
            }, "Worker-" + i);
            worker.start();
            workerThreads.add(worker);
        }
    }

    /**
     * Reads booking requests from a JSON file and enqueues them for processing.
     * Simulates delay between incoming requests.
     */
    private static void enqueueBookingRequests(String filepath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<BookingRequest> requests = mapper.readValue(new File(filepath),
                    new TypeReference<List<BookingRequest>>() {
                    });

            for (BookingRequest request : requests) {
                bookingQueue.put(request); // Blocking put in case queue is full (unbounded here)
                System.out.println("Enqueued booking request: Room " + request.getRoomNumber() + ", Guest " + request.getGuest());

                Thread.sleep(3000); // simulate delay between requests
            }

        } catch (IOException e) {
            System.err.println("Failed to read booking requests: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Booking request enqueue interrupted.");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Processes booking for a single room and guest.
     * Synchronizes on the Room object to prevent race conditions.
     */
    private static void processBooking(int roomNumber, String guest) {
        Room room = RoomDatabaseAccessService.getInstance().loadRoom(roomNumber);

        if (room == null) {
            System.out.println("Room " + roomNumber + " not found for guest " + guest);
            return;
        }

        synchronized (room) {
            if (room.isAvailable()) {
                room.bookRoom();
                System.out.println("Room " + roomNumber + " successfully booked by " + guest);
            } else {
                System.out.println("Room " + roomNumber + " is already booked; unable to book for " + guest);
            }
        }
    }

    /**
     * Stops all worker threads gracefully by interrupting and joining them.
     */
    private static void stopWorkerThreads() {
        System.out.println("Initiating shutdown of worker threads...");
        for (Thread worker : workerThreads) {
            worker.interrupt();
        }
        for (Thread worker : workerThreads) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("All worker threads stopped.");
    }
}
