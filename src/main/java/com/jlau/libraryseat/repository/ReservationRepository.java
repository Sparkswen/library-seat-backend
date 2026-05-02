package com.jlau.libraryseat.repository;

import com.jlau.libraryseat.entity.Reservation;
import com.jlau.libraryseat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.user = ?1 AND r.status IN ('PENDING', 'CHECKED_IN', 'PAUSED')")
    Optional<Reservation> findCurrentReservationByUser(User user);

    /**
     * 查询座位某天的预约记录
     */
    @Query("SELECT r FROM Reservation r WHERE r.seat.id = :seatId AND r.reservationDate = :date AND r.status IN :statuses")
    List<Reservation> findBySeatIdAndReservationDateAndStatusIn(
            @Param("seatId") Long seatId,
            @Param("date") LocalDate date,
            @Param("statuses") List<Reservation.Status> statuses
    );

    /**
     * 查询用户的所有预约
     */
    List<Reservation> findByUserOrderByCreateTimeDesc(User user);

    /**
     * 查询某座位的所有预约
     */
    List<Reservation> findBySeatId(Long seatId);
}