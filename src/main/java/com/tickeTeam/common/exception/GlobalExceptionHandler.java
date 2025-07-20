package com.tickeTeam.common.exception;

import com.tickeTeam.common.exception.customException.BusinessException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e){
        log.error(e.getMessage(), e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    protected ResponseEntity<Void> handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        // 클라이언트가 먼저 연결을 끊어서 발생하는 자연스러운 예외이므로,
        // 별도의 에러 로그를 남기지 않고 null 응답을 보내 조용히 처리합니다.
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INPUT_INVALID_VALUE, e.getBindingResult());
        log.warn(e.getMessage());
        return ResponseEntity.status(ErrorCode.INPUT_INVALID_VALUE.getStatus()).body(response);
    }

    // @RestController 에서 발생한 에러 핸들러, @Controller 관련 어노테이션이 있어야지만 사용 가능
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleRuntimeException(BusinessException e){
        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = makeErrorResponse(errorCode);
        log.error(e.getMessage(), e);
        log.warn(e.getMessage());
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    private ErrorResponse makeErrorResponse(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .errorMessage(errorCode.getMessage())
                .businessCode(errorCode.getCode())
                .build();
    }
}
