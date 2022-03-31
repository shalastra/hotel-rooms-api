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

        if (premiumBidsAmount >= premiumRequested) {
            return allocateRooms(premiumBids, premiumRequested, economyBids, economyRequested);
        } else {
            return allocateRooms(premiumBids, premiumRequested, economyBids, economyRequested, premiumBidsAmount);
        }
    }

    private RoomAllocation allocateRooms(List<BigDecimal> premiumBids, long premiumRequested,
                                         List<BigDecimal> economyBids, long economyRequested,
                                         long premiumBidsAmount) {
        long premiumRoomsLeft = premiumRequested - premiumBidsAmount;

        BigDecimal premiumIncome = sumBids(premiumBids).add(sumBids(economyBids, premiumRoomsLeft));
        long reservedPremiumRooms = premiumBidsAmount + premiumRoomsLeft;

        BigDecimal economyIncome = sumBids(economyBids, economyRequested, premiumRoomsLeft);
        long reservedEconomyRooms = economyBids
                .stream()
                .skip(premiumRoomsLeft)
                .limit(economyRequested)
                .count();

        return new RoomAllocation(reservedPremiumRooms, premiumIncome, reservedEconomyRooms, economyIncome);
    }

    private RoomAllocation allocateRooms(List<BigDecimal> premiumBids, long premiumRequested,
                                         List<BigDecimal> economyBids, long economyRequested) {
        BigDecimal premiumIncome = sumBids(premiumBids, premiumRequested);
        long reservedPremiumRooms = premiumRequested;

        BigDecimal economyIncome = sumBids(economyBids, economyRequested);
        long reservedEconomyRooms = economyBids
                .stream()
                .limit(economyRequested)
                .count();

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
