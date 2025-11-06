package com.spa.spa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spa.spa.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>{
    List<Reservation> findAllByUserId(Long userId);
    
}
