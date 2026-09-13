package com.lockdoc.app.security;

import com.lockdoc.common.config.AppUserPrincipal;
import com.lockdoc.common.config.JwtUtil;
import com.lockdoc.common.repository.FacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Super Admin (FP_USER) works inside facility and doctor modules by sending the
 * acting-context headers the UI's module switcher sets - see AppUserPrincipal.
 * Everyone else's acting headers must be ignored.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SuperAdminActingContextTest {

    private static final String FACILITY_SCOPED = "/op/approval-policies";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FacilityRepository facilityRepository;

    private String superAdmin;
    private String hospitalAdmin;
    private Long bootstrapFacilityId;

    @BeforeEach
    void setUp() {
        superAdmin = "Bearer " + jwtUtil.generateToken("FP_USER", List.of("SUPER_ADMIN"), List.of(), null);
        bootstrapFacilityId = facilityRepository.findByNameIgnoreCase("Default Facility (bootstrap)").orElseThrow().getId();
        hospitalAdmin = "Bearer " + jwtUtil.generateToken("BOOTSTRAP_ADMIN", List.of("HOSPITAL_ADMIN"), List.of(), bootstrapFacilityId);
    }

    @Test
    void superAdminWithoutFacilityIsRejectedFromFacilityScopedEndpoint() throws Exception {
        mockMvc.perform(get(FACILITY_SCOPED).header("Authorization", superAdmin))
                .andExpect(status().isForbidden());
    }

    @Test
    void superAdminActingAtFacilityCanReadAndCreateData() throws Exception {
        mockMvc.perform(get(FACILITY_SCOPED)
                        .header("Authorization", superAdmin)
                        .header(AppUserPrincipal.ACTING_FACILITY_HEADER, bootstrapFacilityId))
                .andExpect(status().isOk());

        mockMvc.perform(put(FACILITY_SCOPED)
                        .header("Authorization", superAdmin)
                        .header(AppUserPrincipal.ACTING_FACILITY_HEADER, bootstrapFacilityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approvalType\":\"REFUND\",\"thresholdValue\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thresholdValue").value(500));
    }

    @Test
    void unknownActingFacilityIsABadRequest() throws Exception {
        mockMvc.perform(get(FACILITY_SCOPED)
                        .header("Authorization", superAdmin)
                        .header(AppUserPrincipal.ACTING_FACILITY_HEADER, 987654321L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonSuperAdminActingHeadersAreIgnored() throws Exception {
        // Would be a 400 if the header were honoured; instead the admin stays at their own facility.
        mockMvc.perform(get(FACILITY_SCOPED)
                        .header("Authorization", hospitalAdmin)
                        .header(AppUserPrincipal.ACTING_FACILITY_HEADER, 987654321L))
                .andExpect(status().isOk());
    }

    @Test
    void superAdminActingAsDoctorUsesThatDoctorsProfile() throws Exception {
        mockMvc.perform(get("/doctor/profile").header("Authorization", superAdmin))
                .andExpect(status().isForbidden());

        String username = "acting_doc_" + System.nanoTime();
        String body = mockMvc.perform(post("/platform/doctors/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"Passw0rd!Long\","
                                + "\"fullName\":\"Acting Doctor\",\"registrationNumber\":\"REG-" + username + "\"}"))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        long doctorId = Long.parseLong(body.replaceAll("^.*?\"id\"\\s*:\\s*(\\d+).*", "$1"));

        mockMvc.perform(get("/doctor/profile")
                        .header("Authorization", superAdmin)
                        .header(AppUserPrincipal.ACTING_DOCTOR_HEADER, doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Acting Doctor"));
    }
}
