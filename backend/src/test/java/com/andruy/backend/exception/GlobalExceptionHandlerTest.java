package com.andruy.backend.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.servlet.http.HttpServletRequest;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Nested
    @DisplayName("handleNotFound")
    class HandleNotFound {

        @Test
        @DisplayName("Should return 404 with correct message")
        void handleNotFound_Returns404WithMessage() {
            NotFoundException ex = new NotFoundException("Resource not found");

            ResponseEntity<ApiError> response = handler.handleNotFound(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().error()).isEqualTo("Not Found");
            assertThat(response.getBody().message()).isEqualTo("Resource not found");
            assertThat(response.getBody().path()).isEqualTo("/api/test");
        }

        @Test
        @DisplayName("Should include timestamp")
        void handleNotFound_IncludesTimestamp() {
            NotFoundException ex = new NotFoundException("Not found");

            ResponseEntity<ApiError> response = handler.handleNotFound(ex, request);

            assertThat(response.getBody().timestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should have null fieldErrors")
        void handleNotFound_HasNullFieldErrors() {
            NotFoundException ex = new NotFoundException("Not found");

            ResponseEntity<ApiError> response = handler.handleNotFound(ex, request);

            assertThat(response.getBody().fieldErrors()).isNull();
        }
    }

    @Nested
    @DisplayName("handleValidation")
    class HandleValidation {

        @Test
        @DisplayName("Should return 400 with validation message")
        void handleValidation_Returns400() {
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                    new FieldError("object", "field1", "must not be null"),
                    new FieldError("object", "field2", "must be positive")
            ));

            ResponseEntity<ApiError> response = handler.handleValidation(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().error()).isEqualTo("Bad Request");
            assertThat(response.getBody().message()).isEqualTo("Validation failed");
        }

        @Test
        @DisplayName("Should include field errors")
        void handleValidation_IncludesFieldErrors() {
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                    new FieldError("object", "username", "must not be blank"),
                    new FieldError("object", "email", "must be valid email")
            ));

            ResponseEntity<ApiError> response = handler.handleValidation(ex, request);

            assertThat(response.getBody().fieldErrors()).isNotNull();
            assertThat(response.getBody().fieldErrors()).hasSize(2);
            assertThat(response.getBody().fieldErrors().get("username")).isEqualTo("must not be blank");
            assertThat(response.getBody().fieldErrors().get("email")).isEqualTo("must be valid email");
        }

        @Test
        @DisplayName("Should handle empty field errors")
        void handleValidation_WithEmptyFieldErrors() {
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of());

            ResponseEntity<ApiError> response = handler.handleValidation(ex, request);

            assertThat(response.getBody().fieldErrors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("handleBadRequest")
    class HandleBadRequest {

        @Test
        @DisplayName("Should return 400 with message")
        void handleBadRequest_Returns400() {
            IllegalArgumentException ex = new IllegalArgumentException("Invalid argument provided");

            ResponseEntity<ApiError> response = handler.handleBadRequest(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().error()).isEqualTo("Bad Request");
            assertThat(response.getBody().message()).isEqualTo("Invalid argument provided");
        }

        @Test
        @DisplayName("Should have null fieldErrors")
        void handleBadRequest_HasNullFieldErrors() {
            IllegalArgumentException ex = new IllegalArgumentException("Bad argument");

            ResponseEntity<ApiError> response = handler.handleBadRequest(ex, request);

            assertThat(response.getBody().fieldErrors()).isNull();
        }
    }

    @Nested
    @DisplayName("handleGeneric")
    class HandleGeneric {

        @Test
        @DisplayName("Should return 500 with generic message")
        void handleGeneric_Returns500() {
            Exception ex = new RuntimeException("Something went wrong");

            ResponseEntity<ApiError> response = handler.handleGeneric(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
            assertThat(response.getBody().message()).isEqualTo("Unexpected error");
        }

        @Test
        @DisplayName("Should not expose original exception message")
        void handleGeneric_HidesExceptionMessage() {
            Exception ex = new RuntimeException("Sensitive database error with credentials");

            ResponseEntity<ApiError> response = handler.handleGeneric(ex, request);

            assertThat(response.getBody().message()).isEqualTo("Unexpected error");
            assertThat(response.getBody().message()).doesNotContain("database");
            assertThat(response.getBody().message()).doesNotContain("credentials");
        }

        @Test
        @DisplayName("Should have null fieldErrors")
        void handleGeneric_HasNullFieldErrors() {
            Exception ex = new RuntimeException("Error");

            ResponseEntity<ApiError> response = handler.handleGeneric(ex, request);

            assertThat(response.getBody().fieldErrors()).isNull();
        }
    }

    @Nested
    @DisplayName("Common behavior")
    class CommonBehavior {

        @Test
        @DisplayName("Should use request URI as path")
        void allHandlers_UseRequestUriAsPath() {
            when(request.getRequestURI()).thenReturn("/api/custom/path");

            NotFoundException ex = new NotFoundException("Not found");
            ResponseEntity<ApiError> response = handler.handleNotFound(ex, request);

            assertThat(response.getBody().path()).isEqualTo("/api/custom/path");
        }

        @Test
        @DisplayName("Should handle special characters in path")
        void allHandlers_HandleSpecialCharsInPath() {
            when(request.getRequestURI()).thenReturn("/api/users/123?filter=active&sort=name");

            NotFoundException ex = new NotFoundException("Not found");
            ResponseEntity<ApiError> response = handler.handleNotFound(ex, request);

            assertThat(response.getBody().path()).isEqualTo("/api/users/123?filter=active&sort=name");
        }
    }
}
