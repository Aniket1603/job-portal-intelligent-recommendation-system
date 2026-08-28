package com.jobportal.service;

import com.jobportal.dto.candidate.CandidateSkillResponse;
import com.jobportal.dto.candidate.SkillRequest;
import com.jobportal.entity.Candidate;
import com.jobportal.entity.CandidateSkill;
import com.jobportal.entity.Skill;
import com.jobportal.entity.User;
import com.jobportal.exception.DuplicateResourceException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.CandidateSkillRepository;
import com.jobportal.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {

    private final CandidateService candidateService;
    private final SkillRepository skillRepository;
    private final CandidateSkillRepository candidateSkillRepository;

    @Transactional(readOnly = true)
    public List<CandidateSkillResponse> getSkills(User user) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);
        return candidateSkillRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CandidateSkillResponse addSkill(User user, SkillRequest request) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);

        String normalizedName = request.getName().trim();

        // Find or create the master Skill entity case-insensitively
        Skill skill = skillRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> skillRepository.save(
                        Skill.builder().name(normalizedName).build()
                ));

        // Check if candidate already has this skill
        if (candidateSkillRepository.existsByCandidateIdAndSkillId(candidate.getId(), skill.getId())) {
            throw new DuplicateResourceException("Skill", "name", normalizedName);
        }

        CandidateSkill candidateSkill = CandidateSkill.builder()
                .candidate(candidate)
                .skill(skill)
                .yearsOfExperience(request.getYearsOfExperience())
                .build();

        CandidateSkill saved = candidateSkillRepository.save(candidateSkill);
        log.info("Added skill [{}] to Candidate ID: {}", normalizedName, candidate.getId());

        return toResponse(saved);
    }

    @Transactional
    public void removeSkill(User user, Long skillId) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);

        CandidateSkill candidateSkill = candidateSkillRepository.findByCandidateIdAndSkillId(candidate.getId(), skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill assignment not found"));

        candidateSkillRepository.delete(candidateSkill);
        log.info("Removed skill ID: {} from Candidate ID: {}", skillId, candidate.getId());
    }

    private CandidateSkillResponse toResponse(CandidateSkill cs) {
        return CandidateSkillResponse.builder()
                .id(cs.getId())
                .skillId(cs.getSkill().getId())
                .name(cs.getSkill().getName())
                .yearsOfExperience(cs.getYearsOfExperience())
                .build();
    }
}
