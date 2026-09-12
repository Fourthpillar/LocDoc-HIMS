package com.lockdoc.app.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Soft-stop (Master Spec §11.5) - carries a "warnings" array on top of
    // the usual body so the frontend can tell this apart from a hard
    // failure and offer "confirm and proceed" rather than just an error.
    @ExceptionHandler(SafetyCheckException.class)
    public ResponseEntity<Map<String, Object>> handleSafetyCheck(SafetyCheckException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Safety Check");
        body.put("message", ex.getMessage());
        body.put("warnings", ex.getWarnings());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidDocumentStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDocumentState(InvalidDocumentStateException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(DuplicateResourceException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Validation failed");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Pre-existing gap, found while curl-testing OP Reports (§17.7 #12a) -
    // any endpoint's required @RequestParam being omitted or the wrong
    // type (e.g. an unparseable date) fell through to the generic 500
    // handler below with no useful message. Affects every controller in
    // the app, not just the new report endpoints.
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(org.springframework.web.bind.MissingServletRequestParameterException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getParameterName() + " is required");
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getName() + " is not a valid value");
    }

    // Thrown by SecurityUtils.requireFacilityId() when a facility-scoped
    // endpoint is called by a principal with none (i.e. Super Admin, who
    // acts through platform-level endpoints instead - Master Spec §5.1).
    // This is an expected, by-design rejection, not a server fault, so it
    // gets its own clean 403 rather than falling through to the generic
    // 500 handler below.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // Thrown by application code (e.g. DoctorFacilityMappingService) for an
    // ownership check - a doctor acting on another doctor's mapping/status/
    // rate, not a rights problem @PreAuthorize would already have caught.
    // Spring Security's own AccessDeniedException only auto-resolves to 403
    // when thrown from within the filter chain (e.g. @PreAuthorize itself);
    // thrown from a @Service method it would otherwise fall through to the
    // generic 500 handler below without this.
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // Unknown URL -> 404, not 500. Spring Boot 3.2+ maps static resources at
    // "/**", so a request that matches no @RequestMapping is handed to
    // ResourceHttpRequestHandler, which throws NoResourceFoundException
    // rather than the DispatcherServlet's NoHandlerFoundException. Without
    // this handler every typo'd/stale URL fell through to the generic 500
    // below (and logged a full stack trace), which told API consumers the
    // server was broken when the path simply does not exist.
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(
            org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        log.debug("No handler for {} /{}", ex.getHttpMethod(), ex.getResourcePath());
        return buildResponse(HttpStatus.NOT_FOUND, "No endpoint " + ex.getHttpMethod() + " /" + ex.getResourcePath());
    }

    // Belt-and-braces companion to the above: this is what DispatcherServlet
    // throws instead if static-resource mappings are ever turned off
    // (spring.web.resources.add-mappings=false). Same 404 either way.
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFound(
            org.springframework.web.servlet.NoHandlerFoundException ex) {
        log.debug("No handler for {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return buildResponse(HttpStatus.NOT_FOUND, "No endpoint " + ex.getHttpMethod() + " " + ex.getRequestURL());
    }

    // Right path, wrong verb -> 405 (was also falling through to the generic
    // 500). Carries the Allow header, which RFC 9110 makes mandatory on a 405.
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        log.debug("Method not supported: {}", ex.getMessage());
        Set<HttpMethod> supported = ex.getSupportedHttpMethods();
        String message = "Method " + ex.getMethod() + " is not supported for this endpoint"
                + (supported == null || supported.isEmpty()
                        ? ""
                        : "; supported: " + supported.stream().map(HttpMethod::name).sorted().collect(Collectors.joining(", ")));

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        body.put("error", HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
        body.put("message", message);

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED);
        if (supported != null && !supported.isEmpty()) {
            builder.allow(supported.toArray(new HttpMethod[0]));
        }
        return builder.body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
