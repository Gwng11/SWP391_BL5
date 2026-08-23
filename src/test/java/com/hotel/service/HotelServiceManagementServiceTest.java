package com.hotel.service;

import com.hotel.entity.HotelService;
import com.hotel.interfaces.IHotelServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HotelServiceManagementServiceTest {
    private IHotelServiceRepository repository;
    private HotelServiceManagementService service;

    @BeforeEach
    void setUp() {
        repository = mock(IHotelServiceRepository.class);
        service = new HotelServiceManagementService(repository);
    }

    @Test
    void createNormalizesAndValidatesCatalogItem() {
        HotelService item = item();
        item.setServiceCode(" spa-01 ");
        when(repository.findAll()).thenReturn(List.of());
        service.create(item);
        assertEquals("SPA-01", item.getServiceCode());
        assertTrue(item.isActive());
        verify(repository).insert(item);
    }

    @Test
    void duplicateCodeAndNegativePriceAreRejected() {
        HotelService existing = item();
        existing.setHotelServiceId(1);
        when(repository.findAll()).thenReturn(List.of(existing));
        assertThrows(IllegalArgumentException.class, () -> service.create(item()));
        HotelService negative = item();
        negative.setUnitPrice(new BigDecimal("-1"));
        assertThrows(IllegalArgumentException.class, () -> service.create(negative));
    }

    @Test
    void deleteOnlyWhenThereIsNoRequestHistory() {
        HotelService item = item();
        item.setHotelServiceId(7);
        when(repository.findById(7)).thenReturn(item);
        when(repository.countServiceRequests(7)).thenReturn(1);
        assertThrows(IllegalStateException.class, () -> service.deleteIfSafe(7));
        verify(repository, never()).delete(7);

        when(repository.countServiceRequests(7)).thenReturn(0);
        service.deleteIfSafe(7);
        verify(repository).delete(7);
    }

    private HotelService item() {
        HotelService item = new HotelService();
        item.setServiceCode("SPA");
        item.setServiceName("Spa");
        item.setUnitName("lượt");
        item.setUnitPrice(new BigDecimal("100000"));
        return item;
    }
}
