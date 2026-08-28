package com.jobportal.service;

import com.jobportal.dto.candidate.CandidateProfileRequest;
import com.jobportal.dto.candidate.CandidateProfileResponse;
import com.jobportal.entity.Candidate;
import com.jobportal.entity.User;
import com.jobportal.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;

    @Transactional
    public Candidate getOrCreateCandidate(User user) {
        return candidateRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.info("Auto-creating Candidate record for User ID: {}", user.getId());
                    Candidate candidate = Candidate.builder()
                            .user(user)
                            .build();
                    return candidateRepository.save(candidate);
                });
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse getProfile(User user) {
        Candidate candidate = getOrCreateCandidate(user);
        return toProfileResponse(candidate);
    }

    @Transactional
    public CandidateProfileResponse updateProfile(User user, CandidateProfileRequest request) {
        Candidate candidate = getOrCreateCandidate(user);

        candidate.setPhone(request.getPhone());
        candidate.setDateOfBirth(request.getDateOfBirth());
        candidate.setGender(request.getGender());
        candidate.setLocation(request.getLocation());
        candidate.setBio(request.getBio());
        candidate.setProfileImageUrl(request.getProfileImageUrl());
        candidate.setLinkedinUrl(request.getLinkedinUrl());
        candidate.setGithubUrl(request.getGithubUrl());
        candidate.setPortfolioUrl(request.getPortfolioUrl());

        Candidate saved = candidateRepository.save(candidate);
        log.info("Updated Candidate profile for User ID: {}", user.getId());

        return toProfileResponse(saved);
    }

    private CandidateProfileResponse toProfileResponse(Candidate candidate) {
        User user = candidate.getUser();
        return CandidateProfileResponse.builder()
                .id(candidate.getId())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(candidate.getPhone())
                .dateOfBirth(candidate.getDateOfBirth())
                .gender(candidate.getGender())
                .location(candidate.getLocation())
                .bio(candidate.getBio())
                .profileImageUrl(candidate.getProfileImageUrl())
                .linkedinUrl(candidate.getLinkedinUrl())
                .githubUrl(candidate.getGithubUrl())
                .portfolioUrl(candidate.getPortfolioUrl())
                .createdAt(candidate.getCreatedAt())
                .updatedAt(candidate.getUpdatedAt())
                .build();
    }
}
