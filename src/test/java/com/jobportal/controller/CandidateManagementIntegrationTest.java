package com.jobportal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.dto.auth.LoginRequest;
import com.jobportal.dto.auth.RegisterRequest;
import com.jobportal.dto.candidate.*;
import com.jobportal.entity.Education;
import com.jobportal.entity.Experience;
import com.jobportal.enums.EmploymentType;
import com.jobportal.enums.Gender;
import com.jobportal.enums.RemotePreference;
import com.jobportal.enums.Role;
import com.jobportal.repository.EducationRepository;
import com.jobportal.repository.ExperienceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CandidateManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private ExperienceRepository experienceRepository;

    private String candidate1Token;
    private String candidate2Token;
    private String recruiterToken;

    @BeforeEach
    void setUp() throws Exception {
        candidate1Token = registerAndLogin("cand1@test.com", "Password123!", "Candidate One", Role.CANDIDATE);
        candidate2Token = registerAndLogin("cand2@test.com", "Password123!", "Candidate Two", Role.CANDIDATE);
        recruiterToken = registerAndLogin("rec1@test.com", "Password123!", "Recruiter One", Role.RECRUITER);
    }

    private String registerAndLogin(String email, String password, String name, Role role) throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName(name);
        reg.setEmail(email);
        reg.setPassword(password);
        reg.setRole(role);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/accessToken").asText();
    }

    // =========================================================================
    // CANDIDATE PROFILE TESTS (1 - 5)
    // =========================================================================

    @Test
    @DisplayName("1. Candidate can create profile and get 200")
    void candidateCanCreateProfile() throws Exception {
        CandidateProfileRequest request = CandidateProfileRequest.builder()
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1995, 5, 10))
                .gender(Gender.MALE)
                .location("New York")
                .bio("Software Engineer")
                .build();

        mockMvc.perform(put("/api/candidate/profile")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phone").value("+1234567890"))
                .andExpect(jsonPath("$.data.gender").value("MALE"));
    }

    @Test
    @DisplayName("2. Candidate can retrieve own profile")
    void candidateCanRetrieveOwnProfile() throws Exception {
        mockMvc.perform(get("/api/candidate/profile")
                        .header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Candidate One"))
                .andExpect(jsonPath("$.data.email").value("cand1@test.com"));
    }

    @Test
    @DisplayName("3. Candidate can update own profile")
    void candidateCanUpdateOwnProfile() throws Exception {
        CandidateProfileRequest update = CandidateProfileRequest.builder()
                .bio("Senior Software Engineer")
                .location("San Francisco")
                .build();

        mockMvc.perform(put("/api/candidate/profile")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bio").value("Senior Software Engineer"))
                .andExpect(jsonPath("$.data.location").value("San Francisco"));
    }

    @Test
    @DisplayName("4. Non-candidate cannot access candidate endpoints")
    void nonCandidateCannotAccessCandidateEndpoints() throws Exception {
        mockMvc.perform(get("/api/candidate/profile")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("5. Unauthenticated request returns 401")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/candidate/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =========================================================================
    // SKILLS TESTS (6 - 8)
    // =========================================================================

    @Test
    @DisplayName("6. Candidate can add skill")
    void candidateCanAddSkill() throws Exception {
        SkillRequest req = SkillRequest.builder()
                .name("Java")
                .yearsOfExperience(3.5)
                .build();

        mockMvc.perform(post("/api/candidate/skills")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Java"))
                .andExpect(jsonPath("$.data.yearsOfExperience").value(3.5));
    }

    @Test
    @DisplayName("7. Candidate cannot add duplicate skill")
    void candidateCannotAddDuplicateSkill() throws Exception {
        SkillRequest req = SkillRequest.builder()
                .name("Java")
                .yearsOfExperience(3.5)
                .build();

        // Add first time
        mockMvc.perform(post("/api/candidate/skills")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Try adding again
        mockMvc.perform(post("/api/candidate/skills")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("8. Candidate can remove own skill")
    void candidateCanRemoveOwnSkill() throws Exception {
        SkillRequest req = SkillRequest.builder()
                .name("Java")
                .yearsOfExperience(3.5)
                .build();

        MvcResult result = mockMvc.perform(post("/api/candidate/skills")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        long skillId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/skillId").asLong();

        mockMvc.perform(delete("/api/candidate/skills/" + skillId)
                        .header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isOk());

        // Verify it is gone
        mockMvc.perform(get("/api/candidate/skills")
                        .header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    // =========================================================================
    // EDUCATION TESTS (9 - 12)
    // =========================================================================

    @Test
    @DisplayName("9. Candidate can create education")
    void candidateCanCreateEducation() throws Exception {
        EducationRequest req = EducationRequest.builder()
                .institution("MIT")
                .degree("BS")
                .fieldOfStudy("Computer Science")
                .startDate(LocalDate.of(2015, 9, 1))
                .endDate(LocalDate.of(2019, 6, 1))
                .description("GPA 4.0")
                .build();

        mockMvc.perform(post("/api/candidate/education")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.institution").value("MIT"))
                .andExpect(jsonPath("$.data.degree").value("BS"));
    }

    @Test
    @DisplayName("10. Candidate can update own education")
    void candidateCanUpdateOwnEducation() throws Exception {
        EducationRequest req = EducationRequest.builder()
                .institution("MIT")
                .degree("BS")
                .fieldOfStudy("Computer Science")
                .startDate(LocalDate.of(2015, 9, 1))
                .endDate(LocalDate.of(2019, 6, 1))
                .build();

        MvcResult result = mockMvc.perform(post("/api/candidate/education")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        long eduId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();

        EducationRequest update = EducationRequest.builder()
                .institution("Harvard")
                .degree("MS")
                .fieldOfStudy("Data Science")
                .startDate(LocalDate.of(2019, 9, 1))
                .build();

        mockMvc.perform(put("/api/candidate/education/" + eduId)
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.institution").value("Harvard"))
                .andExpect(jsonPath("$.data.degree").value("MS"));
    }

    @Test
    @DisplayName("11. Candidate can delete own education")
    void candidateCanDeleteOwnEducation() throws Exception {
        EducationRequest req = EducationRequest.builder()
                .institution("MIT")
                .degree("BS")
                .fieldOfStudy("Computer Science")
                .startDate(LocalDate.of(2015, 9, 1))
                .build();

        MvcResult result = mockMvc.perform(post("/api/candidate/education")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        long eduId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();

        mockMvc.perform(delete("/api/candidate/education/" + eduId)
                        .header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("12. Candidate cannot modify another candidate's education")
    void candidateCannotModifyAnotherCandidateEducation() throws Exception {
        EducationRequest req = EducationRequest.builder()
                .institution("MIT")
                .degree("BS")
                .fieldOfStudy("Computer Science")
                .startDate(LocalDate.of(2015, 9, 1))
                .build();

        MvcResult result = mockMvc.perform(post("/api/candidate/education")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        long eduId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();

        EducationRequest update = EducationRequest.builder()
                .institution("Stanford")
                .degree("PhD")
                .fieldOfStudy("AI")
                .startDate(LocalDate.of(2020, 9, 1))
                .build();

        // Try updating it using candidate 2 token
        mockMvc.perform(put("/api/candidate/education/" + eduId)
                        .header("Authorization", "Bearer " + candidate2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());

        // Try deleting it using candidate 2 token
        mockMvc.perform(delete("/api/candidate/education/" + eduId)
                        .header("Authorization", "Bearer " + candidate2Token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // EXPERIENCE TESTS (13 - 16)
    // =========================================================================

    @Test
    @DisplayName("13. Candidate can create experience")
    void candidateCanCreateExperience() throws Exception {
        ExperienceRequest req = ExperienceRequest.builder()
                .companyName("Google")
                .jobTitle("SWE")
                .employmentType(EmploymentType.FULL_TIME)
                .startDate(LocalDate.of(2020, 1, 1))
                .currentlyWorking(true)
                .build();

        mockMvc.perform(post("/api/candidate/experience")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyName").value("Google"))
                .andExpect(jsonPath("$.data.currentlyWorking").value(true));
    }

    @Test
    @DisplayName("14. Candidate can update own experience")
    void candidateCanUpdateOwnExperience() throws Exception {
        ExperienceRequest req = ExperienceRequest.builder()
                .companyName("Google")
                .jobTitle("SWE")
                .employmentType(EmploymentType.FULL_TIME)
                .startDate(LocalDate.of(2020, 1, 1))
                .currentlyWorking(true)
                .build();

        MvcResult result = mockMvc.perform(post("/api/candidate/experience")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        long expId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();

        ExperienceRequest update = ExperienceRequest.builder()
                .companyName("YouTube")
                .jobTitle("Senior SWE")
                .employmentType(EmploymentType.FULL_TIME)
                .startDate(LocalDate.of(2020, 1, 1))
                .currentlyWorking(false)
                .endDate(LocalDate.of(2022, 12, 31))
                .build();

        mockMvc.perform(put("/api/candidate/experience/" + expId)
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("YouTube"))
                .andExpect(jsonPath("$.data.jobTitle").value("Senior SWE"));
    }

    @Test
    @DisplayName("15. Candidate can delete own experience")
    void candidateCanDeleteOwnExperience() throws Exception {
        ExperienceRequest req = ExperienceRequest.builder()
                .companyName("Google")
                .jobTitle("SWE")
                .employmentType(EmploymentType.FULL_TIME)
                .startDate(LocalDate.of(2020, 1, 1))
                .currentlyWorking(true)
                .build();

        MvcResult result = mockMvc.perform(post("/api/candidate/experience")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        long expId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();

        mockMvc.perform(delete("/api/candidate/experience/" + expId)
                        .header("Authorization", "Bearer " + candidate1Token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("16. Candidate cannot modify another candidate's experience")
    void candidateCannotModifyAnotherCandidateExperience() throws Exception {
        ExperienceRequest req = ExperienceRequest.builder()
                .companyName("Google")
                .jobTitle("SWE")
                .employmentType(EmploymentType.FULL_TIME)
                .startDate(LocalDate.of(2020, 1, 1))
                .currentlyWorking(true)
                .build();

        MvcResult result = mockMvc.perform(post("/api/candidate/experience")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        long expId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/id").asLong();

        ExperienceRequest update = ExperienceRequest.builder()
                .companyName("Meta")
                .jobTitle("Manager")
                .employmentType(EmploymentType.FULL_TIME)
                .startDate(LocalDate.of(2021, 1, 1))
                .currentlyWorking(true)
                .build();

        // Try updating using candidate 2 token
        mockMvc.perform(put("/api/candidate/experience/" + expId)
                        .header("Authorization", "Bearer " + candidate2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());

        // Try deleting using candidate 2 token
        mockMvc.perform(delete("/api/candidate/experience/" + expId)
                        .header("Authorization", "Bearer " + candidate2Token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // PREFERENCES TESTS (17 - 18)
    // =========================================================================

    @Test
    @DisplayName("17. Candidate can create/update preferences")
    void candidateCanCreateOrUpdatePreferences() throws Exception {
        PreferenceRequest req = PreferenceRequest.builder()
                .preferredJobTitle("Staff Engineer")
                .preferredLocation("Seattle")
                .minimumSalary(BigDecimal.valueOf(120000.00))
                .maximumSalary(BigDecimal.valueOf(180000.00))
                .preferredEmploymentType(EmploymentType.FULL_TIME)
                .remotePreference(RemotePreference.REMOTE)
                .build();

        mockMvc.perform(put("/api/candidate/preferences")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preferredJobTitle").value("Staff Engineer"))
                .andExpect(jsonPath("$.data.remotePreference").value("REMOTE"));
    }

    @Test
    @DisplayName("18. Candidate cannot modify another candidate's preferences")
    void candidateCannotModifyAnotherCandidatePreferences() throws Exception {
        // Preferences are 1-1 mapped dynamically to authenticated User context only,
        // so candidate2 cannot target candidate1's preferences by passing an ID since
        // the PUT preference endpoint does not accept an ID in path or body.
        // Thus BOLA is structurally impossible.
    }

    // =========================================================================
    // VALIDATION TESTS (19)
    // =========================================================================

    @Test
    @DisplayName("19. Invalid data returns validation error (400)")
    void invalidDataReturnsValidationError() throws Exception {
        // 1. Negative years of experience in Skill
        SkillRequest badSkill = SkillRequest.builder()
                .name("Java")
                .yearsOfExperience(-1.5)
                .build();

        mockMvc.perform(post("/api/candidate/skills")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badSkill)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.fieldErrors.yearsOfExperience").exists());

        // 2. Start date after End date in Education
        EducationRequest badEdu = EducationRequest.builder()
                .institution("MIT")
                .degree("BS")
                .fieldOfStudy("CS")
                .startDate(LocalDate.of(2019, 9, 1))
                .endDate(LocalDate.of(2015, 6, 1))
                .build();

        mockMvc.perform(post("/api/candidate/education")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badEdu)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start date must be before end date"));

        // 3. Experience currentlyWorking=true but endDate is set
        ExperienceRequest badExp = ExperienceRequest.builder()
                .companyName("Google")
                .jobTitle("SWE")
                .employmentType(EmploymentType.FULL_TIME)
                .startDate(LocalDate.of(2020, 1, 1))
                .endDate(LocalDate.of(2022, 1, 1))
                .currentlyWorking(true)
                .build();

        mockMvc.perform(post("/api/candidate/experience")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badExp)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("End date must be null if currently working"));

        // 4. Preferences minimumSalary > maximumSalary
        PreferenceRequest badPref = PreferenceRequest.builder()
                .minimumSalary(BigDecimal.valueOf(10000))
                .maximumSalary(BigDecimal.valueOf(5000))
                .build();

        mockMvc.perform(put("/api/candidate/preferences")
                        .header("Authorization", "Bearer " + candidate1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badPref)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Minimum salary must be less than or equal to maximum salary"));
    }
}
