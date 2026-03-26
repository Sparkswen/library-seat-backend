package com.jlau.libraryseat.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_seat")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private String area;

    @Column(nullable = false, unique = true)
    private String seatNo;

    @Enumerated(EnumType.STRING)
    private Status status = Status.AVAILABLE;

    public enum Status {
        AVAILABLE("空闲"), RESERVED("已预约"),
        OCCUPIED("使用中"), MAINTENANCE("维护中");

        private final String displayName;
        Status(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
