package com.spa.spa.service.company;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spa.spa.dto.AdDTO;
import com.spa.spa.dto.ReservationDTO;
import com.spa.spa.entity.Ad;
import com.spa.spa.entity.Reservation;
import com.spa.spa.entity.User;
import com.spa.spa.enums.ReservationStatus;
import com.spa.spa.repository.AdRepository;
import com.spa.spa.repository.ReservationRepository;
import com.spa.spa.repository.UserRepository;

@Service
public class CompanyServiceImpl implements CompanyService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    public boolean postAd(Long userId, AdDTO adDTO) throws IOException{
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()){
            Ad ad = new Ad();

            ad.setServiceName(adDTO.getServiceName());
            ad.setDescription(adDTO.getDescription());
            ad.setImg(adDTO.getImg().getBytes());
            ad.setPrice(adDTO.getPrice());

            adRepository.save(ad);

            return true;  
        }

        return false;
    }



    public List<ReservationDTO> getAllReservation(){
        return reservationRepository.findAll().stream().map(Reservation::getReservationDTO).collect(Collectors.toList());
    }

    public boolean changeBookingStatus(Long bookingId, String status){
        Optional<Reservation> optionalReservation = reservationRepository.findById(bookingId);
        if(optionalReservation.isPresent()){
            Reservation existingReservation = optionalReservation.get();
            if(Objects.equals(status, "Approve")){
                existingReservation.setReservationStatus(ReservationStatus.APPROVED);
            }else{
                existingReservation.setReservationStatus(ReservationStatus.REJECTED);
            }

            reservationRepository.save(existingReservation);
            return true;
        }
        return false;
    }

    public boolean updateBooking(ReservationDTO reservationDTO){
        Optional<Reservation> reservationOptional = reservationRepository.findById(reservationDTO.getId());

        if (reservationOptional.isPresent()) {
        Reservation reservation = reservationOptional.get();

            // Only update fields that are allowed to be changed
            if (reservationDTO.getBookDate() != null) {
                reservation.setBookDate(reservationDTO.getBookDate());
            }

            if (reservationDTO.getReservationStatus() != null) {
                reservation.setReservationStatus(reservationDTO.getReservationStatus());
            }

            if (reservationDTO.getReviewStatus() != null) {
                reservation.setReviewStatus(reservationDTO.getReviewStatus());
            }

            reservationRepository.save(reservation);
            return true;
        }

        return false;
    }


    public boolean updateBookingDate(Long reservationId, String newDateStr) {
    Optional<Reservation> optionalReservation = reservationRepository.findById(reservationId);

    if (optionalReservation.isPresent()) {
        Reservation reservation = optionalReservation.get();
        try {
            // Same format used by frontend when making reservation
            // e.g. "2025-10-20T08:00:00"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date newDate = sdf.parse(newDateStr);

            reservation.setBookDate(newDate);
            reservationRepository.save(reservation);
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    return false;
}
}
