package com.example.hotelroomapi.booking;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Validated
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
        if (bids.isEmpty()) {
            /*
              Normally, I would use ControllerAdvice to handle exceptions,
              the ResponseStatusException is used solely for the purpose of the task
             */
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Bids list cannot be empty!");
        }

        return roomAllocator.allocate(premiumRequested, economyRequested, bids);
    }
}