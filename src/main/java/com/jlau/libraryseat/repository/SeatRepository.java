package com.jlau.libraryseat.repository;

import com.jlau.libraryseat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByFloor(Integer floor);
}
