package com.tower_of_fisa.paydeuk_server_auth.global.config.exception.global;

import com.tower_of_fisa.paydeuk_server_auth.global.common.ErrorDefineCode;
import com.tower_of_fisa.paydeuk_server_auth.global.common.response.CommonError;
import com.tower_of_fisa.paydeuk_server_auth.global.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.LocalDateTime;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j(topic = "EXCEPTION_HANDLER")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  public static final String TRACE = "trace";

  @Value("${error.printStackTrace}")
  private boolean printStackTrace;

  @Value("${error.printStackTraceLine}")
  private int printStackTraceLine;

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      @NonNull Exception ex,
      Object body,
      @NonNull HttpHeaders headers,
      HttpStatusCode statusCode,
      @NonNull WebRequest request) {
    return buildErrorResponse(ex, ErrorDefineCode.UNCAUGHT, HttpStatus.valueOf(statusCode.value()));
  }

  protected ResponseEntity<Object> buildErrorResponse(
      Exception exception, ErrorDefineCode errorCode, HttpStatus httpStatus) {
    CommonError error = new CommonError(errorCode.getCode(), LocalDateTime.now());
    if (printStackTrace && isTraceOn(exception)) {
      error.setStackTrace(getStackTrace(exception, printStackTraceLine));
    }
    CommonResponse<CommonError> errorResponseDto =
        new CommonResponse<>(false, httpStatus, errorCode.getMessage(), error);

    return ResponseEntity.status(httpStatus).body(errorResponseDto);
  }

  private String getStackTrace(Exception e, int line) {
    String stackTrace = ExceptionUtils.getStackTrace(e);

    // 스택 트레이스를 줄 단위로 분할하여 line 줄까지만 사용
    String[] lines = stackTrace.split(System.lineSeparator());
    StringBuilder limitedStackTrace = new StringBuilder();
    int limit = Math.min(lines.length, line);
    for (int i = 0; i < limit; i++) {
      limitedStackTrace.append(lines[i]).append(System.lineSeparator());
    }

    return limitedStackTrace.toString();
  }

  private boolean isTraceOn(Exception exception) {
    return exception.getStackTrace() != null && exception.getStackTrace().length > 0;
  }

  // 412 Validate Exception
  @Override
  @Hidden
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {
    CommonError error = new CommonError(ErrorDefineCode.VALID_ERROR.getCode(), LocalDateTime.now());
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      error.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
    }

    CommonResponse<CommonError> errorResponseDto =
        new CommonResponse<>(
            false,
            HttpStatus.UNPROCESSABLE_ENTITY,
            ErrorDefineCode.VALID_ERROR.getMessage(),
            error);

    return ResponseEntity.unprocessableEntity().body(errorResponseDto);
  }

  // 500 Uncaught Exception
  @ExceptionHandler(Exception.class)
  @Hidden
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<Object> handleAllUncaughtException(
      Exception exception, WebRequest request) {
    log.error("Internal error occurred", exception);
    return buildErrorResponse(
        exception, ErrorDefineCode.UNCAUGHT, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // 403 Access Denied Exception
  @ExceptionHandler(AccessDeniedException.class)
  @Hidden
  @ResponseStatus(HttpStatus.FORBIDDEN) // 403 Forbidden
  public ResponseEntity<Object> handleAccessDeniedException(
      AccessDeniedException exception, WebRequest request) {
    log.error("Access denied", exception);
    return buildErrorResponse(exception, ErrorDefineCode.AUTHENTICATE_FAIL, HttpStatus.FORBIDDEN);
  }
}
