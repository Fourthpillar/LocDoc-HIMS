package com.lockdoc.app.dto.op;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlcRegisterRow {
    private String opNo;
    private String patientName;
    private String patientMrn;
    private String doctorName;
    private String mlcPoliceStation;
    private String mlcNumber;
    private LocalDateTime arrivedTs;
}
