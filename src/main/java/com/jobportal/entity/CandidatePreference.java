package com.jobportal.entity;

import com.jobportal.enums.EmploymentType;
import com.jobportal.enums.RemotePreference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "candidate_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CandidatePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    @ToString.Exclude
    private Candidate candidate;

    @Column(length = 100)
    private String preferredJobTitle;

    @Column(length = 150)
    private String preferredLocation;

    @Column(precision = 12, scale = 2)
    private BigDecimal minimumSalary;

    @Column(precision = 12, scale = 2)
    private BigDecimal maximumSalary;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EmploymentType preferredEmploymentType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RemotePreference remotePreference;
}
