package com.kayak.hotelsearch.booking;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// Immutable class (good for thread safety)
public final class BookingRequest {
    private final int roomNumber;
    private final String guest;

    @JsonCreator
    public BookingRequest(
            @JsonProperty("roomNumber") int roomNumber,
            @JsonProperty("guest") String guest) {
        this.roomNumber = roomNumber;
        this.guest = guest;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getGuest() {
        return guest;
    }
}
