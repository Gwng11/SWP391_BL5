package com.hotel.interfaces;

import com.hotel.entity.ReservationGuest;
import java.util.List;

public interface IReservationGuestRepository {
    List<ReservationGuest> findByReservation(long reservationId);
    /** F07: thay toàn bộ danh sách khách ở (transaction delete + insert) */
    void replaceGuests(long reservationId, List<ReservationGuest> guests);
}
