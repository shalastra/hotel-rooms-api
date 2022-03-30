package com.example.hotelroomapi;

import com.example.hotelroomapi.response.RoomAllocation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class RoomOccupancyManagerController {

    private static final BigDecimal LIMIT = new BigDecimal("100");

    private static boolean test(BigDecimal price) {
        return price.compareTo(LIMIT) >= 0;
    }

    @PostMapping("/booking")
    public RoomAllocation foo(@RequestParam("premium") long premium,
                              @RequestParam("economy") long economy,
                              @RequestBody List<BigDecimal> bids) {
        List<BigDecimal> sorted = bids
                .stream()
                .sorted(Comparator.reverseOrder()).toList();

        Map<Boolean, List<BigDecimal>> collect = sorted.stream()
                .collect(Collectors
                        .partitioningBy(RoomOccupancyManagerController::test));

        long premiumBids = collect.get(Boolean.TRUE).size();

        BigDecimal premiumIncome, economyIncome;
        long reservedPremiumRooms, reservedEconomyRooms;
        if (premiumBids >= premium) {
            premiumIncome = collect.get(Boolean.TRUE).stream().limit(premium).reduce(BigDecimal.ZERO, BigDecimal::add);
            reservedPremiumRooms = premium;

            List<BigDecimal> stepEconomy = collect.get(Boolean.FALSE).stream().limit(economy).toList();
            economyIncome = stepEconomy.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            reservedEconomyRooms = stepEconomy.size();
        } else {
            long missing = premium - premiumBids;
            BigDecimal partial = collect.get(Boolean.TRUE).stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            premiumIncome = collect.get(Boolean.FALSE).stream().limit(missing).reduce(BigDecimal.ZERO, BigDecimal::add).add(partial);
            reservedPremiumRooms = premiumBids + missing;

            List<BigDecimal> stepEconomy = collect.get(Boolean.FALSE).stream().skip(missing).limit(economy).toList();
            economyIncome = stepEconomy.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            reservedEconomyRooms = stepEconomy.size();
        }

        return new RoomAllocation(reservedPremiumRooms, premiumIncome, reservedEconomyRooms, economyIncome);
    }
}