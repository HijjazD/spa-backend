package com.spa.spa.service.company;

import java.io.IOException;
import java.util.List;

import com.spa.spa.dto.AdDTO;
import com.spa.spa.dto.ReservationDTO;

public interface CompanyService {
    boolean postAd(Long userId, AdDTO adDTO) throws IOException;
    
    List<ReservationDTO> getAllReservation();
    boolean changeBookingStatus(Long bookingId, String status);
    boolean updateBookingDate(Long reservationId, String newDateStr);
    boolean updateBooking(ReservationDTO reservationDTO);
}
