package com.hotel.interfaces;

import com.hotel.entity.RoomRate;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IRoomRateRepository {
    /** Giá theo ngày trong khoảng [from, toExclusive) - key là ngày */
    Map<LocalDate, RoomRate> findRates(long roomTypeId, LocalDate from, LocalDate toExclusive);
    /** Có ngày nào bị chặn bán (stop_sell) trong khoảng ở không */
    boolean hasStopSell(long roomTypeId, LocalDate from, LocalDate toExclusive);
    List<RoomRate> findRateList(long roomTypeId, LocalDate from, LocalDate toInclusive);
    void upsertRange(long roomTypeId, LocalDate from, LocalDate toInclusive,
                     java.math.BigDecimal nightlyPrice, boolean stopSell);
    void updatePricing(long roomTypeId, java.math.BigDecimal basePrice, LocalDate from,
                       LocalDate toInclusive, java.math.BigDecimal nightlyPrice, boolean stopSell);
}
