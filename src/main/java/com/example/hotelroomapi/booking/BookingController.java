package com.example.hotelroomapi.booking;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class RoomOccupancyManagerController {

    private final RoomAllocator roomAllocator;

    public RoomOccupancyManagerController(RoomAllocator roomAllocator) {
        this.roomAllocator = roomAllocator;
    }

    @PostMapping("/booking")
    public RoomAllocation foo(@RequestParam("premium") long premium,
                              @RequestParam("economy") long economy,
                              @RequestBody List<BigDecimal> bids) {
        return roomAllocator.allocate(premium, economy, bids);
    }
}