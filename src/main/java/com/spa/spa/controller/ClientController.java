package com.spa.spa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spa.spa.dto.ReservationDTO;
import com.spa.spa.dto.ReviewDTO;
import com.spa.spa.entity.User;
import com.spa.spa.repository.UserRepository;
import com.spa.spa.service.client.ClientService;
//import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/client")
@CrossOrigin(origins = "http://localhost:5173")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/book-service")
    public ResponseEntity<?> bookService(
        @RequestBody ReservationDTO reservationDTO
    ){
        boolean success = clientService.bookService(reservationDTO);
        System.out.println("✅ bookService endpoint hit with " + reservationDTO);


        if(success){
            return ResponseEntity.ok("booking successful");
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("booking fail");
        }
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<?> getAllBookingByUserId(Authentication authentication){
        String email = authentication.getName();

    // Find the user from the repository
        User user = userRepository.findFirstByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(clientService.getAllBookingByUserId(user.getId()));
    }

    @PostMapping("/review")
    public ResponseEntity<?> giveReview(@RequestBody ReviewDTO reviewDTO){
        Boolean success = clientService.giveReview(reviewDTO);
        if(success){
            return ResponseEntity.status(HttpStatus.OK).build();
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/getreview")
    public ResponseEntity<?> getAllReview(){
        try {
            return ResponseEntity.ok(clientService.getAllReviews());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error fetching reviews: " + e.getMessage());
        }
    }
    
}
