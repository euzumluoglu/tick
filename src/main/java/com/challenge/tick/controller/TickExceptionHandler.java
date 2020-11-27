package com.challenge.tick.controller;

import com.challenge.tick.dto.ExceptionDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class TickExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value
      = {Exception.class})
  protected ResponseEntity<ExceptionDto> handleException(RuntimeException ex, WebRequest request) {
    List<String> details = new ArrayList<>();
    details.add(ex.getLocalizedMessage());
    ExceptionDto error = new ExceptionDto("Unexpected exception occured", details);
    logger.error("unexpected exception occured", ex);
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
