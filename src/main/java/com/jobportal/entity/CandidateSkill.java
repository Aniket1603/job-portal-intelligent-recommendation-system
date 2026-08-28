package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "candidate_skills",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_candidate_skills_candidate_skill",
                columnNames = {"candidate_id", "skill_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CandidateSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    @ToString.Exclude
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private Double yearsOfExperience;
}
