package com.hotel.service;

import com.hotel.entity.Customer;
import com.hotel.entity.HotelService;
import com.hotel.entity.Reservation;
import com.hotel.entity.ServiceRequest;
import com.hotel.entity.User;
import com.hotel.interfaces.IHotelServiceRepository;
import com.hotel.interfaces.IReservationRepository;
import com.hotel.interfaces.IServiceRequestRepository;
import com.hotel.interfaces.IUserRepository;
import com.hotel.ultis.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceRequestServiceTest {
    private IHotelServiceRepository services;
    private IServiceRequestRepository requests;
    private IReservationRepository reservations;
    private IUserRepository users;
    private ServiceRequestService service;

    @BeforeEach
    void setUp() {
        services = mock(IHotelServiceRepository.class);
        requests = mock(IServiceRequestRepository.class);
        reservations = mock(IReservationRepository.class);
        users = mock(IUserRepository.class);
        service = new ServiceRequestService(services, requests, reservations, users);
    }

    @Test
    void customerRequestUsesOwnedCurrentStayAndPriceSnapshot() {
        User actor = user(5, Constants.ROLE_CUSTOMER);
        Customer customer = new Customer();
        customer.setCustomerId(9);
        Reservation stay = stay(17, 9);
        when(reservations.findByCustomer(9)).thenReturn(List.of(stay));
        HotelService catalogItem = hotelService(true);
        when(services.findById(3)).thenReturn(catalogItem);
        when(requests.insert(any())).thenReturn(44L);

        long id = service.createRequest(actor, customer, 999L, 3,
                new BigDecimal("2"), LocalDateTime.now().plusHours(2), " Giao tại phòng ");

        assertEquals(44L, id);
        ArgumentCaptor<ServiceRequest> captor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(requests).insert(captor.capture());
        assertEquals(17, captor.getValue().getReservationId());
        assertEquals(new BigDecimal("200.00"), captor.getValue().getTotalAmount());
        assertEquals("Giao tại phòng", captor.getValue().getNotes());
        verify(reservations, never()).findById(999L);
    }

    @Test
    void customerMustHaveExactlyOneCurrentStay() {
        User actor = user(5, Constants.ROLE_CUSTOMER);
        Customer customer = new Customer();
        customer.setCustomerId(9);
        when(reservations.findByCustomer(9)).thenReturn(List.of(stay(1, 9), stay(2, 9)));
        assertThrows(IllegalStateException.class,
                () -> service.resolveCurrentStay(actor, customer, null));
    }

    @Test
    void customerWithConfirmedReservationCanRequestServiceBeforeCheckIn() {
        User actor = user(5, Constants.ROLE_CUSTOMER);
        Customer customer = new Customer();
        customer.setCustomerId(9);
        Reservation confirmed = stay(17, 9);
        confirmed.setStatusCode(Constants.RES_CONFIRMED);
        when(reservations.findByCustomer(9)).thenReturn(List.of(confirmed));
        when(services.findById(3)).thenReturn(hotelService(true));
        when(requests.insert(any())).thenReturn(45L);

        assertEquals(45L, service.createRequest(actor, customer, null, 3,
                BigDecimal.ONE, LocalDateTime.now().plusDays(1), null));

        ArgumentCaptor<ServiceRequest> captor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(requests).insert(captor.capture());
        assertEquals(17L, captor.getValue().getReservationId());
    }

    @Test
    void activeStayIsPreferredOverConfirmedFutureReservation() {
        User actor = user(5, Constants.ROLE_CUSTOMER);
        Customer customer = new Customer();
        customer.setCustomerId(9);
        Reservation active = stay(17, 9);
        Reservation confirmed = stay(18, 9);
        confirmed.setStatusCode(Constants.RES_CONFIRMED);
        when(reservations.findByCustomer(9)).thenReturn(List.of(confirmed, active));

        assertSame(active, service.resolveCurrentStay(actor, customer, null));
    }

    @Test
    void pendingReservationCannotRequestService() {
        User actor = user(5, Constants.ROLE_CUSTOMER);
        Customer customer = new Customer();
        customer.setCustomerId(9);
        Reservation pending = stay(17, 9);
        pending.setStatusCode(Constants.RES_PENDING);
        when(reservations.findByCustomer(9)).thenReturn(List.of(pending));

        assertThrows(IllegalStateException.class,
                () -> service.resolveCurrentStay(actor, customer, null));
    }

    @Test
    void customerHistoryIsLoadedByAuthenticatedCustomerId() {
        User actor = user(5, Constants.ROLE_CUSTOMER);
        Customer customer = new Customer();
        customer.setCustomerId(9);
        ServiceRequest request = request(Constants.SR_COMPLETED);
        when(requests.findByCustomer(9)).thenReturn(List.of(request));

        assertEquals(List.of(request), service.getCustomerRequests(actor, customer));
        verify(requests).findByCustomer(9);

        assertThrows(IllegalStateException.class,
                () -> service.getCustomerRequests(user(2, Constants.ROLE_RECEPTIONIST), customer));
        assertThrows(IllegalStateException.class,
                () -> service.getCustomerRequests(actor, null));
    }

    @Test
    void receptionistUsesValidatedStayContextWithoutASelectionForm() {
        User actor = user(2, Constants.ROLE_RECEPTIONIST);
        Reservation stay = stay(17, 9);
        stay.setStatusCode(Constants.RES_CONFIRMED);
        when(reservations.findById(17)).thenReturn(stay);
        assertSame(stay, service.resolveCurrentStay(actor, null, 17L));
        assertThrows(IllegalStateException.class,
                () -> service.resolveCurrentStay(actor, null, null));
    }

    @Test
    void inactiveServiceAndPastTimeAreRejected() {
        User actor = user(2, Constants.ROLE_RECEPTIONIST);
        when(reservations.findById(17)).thenReturn(stay(17, 9));
        when(services.findById(3)).thenReturn(hotelService(false));
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(
                actor, null, 17L, 3, BigDecimal.ONE, LocalDateTime.now().plusHours(1), null));
        when(services.findById(3)).thenReturn(hotelService(true));
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(
                actor, null, 17L, 3, BigDecimal.ONE, LocalDateTime.now().minusHours(1), null));
        verify(requests, never()).insert(any());
    }

    @Test
    void onlyEligibleGeneralServiceStaffCanBeAssigned() {
        User eligible = user(8, Constants.ROLE_SERVICE_STAFF);
        eligible.setDepartmentCode("GENERAL_SERVICE");
        when(users.findActiveServiceStaffWithWorkload("GENERAL_SERVICE")).thenReturn(List.of(eligible));
        service.assign(10, 8);
        verify(requests).assign(10, 8);
        assertThrows(IllegalArgumentException.class, () -> service.assign(10, 99));
    }

    @Test
    void staffActionsPassActorIdToAssignmentProtectedRepositoryMethods() {
        User staff = user(8, Constants.ROLE_SERVICE_STAFF);
        service.claim(9, staff);
        service.start(10, staff);
        service.complete(10, staff);
        service.reportUnable(11, staff, "Thiếu thiết bị");
        verify(requests).claim(9, 8);
        verify(requests).start(10, 8);
        verify(requests).completeAndAddCharge(10, 8);
        verify(requests).reportUnable(11, 8, "Thiếu thiết bị");

        User receptionist = user(2, Constants.ROLE_RECEPTIONIST);
        assertThrows(IllegalStateException.class, () -> service.claim(9, receptionist));
        assertThrows(IllegalStateException.class, () -> service.complete(10, receptionist));
    }

    @Test
    void staffWorkQueueUsesSharedPendingAndOwnAssignmentsRepositoryView() {
        User staff = user(8, Constants.ROLE_SERVICE_STAFF);
        ServiceRequest pending = request(Constants.SR_PENDING);
        when(requests.findAssignedToStaff(8, null)).thenReturn(List.of(pending));

        assertEquals(List.of(pending), service.getRequestsFor(staff, null));
        verify(requests).findAssignedToStaff(8, null);
    }

    @Test
    void receptionistCanRescheduleReturnedPendingRequest() {
        LocalDateTime newTime = LocalDateTime.now().plusHours(3);
        service.reschedule(12, newTime, "Theo yêu cầu của khách");
        verify(requests).reschedule(12, newTime, "Theo yêu cầu của khách");
        assertThrows(IllegalArgumentException.class,
                () -> service.reschedule(12, LocalDateTime.now().minusHours(1), null));
    }

    @Test
    void finalInvoiceIsBlockedWhileAnyServiceRequestIsActive() {
        ServiceRequest completed = request("COMPLETED");
        ServiceRequest active = request("IN_PROGRESS");
        when(requests.findByReservation(17)).thenReturn(List.of(completed, active));

        assertThrows(IllegalStateException.class,
                () -> service.requireReadyForFinalInvoice(17));

        when(requests.findByReservation(17)).thenReturn(List.of(completed));
        assertDoesNotThrow(() -> service.requireReadyForFinalInvoice(17));
    }

    @Test
    void checkoutIsBlockedUntilEveryCompletedServiceIsInvoiced() {
        ServiceRequest completed = request("COMPLETED");
        when(requests.findByReservation(17)).thenReturn(List.of(completed));
        when(requests.findCompletedNotInvoiced(17)).thenReturn(List.of(completed));

        assertThrows(IllegalStateException.class,
                () -> service.requireReadyForCheckout(17));

        when(requests.findCompletedNotInvoiced(17)).thenReturn(List.of());
        assertDoesNotThrow(() -> service.requireReadyForCheckout(17));
    }

    private User user(long id, String role) {
        User user = new User();
        user.setUserId(id);
        user.setRoleCode(role);
        return user;
    }

    private Reservation stay(long id, long customerId) {
        Reservation reservation = new Reservation();
        reservation.setReservationId(id);
        reservation.setCustomerId(customerId);
        reservation.setStatusCode(Constants.RES_CHECKED_IN);
        return reservation;
    }

    private HotelService hotelService(boolean active) {
        HotelService result = new HotelService();
        result.setHotelServiceId(3);
        result.setServiceCode("SPA");
        result.setServiceName("Spa");
        result.setUnitName("lượt");
        result.setUnitPrice(new BigDecimal("100"));
        result.setActive(active);
        return result;
    }

    private ServiceRequest request(String status) {
        ServiceRequest result = new ServiceRequest();
        result.setStatusCode(status);
        return result;
    }
}
