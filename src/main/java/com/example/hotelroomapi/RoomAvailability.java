package com.example.hotelroomapi;

import java.math.BigDecimal;

public record RoomAvailability(long premiumRooms,
                               BigDecimal premiumIncome,
                               long economyRooms,
                               BigDecimal economyIncome) {
}
