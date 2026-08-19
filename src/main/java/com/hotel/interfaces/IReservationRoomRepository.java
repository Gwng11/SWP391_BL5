package com.hotel.interfaces;

import com.hotel.entity.ReservationRoom;
import java.util.List;

public interface IReservationRoomRepository {
    List<ReservationRoom> findByReservation(long reservationId);
    ReservationRoom findById(long reservationRoomId);
}
