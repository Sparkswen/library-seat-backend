package com.jlau.libraryseat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;
    private Integer studyMinutes = 0;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING("待签到"), CHECKED_IN("学习中"), PAUSED("暂离"),
        COMPLETED("已完成"), CANCELLED("已取消"), NO_SHOW("违约");

        private final String displayName;
        Status(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
