package factoid.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdvice {

  @ExceptionHandler(ConverterException.class)
  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleException(ConverterException e) {
    return String.format("%s; %s", e.getStatus().getReasonPhrase(), e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseBody
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleException(Exception e) {
    return e.toString();
  }

}
