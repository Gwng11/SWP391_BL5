package com.hotel.service;

import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationRoom;
import com.hotel.entity.Room;
import com.hotel.interfaces.*;
import com.hotel.ultis.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FrontDeskServiceTest {
    private IReservationRepository reservations;private IReservationRoomRepository reservationRooms;private IRoomAssignmentRepository assignments;private IRoomRepository rooms;private FrontDeskService service;
    @BeforeEach void setUp(){reservations=mock(IReservationRepository.class);reservationRooms=mock(IReservationRoomRepository.class);assignments=mock(IRoomAssignmentRepository.class);rooms=mock(IRoomRepository.class);service=new FrontDeskService(reservations,reservationRooms,assignments,rooms,mock(IInvoiceRepository.class),mock(IInvoiceItemRepository.class),mock(PaymentService.class));ReservationRoom rr=new ReservationRoom();rr.setReservationRoomId(1);rr.setReservationId(2);rr.setRoomTypeId(3);rr.setQuantity(1);when(reservationRooms.findById(1)).thenReturn(rr);Reservation reservation=new Reservation();reservation.setStatusCode(Constants.RES_CONFIRMED);when(reservations.findById(2)).thenReturn(reservation);}
    @Test void roomThatHasNotPassedInspectionCannotBeAssigned(){Room room=room(Constants.CLEAN_CLEAN);when(rooms.findById(4)).thenReturn(room);assertThrows(IllegalStateException.class,()->service.assignRoom(1,4,9));verify(assignments,never()).assign(anyLong(),anyLong(),anyLong());}
    @Test void readyRoomCanBeAssigned(){Room room=room(Constants.CLEAN_READY);when(rooms.findById(4)).thenReturn(room);service.assignRoom(1,4,9);verify(assignments).assign(1,4,9);}
    private Room room(String clean){Room r=new Room();r.setRoomId(4);r.setRoomTypeId(3);r.setOperationalStatus(Constants.ROOM_AVAILABLE);r.setCleaningStatus(clean);return r;}
}
