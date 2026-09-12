package com.lockdoc.app.dto.dataprotection;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErasureRequestResolveRequest {

    @NotBlank(message = "status is required")
    private String status;

    private String resolutionNotes;
}
