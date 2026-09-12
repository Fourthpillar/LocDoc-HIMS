package com.lockdoc.app.dto.pharmacy;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Shared request shape for both approve (approvedQty per line) and issue (issueQty per line). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndentLineQtyListRequest {

    @NotEmpty(message = "At least one line is required")
    @Valid
    private List<IndentLineQtyRequest> lines;
}
