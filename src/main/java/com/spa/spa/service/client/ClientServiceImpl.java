package com.spa.spa.service.client;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.spa.spa.dto.ReservationDTO;
import com.spa.spa.dto.ReviewDTO;
import com.spa.spa.entity.Ad;
import com.spa.spa.entity.Reservation;
import com.spa.spa.entity.Review;
import com.spa.spa.entity.User;
import com.spa.spa.enums.ReservationStatus;
import com.spa.spa.enums.ReviewStatus;
import com.spa.spa.repository.AdRepository;
import com.spa.spa.repository.ReservationRepository;
import com.spa.spa.repository.ReviewRepository;
import com.spa.spa.repository.UserRepository;

@Service
public class ClientServiceImpl implements ClientService{
    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public boolean bookService(ReservationDTO reservationDTO){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        System.out.println("username: "+ username);

        Optional<Ad> optionalAd = adRepository.findById(reservationDTO.getAdId());
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findFirstByEmail(username));


        if(optionalAd.isPresent() && optionalUser.isPresent()){
            Reservation reservation = new Reservation();

            reservation.setBookDate(reservationDTO.getBookDate());
            reservation.setReservationStatus(ReservationStatus.PENDING);
            reservation.setReviewStatus(ReviewStatus.FALSE);
            reservation.setUser(optionalUser.get());
            reservation.setAd(optionalAd.get());

            
            reservationRepository.save(reservation);
            
            return true;
        }
        return false;
    }


    public List<ReservationDTO> getAllBookingByUserId(Long userId){
        return reservationRepository.findAllByUserId(userId).stream().map(Reservation::getReservationDTO).collect(Collectors.toList());

    }

    public Boolean giveReview(ReviewDTO reviewDTO){
        Optional<User> optionalUser = userRepository.findById(reviewDTO.getUserId());
        Optional<Reservation> optionalBooking = reservationRepository.findById(reviewDTO.getBookId());

        if(optionalUser.isPresent() && optionalBooking.isPresent()){
            Review review = new Review();

            review.setReviewDate(new Date());
            review.setReview(reviewDTO.getReview());
            review.setRating(reviewDTO.getRating());

            review.setUser(optionalUser.get());
            review.setAd(optionalBooking.get().getAd());

            reviewRepository.save(review);

            Reservation booking = optionalBooking.get();
            booking.setReviewStatus(ReviewStatus.TRUE);

            reservationRepository.save(booking);

            return true;
        }

        return false;
    }

    public List<ReviewDTO> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                    .map(Review::getReviewDTO)
                    .collect(Collectors.toList());
    }

}
