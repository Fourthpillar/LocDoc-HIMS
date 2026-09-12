package com.lockdoc.app.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** What ending a recurring session actually did - trimmed to a last date, or removed outright because it had never run. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEndResponse {

    private Long doctorScheduleId;
    private String sessionName;
    /** True when the session hadn't started yet, so there was no history to keep and it was removed entirely. */
    private boolean removed;
    /** The session's new last date; null when {@link #removed}. */
    private LocalDate lastDate;
}
