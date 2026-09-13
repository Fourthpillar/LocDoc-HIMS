package com.lockdoc.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Module entitlement management (Master Spec §10): the whole set of
 * active modules for a facility, replaced wholesale rather than
 * toggled one at a time - the caller always states the complete set it
 * wants active, so there's never an ambiguous "add or remove?" on a
 * single-module PATCH.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityModulesRequest {

    @NotNull(message = "activeModules is required")
    private Set<String> activeModules;
}
