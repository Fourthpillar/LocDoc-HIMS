package com.lockdoc.app.exception;

import com.lockdoc.app.config.JwtUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A URL nobody implemented must look like a client mistake (404/405), not a
 * server fault (500). Before {@code GlobalExceptionHandler} grew explicit
 * handlers for them, Spring's {@code NoResourceFoundException} and
 * {@code HttpRequestMethodNotSupportedException} were swallowed by the
 * catch-all {@code @ExceptionHandler(Exception.class)} and every typo'd or
 * stale path answered 500 "An unexpected error occurred".
 *
 * Requests carry a real JWT because {@code JwtAuthenticationFilter} short-
 * circuits with 401 before the dispatcher is ever reached otherwise, which
 * would make this test pass for the wrong reason.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UnknownEndpointErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String bearer;

    @BeforeEach
    void issueToken() {
        // FP_USER is the super-admin seeded by Flyway V2, so it exists in the
        // test database too and passes the UserDetailsService lookup.
        bearer = "Bearer " + jwtUtil.generateToken("FP_USER", List.of("SUPER_ADMIN"), List.of(), null);
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/op/no-such-thing").header("Authorization", bearer))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void unknownSubPathOfAKnownControllerReturns404() throws Exception {
        mockMvc.perform(patch("/op/doctor-schedules/161/nonsense").header("Authorization", bearer))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void wrongHttpMethodOnAKnownPathReturns405WithAllowHeader() throws Exception {
        mockMvc.perform(delete("/users/me").header("Authorization", bearer))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", Matchers.containsString("GET")))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
