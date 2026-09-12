package com.lockdoc.app.dto.doctor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Ends one of the doctor's own recurring sessions (Master Spec §8.2). Used to
 * preview which booked appointments fall after {@code lastDate} and - with a
 * decision for each of them in {@code resolutions} - to commit it.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionEndRequest {

    /** The last date the session still runs; every occurrence after it is removed. */
    @NotNull(message = "lastDate is required")
    private LocalDate lastDate;

    /** Commit only - one reschedule-or-cancel decision per affected appointment. Ignored by preview. */
    @Valid
    private List<AppointmentResolutionRequest> resolutions;
}
