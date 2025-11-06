package com.spa.spa.service.client;

import java.util.List;

import com.spa.spa.dto.ReservationDTO;
import com.spa.spa.dto.ReviewDTO;

public interface ClientService {
    boolean bookService(ReservationDTO reservationDTO);
    List<ReservationDTO> getAllBookingByUserId(Long userId);
    Boolean giveReview(ReviewDTO reviewDTO);
    public List<ReviewDTO> getAllReviews();
}
