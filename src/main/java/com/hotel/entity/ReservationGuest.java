package com.hotel.entity;

import java.time.LocalDate;

public class ReservationGuest {
    private long reservationGuestId;
    private long reservationId;
    private Long customerId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String idDocumentType;
    private String idDocumentNumber;
    private String nationality;
    private boolean primaryGuest;

    public long getReservationGuestId() { return reservationGuestId; }
    public void setReservationGuestId(long reservationGuestId) { this.reservationGuestId = reservationGuestId; }
    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getIdDocumentType() { return idDocumentType; }
    public void setIdDocumentType(String idDocumentType) { this.idDocumentType = idDocumentType; }
    public String getIdDocumentNumber() { return idDocumentNumber; }
    public void setIdDocumentNumber(String idDocumentNumber) { this.idDocumentNumber = idDocumentNumber; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public boolean isPrimaryGuest() { return primaryGuest; }
    public void setPrimaryGuest(boolean primaryGuest) { this.primaryGuest = primaryGuest; }
}
