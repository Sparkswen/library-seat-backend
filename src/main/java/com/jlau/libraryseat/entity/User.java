package com.jlau.libraryseat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String studentNo;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;

    private Integer creditScore = 100;
    private Integer totalStudyMinutes = 0;

    @Enumerated(EnumType.STRING)
    private LevelTitle levelTitle = LevelTitle.XUETONG;

    private Integer points = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public enum Role { STUDENT, LIBRARIAN, ADMIN }

    public enum LevelTitle {
        XUETONG("学童"), XUEJIANG("学匠"), XUESHI("学师"),
        XUEZONG("学宗"), XUESHENG("学圣");

        private final String displayName;
        LevelTitle(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }

        public static LevelTitle calculateByMinutes(int minutes) {
            int hours = minutes / 60;
            if (hours >= 200) return XUESHENG;
            if (hours >= 100) return XUEZONG;
            if (hours >= 50) return XUESHI;
            if (hours >= 10) return XUEJIANG;
            return XUETONG;
        }
    }
}
