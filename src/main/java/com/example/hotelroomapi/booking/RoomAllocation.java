package com.example.hotelroomapi.booking;

import java.math.BigDecimal;

public record RoomAllocation(long premiumRooms,
                             BigDecimal premiumIncome,
                             long economyRooms,
                             BigDecimal economyIncome) {
}
