package com.jlau.libraryseat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "tb_reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    // 预约日期
    private LocalDate reservationDate;

    // 开始时间
    private LocalTime startTime;

    // 结束时间
    private LocalTime endTime;

    // 实际签到时间
    private LocalDateTime actualCheckIn;

    // 实际签退时间
    private LocalDateTime actualCheckOut;

    // 学习时长（分钟）
    private Integer studyMinutes = 0;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    // 创建时间
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    public enum Status {
        PENDING("待签到"),
        CHECKED_IN("学习中"),
        PAUSED("暂离"),
        COMPLETED("已完成"),
        CANCELLED("已取消"),
        NO_SHOW("违约");

        private final String displayName;
        Status(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}