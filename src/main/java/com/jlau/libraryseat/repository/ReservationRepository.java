package com.jlau.libraryseat.repository;

import com.jlau.libraryseat.entity.Reservation;
import com.jlau.libraryseat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("SELECT r FROM Reservation r WHERE r.user = ?1 AND r.status IN ('PENDING', 'CHECKED_IN', 'PAUSED')")
    Optional<Reservation> findCurrentReservationByUser(User user);
}
