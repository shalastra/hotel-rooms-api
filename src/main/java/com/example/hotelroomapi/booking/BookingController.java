package com.example.hotelroomapi.booking;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class BookingController {

    private final RoomAllocator roomAllocator;

    public BookingController(RoomAllocator roomAllocator) {
        this.roomAllocator = roomAllocator;
    }

    @PostMapping("/booking")
    public RoomAllocation foo(@RequestParam("premium") long premiumRequested,
                              @RequestParam("economy") long economyRequested,
                              @RequestBody List<BigDecimal> bids) {
        return roomAllocator.allocate(premiumRequested, economyRequested, bids);
    }
}