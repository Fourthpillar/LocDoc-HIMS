package com.lockdoc.outpatient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationsReportResponse {
    private List<CancellationResponse> rows;
    private int totalCount;
    private int approvedCount;
    private int rejectedCount;
    private int pendingCount;
}
