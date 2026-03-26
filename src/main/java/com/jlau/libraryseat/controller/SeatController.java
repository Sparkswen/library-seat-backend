package com.jlau.libraryseat.controller;

import com.jlau.libraryseat.common.Result;
import com.jlau.libraryseat.entity.Reservation;
import com.jlau.libraryseat.entity.Seat;
import com.jlau.libraryseat.entity.User;
import com.jlau.libraryseat.repository.ReservationRepository;
import com.jlau.libraryseat.repository.SeatRepository;
import com.jlau.libraryseat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seat")
@CrossOrigin(origins = "*")
public class SeatController {

    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/list")
    public Result<?> getSeatList() {
        List<Seat> seats = seatRepository.findAll();

        Map<Integer, List<Map<String, Object>>> grouped = seats.stream()
                .collect(Collectors.groupingBy(Seat::getFloor,
                        Collectors.mapping(s -> Map.of(
                                "id", s.getId(),
                                "seatNo", s.getSeatNo(),
                                "area", s.getArea(),
                                "status", s.getStatus().name(),
                                "statusDisplay", s.getStatus().getDisplayName()
                        ), Collectors.toList())));

        return Result.success(grouped);
    }

    @PostMapping("/reserve")
    @Transactional
    public Result<?> reserve(@RequestBody Map<String, Object> params) {
        Long seatId = Long.valueOf(params.get("seatId").toString());
        String studentNo = params.get("studentNo").toString();

        User user = userRepository.findByStudentNo(studentNo).orElse(null);
        if (user == null) return Result.error("用户不存在");

        if (reservationRepository.findCurrentReservationByUser(user).isPresent()) {
            return Result.error("已有进行中的预约");
        }

        Seat seat = seatRepository.findById(seatId).orElse(null);
        if (seat == null) return Result.error("座位不存在");
        if (seat.getStatus() != Seat.Status.AVAILABLE) {
            return Result.error("座位已被占用");
        }

        Reservation r = new Reservation();
        r.setUser(user);
        r.setSeat(seat);
        r.setStartTime(LocalDateTime.now());
        r.setEndTime(LocalDateTime.now().plusHours(4));
        r.setStatus(Reservation.Status.PENDING);

        seat.setStatus(Seat.Status.RESERVED);

        reservationRepository.save(r);
        seatRepository.save(seat);

        return Result.success(Map.of(
                "reservationId", r.getId(),
                "seatNo", seat.getSeatNo(),
                "message", "预约成功，请在30分钟内签到"
        ));
    }

    @PostMapping("/check-in/{id}")
    @Transactional
    public Result<?> checkIn(@PathVariable Long id) {
        Reservation r = reservationRepository.findById(id).orElse(null);
        if (r == null) return Result.error("预约不存在");

        if (r.getStatus() != Reservation.Status.PENDING) {
            return Result.error("状态错误");
        }

        if (r.getStartTime().plusMinutes(30).isBefore(LocalDateTime.now())) {
            r.setStatus(Reservation.Status.NO_SHOW);
            r.getSeat().setStatus(Seat.Status.AVAILABLE);
            reservationRepository.save(r);

            User u = r.getUser();
            u.setCreditScore(u.getCreditScore() - 10);
            userRepository.save(u);
            return Result.error("签到超时，信用分-10");
        }

        r.setStatus(Reservation.Status.CHECKED_IN);
        r.setActualCheckIn(LocalDateTime.now());
        r.getSeat().setStatus(Seat.Status.OCCUPIED);
        reservationRepository.save(r);

        return Result.success("签到成功");
    }

    @PostMapping("/check-out/{id}")
    @Transactional
    public Result<?> checkOut(@PathVariable Long id) {
        Reservation r = reservationRepository.findById(id).orElse(null);
        if (r == null) return Result.error("预约不存在");

        if (r.getStatus() != Reservation.Status.CHECKED_IN) {
            return Result.error("状态错误");
        }

        LocalDateTime now = LocalDateTime.now();
        r.setActualCheckOut(now);

        int minutes = (int) ChronoUnit.MINUTES.between(r.getActualCheckIn(), now);
        r.setStudyMinutes(minutes);
        r.setStatus(Reservation.Status.COMPLETED);
        r.getSeat().setStatus(Seat.Status.AVAILABLE);

        User user = r.getUser();
        user.setTotalStudyMinutes(user.getTotalStudyMinutes() + minutes);
        user.setPoints(user.getPoints() + minutes / 10);
        user.setLevelTitle(User.LevelTitle.calculateByMinutes(user.getTotalStudyMinutes()));

        reservationRepository.save(r);
        userRepository.save(user);

        return Result.success(Map.of(
                "studyMinutes", minutes,
                "pointsEarned", minutes / 10,
                "currentLevel", user.getLevelTitle().getDisplayName()
        ));
    }

    @GetMapping("/current")
    public Result<?> getCurrent(@RequestParam String studentNo) {
        User user = userRepository.findByStudentNo(studentNo).orElse(null);
        if (user == null) return Result.error("用户不存在");

        return reservationRepository.findCurrentReservationByUser(user)
                .map(r -> Result.success(Map.of(
                        "reservationId", r.getId(),
                        "seatNo", r.getSeat().getSeatNo(),
                        "status", r.getStatus().name(),
                        "statusDisplay", r.getStatus().getDisplayName(),
                        "startTime", r.getStartTime()
                )))
                .orElse(Result.success(null));
    }
}
