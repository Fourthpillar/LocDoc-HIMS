package com.lockdoc.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Lines are replaced wholesale on every save (see PrescriptionService) - simplest correct model for a form saved as a whole. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {

    private List<PrescriptionLineRequest> lines = new ArrayList<>();
}
