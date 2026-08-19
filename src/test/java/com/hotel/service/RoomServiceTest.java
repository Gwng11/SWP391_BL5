package com.hotel.service;

import com.hotel.entity.RoomType;
import com.hotel.interfaces.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomServiceTest {
    @Test void inactiveTypesAndUnsellableInventoryDoNotAppearInSearch(){IRoomTypeRepository types=mock(IRoomTypeRepository.class);IRoomRepository rooms=mock(IRoomRepository.class);IRoomRateRepository rates=mock(IRoomRateRepository.class);IReservationRepository reservations=mock(IReservationRepository.class);RoomType type=new RoomType();type.setRoomTypeId(1);type.setMaxAdults(2);type.setMaxChildren(1);type.setBasePrice(BigDecimal.TEN);when(types.findAllActive()).thenReturn(List.of(type));when(rates.hasStopSell(anyLong(),any(),any())).thenReturn(false);when(rates.findRates(anyLong(),any(),any())).thenReturn(Map.of());when(rooms.countSellableByType(1)).thenReturn(0);RoomService service=new RoomService(types,rooms,rates,reservations);assertTrue(service.searchAvailability(LocalDate.now().plusDays(1),LocalDate.now().plusDays(2),2,0).isEmpty());}

    @Test void inactiveTypeCannotBeOpenedOrBookedByDirectUrl(){IRoomTypeRepository types=mock(IRoomTypeRepository.class);IRoomRepository rooms=mock(IRoomRepository.class);IRoomRateRepository rates=mock(IRoomRateRepository.class);IReservationRepository reservations=mock(IReservationRepository.class);RoomType inactive=new RoomType();inactive.setRoomTypeId(9);inactive.setActive(false);when(types.findById(9)).thenReturn(inactive);RoomService service=new RoomService(types,rooms,rates,reservations);assertThrows(IllegalArgumentException.class,()->service.getTypeDetail(9));assertFalse(service.isAvailable(9,LocalDate.now().plusDays(1),LocalDate.now().plusDays(2),1,null));}
}
