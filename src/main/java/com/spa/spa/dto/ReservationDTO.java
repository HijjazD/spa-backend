package com.spa.spa.dto;

import java.util.Date;

import com.spa.spa.enums.ReservationStatus;
import com.spa.spa.enums.ReviewStatus;

import lombok.Data;

@Data
public class ReservationDTO {
    private Long id;

    private String serviceName;

    private ReservationStatus reservationStatus;

    private ReviewStatus reviewStatus;

    private Date bookDate;

    //private Long userId;

    private String userName;

    private Long adId;

    private String userPhone;


}
