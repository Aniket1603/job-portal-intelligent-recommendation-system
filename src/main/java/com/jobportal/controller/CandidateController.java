package com.jobportal.controller;

import com.jobportal.dto.ApiResponse;
import com.jobportal.dto.candidate.*;
import com.jobportal.security.CustomUserPrincipal;
import com.jobportal.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;
    private final SkillService skillService;
    private final EducationService educationService;
    private final ExperienceService experienceService;
    private final CandidatePreferenceService preferenceService;

    // -------------------------------------------------------------------------
    // Profile Endpoints
    // -------------------------------------------------------------------------

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> getProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        CandidateProfileResponse profile = candidateService.getProfile(principal.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CandidateProfileRequest request) {
        CandidateProfileResponse profile = candidateService.updateProfile(principal.getUser(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", profile));
    }

    // -------------------------------------------------------------------------
    // Skill Endpoints
    // -------------------------------------------------------------------------

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<CandidateSkillResponse>>> getSkills(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        List<CandidateSkillResponse> skills = skillService.getSkills(principal.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Skills retrieved successfully", skills));
    }

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<CandidateSkillResponse>> addSkill(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody SkillRequest request) {
        CandidateSkillResponse skill = skillService.addSkill(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Skill added successfully", skill));
    }

    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<Void>> removeSkill(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long skillId) {
        skillService.removeSkill(principal.getUser(), skillId);
        return ResponseEntity.ok(ApiResponse.ok("Skill removed successfully"));
    }

    // -------------------------------------------------------------------------
    // Education Endpoints
    // -------------------------------------------------------------------------

    @GetMapping("/education")
    public ResponseEntity<ApiResponse<List<EducationResponse>>> getEducation(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        List<EducationResponse> education = educationService.getEducationList(principal.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Education list retrieved successfully", education));
    }

    @PostMapping("/education")
    public ResponseEntity<ApiResponse<EducationResponse>> createEducation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody EducationRequest request) {
        EducationResponse education = educationService.createEducation(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Education record created successfully", education));
    }

    @PutMapping("/education/{educationId}")
    public ResponseEntity<ApiResponse<EducationResponse>> updateEducation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long educationId,
            @Valid @RequestBody EducationRequest request) {
        EducationResponse education = educationService.updateEducation(principal.getUser(), educationId, request);
        return ResponseEntity.ok(ApiResponse.ok("Education record updated successfully", education));
    }

    @DeleteMapping("/education/{educationId}")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long educationId) {
        educationService.deleteEducation(principal.getUser(), educationId);
        return ResponseEntity.ok(ApiResponse.ok("Education record deleted successfully"));
    }

    // -------------------------------------------------------------------------
    // Experience Endpoints
    // -------------------------------------------------------------------------

    @GetMapping("/experience")
    public ResponseEntity<ApiResponse<List<ExperienceResponse>>> getExperience(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        List<ExperienceResponse> experience = experienceService.getExperienceList(principal.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Experience list retrieved successfully", experience));
    }

    @PostMapping("/experience")
    public ResponseEntity<ApiResponse<ExperienceResponse>> createExperience(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ExperienceRequest request) {
        ExperienceResponse experience = experienceService.createExperience(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Experience record created successfully", experience));
    }

    @PutMapping("/experience/{experienceId}")
    public ResponseEntity<ApiResponse<ExperienceResponse>> updateExperience(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long experienceId,
            @Valid @RequestBody ExperienceRequest request) {
        ExperienceResponse experience = experienceService.updateExperience(principal.getUser(), experienceId, request);
        return ResponseEntity.ok(ApiResponse.ok("Experience record updated successfully", experience));
    }

    @DeleteMapping("/experience/{experienceId}")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long experienceId) {
        experienceService.deleteExperience(principal.getUser(), experienceId);
        return ResponseEntity.ok(ApiResponse.ok("Experience record deleted successfully"));
    }

    // -------------------------------------------------------------------------
    // Preference Endpoints
    // -------------------------------------------------------------------------

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<PreferenceResponse>> getPreferences(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        PreferenceResponse preference = preferenceService.getPreferences(principal.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Preferences retrieved successfully", preference));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<PreferenceResponse>> updatePreferences(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody PreferenceRequest request) {
        PreferenceResponse preference = preferenceService.updatePreferences(principal.getUser(), request);
        return ResponseEntity.ok(ApiResponse.ok("Preferences updated successfully", preference));
    }
}
