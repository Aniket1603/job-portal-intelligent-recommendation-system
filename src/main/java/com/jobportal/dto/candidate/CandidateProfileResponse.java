package com.jobportal.dto.candidate;

import com.jobportal.enums.Gender;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfileResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String location;
    private String bio;
    private String profileImageUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
