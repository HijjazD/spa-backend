package com.spa.spa.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spa.spa.dto.AdDTO;
import com.spa.spa.service.company.CompanyService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @PostMapping("/ad/{userId}")
    public ResponseEntity<?> postAd(
        @PathVariable Long userId,
        @ModelAttribute AdDTO adDTO
    ) throws IOException{
        boolean success = companyService.postAd(userId, adDTO);
        if(success){
            return ResponseEntity.status(HttpStatus.OK).build();

        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @GetMapping("/reservations")
    public ResponseEntity<?> getAllReservations(){
        return ResponseEntity.ok(companyService.getAllReservation());
    }

    @GetMapping("/booking/{bookingId}/{status}")
    public ResponseEntity<?> changeBookingStatus(@PathVariable Long bookingId, @PathVariable String status){
        boolean success = companyService.changeBookingStatus(bookingId, status);
        if(success) return ResponseEntity.ok().build();
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/update-reservation-date/{reservationId}")
    public ResponseEntity<?> updateBookingDate(
        @PathVariable Long reservationId,
        @RequestBody  Map<String, String> body
    ){
        String newDateStr = body.get("bookDate"); // Expecting something like "2025-10-20T08:00:00"
        boolean success = companyService.updateBookingDate(reservationId, newDateStr);

        if (success) {
            return ResponseEntity.ok("Booking date updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }
    }
}
