package com.example.hotelroomapi.booking;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
class RoomAllocator {

    private static final BigDecimal LIMIT = new BigDecimal("100");

    private static boolean divideBy(BigDecimal price) {
        return price.compareTo(LIMIT) >= 0;
    }

    public RoomAllocation allocate(final long premium, final long economy, final List<BigDecimal> bids) {
        var roomStandards = divideByPremiumAndEconomy(bids);

        List<BigDecimal> premiumBids = roomStandards.get(Boolean.TRUE);
        List<BigDecimal> economyBids = roomStandards.get(Boolean.FALSE);

        long premiumBidsAmount = premiumBids.size();

        BigDecimal premiumIncome, economyIncome;
        long reservedPremiumRooms, reservedEconomyRooms;
        if (premiumBidsAmount >= premium) {
            premiumIncome = premiumBids.stream().limit(premium).reduce(BigDecimal.ZERO, BigDecimal::add);
            reservedPremiumRooms = premium;

            List<BigDecimal> stepEconomy = economyBids.stream().limit(economy).toList();
            economyIncome = stepEconomy.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            reservedEconomyRooms = stepEconomy.size();
        } else {
            long missing = premium - premiumBidsAmount;
            BigDecimal partial = premiumBids.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            premiumIncome = economyBids.stream().limit(missing).reduce(BigDecimal.ZERO, BigDecimal::add).add(partial);
            reservedPremiumRooms = premiumBidsAmount + missing;

            List<BigDecimal> stepEconomy = economyBids.stream().skip(missing).limit(economy).toList();
            economyIncome = stepEconomy.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            reservedEconomyRooms = stepEconomy.size();
        }

        return new RoomAllocation(reservedPremiumRooms, premiumIncome, reservedEconomyRooms, economyIncome);
    }

    private Map<Boolean, List<BigDecimal>> divideByPremiumAndEconomy(final List<BigDecimal> bids) {
        return bids
                .stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors
                        .partitioningBy(RoomAllocator::divideBy));
    }
}
