package com.orrs.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rehab_plan_week")
@Getter
@Setter
public class RehabPlanWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many weeks could exist (versions). For MVP we store latest.
    @ManyToOne(optional = false)
    @JoinColumn(name = "rehab_plan_id")
    private RehabPlan rehabPlan;

    private Integer schemaVersion = 1;

    private LocalDate weekStart;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String weekJson;

    private LocalDate createdAt = LocalDate.now();
}
