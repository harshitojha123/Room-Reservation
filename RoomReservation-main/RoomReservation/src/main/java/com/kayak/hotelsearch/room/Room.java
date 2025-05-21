package com.kayak.hotelsearch.room;

public class Room {
    private final int roomNumber;
    private final RoomType roomType;
    private final double price;

    // Use volatile for visibility across threads for this mutable field
    private volatile boolean isAvailable;

    public Room(int roomNumber, RoomType roomType, double price, boolean isAvailable) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.price = price;
        this.isAvailable = isAvailable;
    }

    // Booking should be synchronized externally, but keep method atomic here
    public boolean bookRoom() {
        if (isAvailable) {
            isAvailable = false;
            return true;
        }
        return false;
    }

    public boolean unbookRoom() {
        if (!isAvailable) {
            isAvailable = true;
            return true;
        }
        return false;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    // ✅ Add these two methods
    public RoomType getRoomType() {
        return roomType;
    }

    public double getPrice() {
        return price;
    }
}
