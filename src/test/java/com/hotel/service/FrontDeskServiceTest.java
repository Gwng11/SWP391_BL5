package com.hotel.service;

import com.hotel.entity.Reservation;
import com.hotel.entity.ReservationRoom;
import com.hotel.entity.Room;
import com.hotel.interfaces.*;
import com.hotel.ultis.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FrontDeskServiceTest {

    @Mock private IReservationRepository reservationRepo;
    @Mock private IReservationRoomRepository resRoomRepo;
    @Mock private IRoomAssignmentRepository assignmentRepo;
    @Mock private IRoomRepository roomRepo;

    @InjectMocks
    private FrontDeskService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReservationRoom rr = new ReservationRoom();
        rr.setReservationRoomId(1);
        rr.setReservationId(2);
        rr.setRoomTypeId(3);
        rr.setQuantity(1);
        when(resRoomRepo.findById(1)).thenReturn(rr);

        Reservation reservation = new Reservation();
        reservation.setStatusCode(Constants.RES_CONFIRMED);
        when(reservationRepo.findById(2)).thenReturn(reservation);
    }

    @Test
    void roomThatHasNotPassedInspectionCannotBeAssigned() {
        Room room = room(Constants.CLEAN_CLEAN);
        when(roomRepo.findById(4)).thenReturn(room);

        assertThrows(IllegalStateException.class, () -> service.assignRoom(1, 4, 9));
        verify(assignmentRepo, never()).assign(anyLong(), anyLong(), anyLong());
    }

    @Test
    void readyRoomCanBeAssigned() {
        Room room = room(Constants.CLEAN_READY);
        when(roomRepo.findById(4)).thenReturn(room);

        service.assignRoom(1, 4, 9);
        verify(assignmentRepo).assign(1, 4, 9);
    }

    private Room room(String clean) {
        Room r = new Room();
        r.setRoomId(4);
        r.setRoomTypeId(3);
        r.setOperationalStatus("AVAILABLE");
        r.setCleaningStatus(clean);
        return r;
    }
}