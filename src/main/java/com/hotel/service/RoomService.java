package com.hotel.service;

import com.hotel.entity.RoomAvailability;
import com.hotel.entity.RoomRate;
import com.hotel.entity.RoomType;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.interfaces.IRoomRateRepository;
import com.hotel.interfaces.IRoomRepository;
import com.hotel.interfaces.IRoomTypeRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRateRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** F02 - Tìm phòng trống, F03 - Chi tiết phòng */
public class RoomService {

    private final IRoomTypeRepository roomTypeRepo = new RoomTypeRepository();
    private final IRoomRepository roomRepo = new RoomRepository();
    private final IRoomRateRepository rateRepo = new RoomRateRepository();
    private final IReservationRepository reservationRepo = new ReservationRepository();

    public List<RoomType> getAllActiveTypes() {
        return roomTypeRepo.findAllActive();
    }

    public RoomType getTypeDetail(long roomTypeId) {
        return roomTypeRepo.findById(roomTypeId);
    }

    /**
     * F02: tìm loại phòng còn trống trong khoảng ngày, đủ sức chứa.
     * available = tổng phòng bán được - số phòng đã bị giữ bởi đơn PENDING/CONFIRMED/CHECKED_IN giao ngày.
     */
    public List<RoomAvailability> searchAvailability(LocalDate checkIn, LocalDate checkOut,
                                                     int adults, int children) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("Ngày trả phòng phải sau ngày nhận phòng");
        if (checkIn.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Ngày nhận phòng không được ở quá khứ");

        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        List<RoomAvailability> results = new ArrayList<>();
        for (RoomType t : roomTypeRepo.findAllActive()) {
            if (t.getMaxAdults() < adults || t.getMaxChildren() < children) continue;      // đủ sức chứa
            if (rateRepo.hasStopSell(t.getRoomTypeId(), checkIn, checkOut)) continue;       // bị chặn bán
            int total = roomRepo.countSellableByType(t.getRoomTypeId());
            int sold = reservationRepo.countSoldRooms(t.getRoomTypeId(), checkIn, checkOut, null);
            int available = total - sold;
            if (available <= 0) continue;

            BigDecimal totalPrice = calcStayPrice(t, checkIn, checkOut);
            RoomAvailability av = new RoomAvailability();
            av.setRoomType(t);
            av.setAvailableRooms(available);
            av.setNights(nights);
            av.setTotalPricePerRoom(totalPrice);
            av.setNightlyAvgPrice(totalPrice.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP));
            results.add(av);
        }
        return results;
    }

    /** Tổng giá 1 phòng cho cả kỳ ở: ưu tiên giá theo ngày (room_rates), thiếu thì dùng base_price */
    public BigDecimal calcStayPrice(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        Map<LocalDate, RoomRate> rates = rateRepo.findRates(type.getRoomTypeId(), checkIn, checkOut);
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate d = checkIn; d.isBefore(checkOut); d = d.plusDays(1)) {
            RoomRate r = rates.get(d);
            total = total.add(r != null ? r.getNightlyPrice() : type.getBasePrice());
        }
        return total;
    }

    /** Kiểm tra 1 loại phòng còn đủ số lượng không (dùng lại khi tạo/sửa đơn) */
    public boolean isAvailable(long roomTypeId, LocalDate checkIn, LocalDate checkOut,
                               int quantity, Long excludeReservationId) {
        if (rateRepo.hasStopSell(roomTypeId, checkIn, checkOut)) return false;
        int total = roomRepo.countSellableByType(roomTypeId);
        int sold = reservationRepo.countSoldRooms(roomTypeId, checkIn, checkOut, excludeReservationId);
        return total - sold >= quantity;
    }
}
