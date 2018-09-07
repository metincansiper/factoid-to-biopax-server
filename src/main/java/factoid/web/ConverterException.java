package factoid.web;

import org.springframework.http.HttpStatus;

public class ConverterException extends RuntimeException {
  private final HttpStatus status;

  ConverterException(HttpStatus status, String msg) {
    super(msg);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
