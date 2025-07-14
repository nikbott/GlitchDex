package br.ufscar.glitchdex.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private String getMessage(String code) {
        // Assume default messages are defined in application.properties or messages.properties
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private Object buildErrorResponse(HttpServletRequest request, HttpStatus status, String message) {
        if (isApiRequest(request)) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", status.value());
            body.put("error", status.getReasonPhrase());
            body.put("message", message);
            body.put("path", request.getRequestURI());
            return new ResponseEntity<>(body, status);
        } else {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("errorMessage", message);
            return mav;
        }
    }

    // --- New handler for validation errors (@Valid) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation error (MethodArgumentNotValidException) for request {}: {}", request.getRequestURI(), ex.getMessage());

        // Mapeia erros de campo para uma lista mais legível
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Constrói a resposta JSON para APIs
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", "Validation failed for request body.");
        body.put("details", errors);
        body.put("path", request.getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // --- New handler for missing/malformed request body (JSON parsing errors) ---
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Request body not readable for request {}: {}", request.getRequestURI(), ex.getMessage());
        String message;

        // Verifica o tipo de erro para fornecer uma mensagem mais precisa
        if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
            message = getMessage("error.request_body_missing");
        } else {
            // Caso seja um JSON malformado
            message = getMessage("error.invalid_json_format");
        }

        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, message);
    }

    // --- Existing handlers ---

    @ExceptionHandler({ResourceNotFoundException.class})
    public Object handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found for request {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(request, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({EmailAlreadyExistsException.class, IllegalStatusChangeException.class, DataIntegrityViolationException.class, IllegalStateException.class})
    public Object handleConflictExceptions(RuntimeException ex, HttpServletRequest request) {
        String message;
        if (ex instanceof DataIntegrityViolationException) {
            message = getMessage("error.data_integrity_violation");
            log.error("Data integrity violation for request {}: {}", request.getRequestURI(), ex.getMessage());
        } else {
            message = ex.getMessage();
            log.warn("Conflict/illegal state for request {}: {}", request.getRequestURI(), message);
        }
        return buildErrorResponse(request, HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler({PasswordValidationException.class, ValidationException.class})
    public Object handleValidationExceptions(RuntimeException ex, HttpServletRequest request) {
        log.warn("Validation error for request {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for request {}: {}", request.getRequestURI(), ex.getMessage());
        String message = getMessage("error.project.access_denied");
        return buildErrorResponse(request, HttpStatus.FORBIDDEN, message);
    }

    @ExceptionHandler(Exception.class)
    public Object handleAllExceptions(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error for request {}", request.getRequestURI(), ex);
        String message = getMessage("error.unexpected");
        return buildErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}