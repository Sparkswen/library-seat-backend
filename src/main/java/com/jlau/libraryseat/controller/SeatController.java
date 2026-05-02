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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
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

    // 时间段配置（每30分钟一个时段）
    private static final int TIME_SLOT_MINUTES = 30;
    private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(22, 0);

    /**
     * 获取座位列表（按楼层分组）
     */
    @GetMapping("/list")
    public Result<?> getSeatList() {
        List<Seat> seats = seatRepository.findAll();

        Map<Integer, List<Map<String, Object>>> grouped = seats.stream()
                .collect(Collectors.groupingBy(Seat::getFloor,
                        Collectors.mapping(s -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", s.getId());
                            map.put("seatNo", s.getSeatNo());
                            map.put("area", s.getArea());
                            map.put("status", s.getStatus().name());
                            map.put("statusDisplay", s.getStatus().getDisplayName());
                            return map;
                        }, Collectors.toList())));

        return Result.success(grouped);
    }

    /**
     * 获取座位可预约时段
     */
    @GetMapping("/time-slots")
    public Result<?> getTimeSlots(
            @RequestParam Long seatId,
            @RequestParam String date) {

        Seat seat = seatRepository.findById(seatId).orElse(null);
        if (seat == null) {
            return Result.error("座位不存在");
        }

        LocalDate queryDate = LocalDate.parse(date);
        LocalDate today = LocalDate.now();

        // 不能预约过去的日期
        if (queryDate.isBefore(today)) {
            return Result.error("不能预约过去的日期");
        }

        // 最多提前7天预约
        if (queryDate.isAfter(today.plusDays(7))) {
            return Result.error("最多提前7天预约");
        }

        // 生成所有时段
        List<Map<String, Object>> timeSlots = generateTimeSlots(seatId, queryDate);

        return Result.success(Map.of(
                "seatId", seatId,
                "seatNo", seat.getSeatNo(),
                "date", date,
                "timeSlots", timeSlots
        ));
    }

    /**
     * 生成时段列表
     */
    private List<Map<String, Object>> generateTimeSlots(Long seatId, LocalDate date) {
        List<Map<String, Object>> slots = new ArrayList<>();

        // 获取该座位当天的所有预约
        List<Reservation> reservations = reservationRepository
                .findBySeatIdAndReservationDateAndStatusIn(
                        seatId,
                        date,
                        Arrays.asList(Reservation.Status.PENDING, Reservation.Status.CHECKED_IN)
                );

        LocalTime current = OPEN_TIME;
        LocalDateTime now = LocalDateTime.now();

        while (current.isBefore(CLOSE_TIME)) {
            LocalTime slotEnd = current.plusMinutes(TIME_SLOT_MINUTES);

            // 检查该时段是否被预约
            boolean isOccupied = isTimeSlotOccupied(current, slotEnd, reservations);

            // 检查是否已过期（今天的时段）
            boolean isExpired = date.equals(LocalDate.now()) &&
                    current.isBefore(now.toLocalTime());

            Map<String, Object> slot = new HashMap<>();
            slot.put("startTime", current.format(DateTimeFormatter.ofPattern("HH:mm")));
            slot.put("endTime", slotEnd.format(DateTimeFormatter.ofPattern("HH:mm")));
            slot.put("available", !isOccupied && !isExpired);
            slot.put("expired", isExpired);

            slots.add(slot);
            current = slotEnd;
        }

        return slots;
    }

    /**
     * 检查时段是否被占用
     */
    private boolean isTimeSlotOccupied(LocalTime start, LocalTime end, List<Reservation> reservations) {
        for (Reservation r : reservations) {
            // 时段重叠判断
            if (r.getStartTime().isBefore(end) && r.getEndTime().isAfter(start)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 预约座位（带时段）
     */
    @PostMapping("/reserve")
    @Transactional
    public Result<?> reserve(@RequestBody Map<String, Object> params) {
        Long seatId = Long.valueOf(params.get("seatId").toString());
        String studentNo = params.get("studentNo").toString();
        String dateStr = params.get("date").toString();
        String startTimeStr = params.get("startTime").toString();
        String endTimeStr = params.get("endTime").toString();

        User user = userRepository.findByStudentNo(studentNo).orElse(null);
        if (user == null) return Result.error("用户不存在");

        // 检查是否有进行中的预约
        if (reservationRepository.findCurrentReservationByUser(user).isPresent()) {
            return Result.error("已有进行中的预约");
        }

        Seat seat = seatRepository.findById(seatId).orElse(null);
        if (seat == null) return Result.error("座位不存在");
        if (seat.getStatus() == Seat.Status.MAINTENANCE) {
            return Result.error("座位维护中");
        }

        LocalDate date = LocalDate.parse(dateStr);
        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);

        // 校验时间
        if (startTime.isBefore(OPEN_TIME) || endTime.isAfter(CLOSE_TIME)) {
            return Result.error("超出开放时间");
        }
        if (!startTime.isBefore(endTime)) {
            return Result.error("结束时间必须晚于开始时间");
        }
        if (ChronoUnit.MINUTES.between(startTime, endTime) < 30) {
            return Result.error("最少预约30分钟");
        }
        if (ChronoUnit.MINUTES.between(startTime, endTime) > 240) {
            return Result.error("单次最多预约4小时");
        }

        // 校验时段冲突
        List<Reservation> conflicts = reservationRepository
                .findBySeatIdAndReservationDateAndStatusIn(
                        seatId, date,
                        Arrays.asList(Reservation.Status.PENDING, Reservation.Status.CHECKED_IN)
                );

        for (Reservation r : conflicts) {
            if (r.getStartTime().isBefore(endTime) && r.getEndTime().isAfter(startTime)) {
                return Result.error("该时段已被预约，请选择其他时段");
            }
        }

        // 创建预约
        Reservation r = new Reservation();
        r.setUser(user);
        r.setSeat(seat);
        r.setReservationDate(date);
        r.setStartTime(startTime);
        r.setEndTime(endTime);
        r.setStatus(Reservation.Status.PENDING);

        // 如果是今天的预约，设置签到截止时间
        if (date.equals(LocalDate.now())) {
            // 预约开始时间后30分钟内签到
        }

        reservationRepository.save(r);

        return Result.success(Map.of(
                "reservationId", r.getId(),
                "seatNo", seat.getSeatNo(),
                "date", dateStr,
                "startTime", startTimeStr,
                "endTime", endTimeStr,
                "message", "预约成功，请在开始前30分钟内签到"
        ));
    }

    /**
     * 签到
     */
    @PostMapping("/check-in/{id}")
    @Transactional
    public Result<?> checkIn(@PathVariable Long id) {
        Reservation r = reservationRepository.findById(id).orElse(null);
        if (r == null) return Result.error("预约不存在");

        if (r.getStatus() != Reservation.Status.PENDING) {
            return Result.error("状态错误");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationStart = LocalDateTime.of(r.getReservationDate(), r.getStartTime());

        // 只能在预约开始前30分钟到开始后30分钟内签到
        if (now.isBefore(reservationStart.minusMinutes(30))) {
            return Result.error("签到时间未到，请在开始前30分钟内签到");
        }

        if (now.isAfter(reservationStart.plusMinutes(30))) {
            r.setStatus(Reservation.Status.NO_SHOW);
            User u = r.getUser();
            u.setCreditScore(u.getCreditScore() - 10);
            userRepository.save(u);
            return Result.error("签到超时，信用分-10");
        }

        r.setStatus(Reservation.Status.CHECKED_IN);
        r.setActualCheckIn(now);

        // 更新座位状态
        Seat seat = r.getSeat();
        seat.setStatus(Seat.Status.OCCUPIED);
        seatRepository.save(seat);

        reservationRepository.save(r);

        return Result.success(Map.of(
                "message", "签到成功",
                "startTime", r.getStartTime().toString(),
                "endTime", r.getEndTime().toString()
        ));
    }

    /**
     * 退座
     */
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

        // 计算学习时长
        int minutes = (int) ChronoUnit.MINUTES.between(r.getActualCheckIn(), now);
        r.setStudyMinutes(minutes);
        r.setStatus(Reservation.Status.COMPLETED);

        // 释放座位
        Seat seat = r.getSeat();
        seat.setStatus(Seat.Status.AVAILABLE);
        seatRepository.save(seat);

        // 更新用户数据
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

    /**
     * 取消预约
     */
    @PostMapping("/cancel/{id}")
    @Transactional
    public Result<?> cancelReservation(@PathVariable Long id) {
        Reservation r = reservationRepository.findById(id).orElse(null);
        if (r == null) return Result.error("预约不存在");

        if (r.getStatus() != Reservation.Status.PENDING) {
            return Result.error("只能取消待签到的预约");
        }

        // 开始前30分钟内不能取消
        LocalDateTime reservationStart = LocalDateTime.of(r.getReservationDate(), r.getStartTime());
        if (LocalDateTime.now().isAfter(reservationStart.minusMinutes(30))) {
            return Result.error("开始前30分钟内不能取消");
        }

        r.setStatus(Reservation.Status.CANCELLED);

        // 释放座位
        Seat seat = r.getSeat();
        seat.setStatus(Seat.Status.AVAILABLE);
        seatRepository.save(seat);

        reservationRepository.save(r);

        return Result.success("取消成功");
    }

    /**
     * 获取当前预约
     */
    @GetMapping("/current")
    public Result<?> getCurrent(@RequestParam String studentNo) {
        User user = userRepository.findByStudentNo(studentNo).orElse(null);
        if (user == null) return Result.error("用户不存在");

        return reservationRepository.findCurrentReservationByUser(user)
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("reservationId", r.getId());
                    map.put("seatNo", r.getSeat().getSeatNo());
                    map.put("date", r.getReservationDate().toString());
                    map.put("startTime", r.getStartTime().toString());
                    map.put("endTime", r.getEndTime().toString());
                    map.put("status", r.getStatus().name());
                    map.put("statusDisplay", r.getStatus().getDisplayName());
                    map.put("floor", r.getSeat().getFloor());
                    map.put("area", r.getSeat().getArea());
                    return Result.success(map);
                })
                .orElse(Result.success(null));
    }

    /**
     * 获取我的预约列表
     */
    @GetMapping("/my-reservations")
    public Result<?> getMyReservations(@RequestParam String studentNo) {
        User user = userRepository.findByStudentNo(studentNo).orElse(null);
        if (user == null) return Result.error("用户不存在");

        List<Reservation> reservations = reservationRepository.findByUserOrderByCreateTimeDesc(user);

        List<Map<String, Object>> list = reservations.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("seatNo", r.getSeat().getSeatNo());
            map.put("floor", r.getSeat().getFloor());
            map.put("area", r.getSeat().getArea());
            map.put("date", r.getReservationDate().toString());
            map.put("startTime", r.getStartTime().toString());
            map.put("endTime", r.getEndTime().toString());
            map.put("status", r.getStatus().name());
            map.put("statusDisplay", r.getStatus().getDisplayName());
            map.put("studyMinutes", r.getStudyMinutes());
            map.put("createTime", r.getCreateTime().toString());
            return map;
        }).collect(Collectors.toList());

        return Result.success(list);
    }
}