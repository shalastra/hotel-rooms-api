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

    public RoomAllocation allocate(final long premiumRequested,
                                   final long economyRequested,
                                   final List<BigDecimal> bids) {
        var roomStandards = divideByPremiumAndEconomy(bids);

        List<BigDecimal> premiumBids = roomStandards.get(Boolean.TRUE);
        List<BigDecimal> economyBids = roomStandards.get(Boolean.FALSE);

        long premiumBidsAmount = premiumBids.size();

        BigDecimal premiumIncome, economyIncome;
        long reservedPremiumRooms, reservedEconomyRooms;
        if (premiumBidsAmount >= premiumRequested) {
            premiumIncome = sumBids(premiumBids, premiumRequested);
            reservedPremiumRooms = premiumRequested;

            economyIncome = sumBids(economyBids, economyRequested);
            reservedEconomyRooms = economyBids.stream().limit(economyRequested).count();
        } else {
            long missing = premiumRequested - premiumBidsAmount;
            BigDecimal partial = sumBids(premiumBids);
            premiumIncome = sumBids(economyBids, missing).add(partial);
            reservedPremiumRooms = premiumBidsAmount + missing;

            List<BigDecimal> stepEconomy = economyBids.stream().skip(missing).limit(economyRequested).toList();
            economyIncome = sumBids(stepEconomy);
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

    private BigDecimal sumBids(final List<BigDecimal> bids) {
        return sumBids(bids, bids.size());
    }

    private BigDecimal sumBids(final List<BigDecimal> bids, final long limit) {
        return bids
                .stream()
                .limit(limit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumBids(final List<BigDecimal> bids, final long limit, final long skip) {
        return bids
                .stream()
                .skip(skip)
                .limit(limit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
