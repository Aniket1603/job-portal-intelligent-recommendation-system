package com.jobportal.repository;

import com.jobportal.entity.CandidatePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidatePreferenceRepository extends JpaRepository<CandidatePreference, Long> {
    Optional<CandidatePreference> findByCandidateId(Long candidateId);
}
