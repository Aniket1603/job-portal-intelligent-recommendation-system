package com.jobportal.dto.candidate;

import com.jobportal.enums.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfileRequest {

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Pattern(regexp = "^$|^[+]?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    @Size(max = 255, message = "Profile image URL cannot exceed 255 characters")
    @URL(message = "Profile image URL must be a valid URL")
    private String profileImageUrl;

    @Size(max = 255, message = "LinkedIn URL cannot exceed 255 characters")
    @URL(message = "LinkedIn URL must be a valid URL")
    private String linkedinUrl;

    @Size(max = 255, message = "GitHub URL cannot exceed 255 characters")
    @URL(message = "GitHub URL must be a valid URL")
    private String githubUrl;

    @Size(max = 255, message = "Portfolio URL cannot exceed 255 characters")
    @URL(message = "Portfolio URL must be a valid URL")
    private String portfolioUrl;
}
