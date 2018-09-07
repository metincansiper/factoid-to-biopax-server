package factoid.web;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import factoid.converter.FactoidToBiopax;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/v1", method = {RequestMethod.POST})
public class Controller {

  public Controller() {
  }

  @ApiOperation(value = "json-to-biopax", notes = "Converts a Factoid model to BioPAX.")
  @RequestMapping(path = "/json-to-biopax",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/vnd.biopax.rdf+xml"
  )
  public String jsonToBiopax(
    @ApiParam("Factoid document content (JSON string)") @RequestBody String body,
    //TODO: add url/path options as needed
    @ApiParam("test") @RequestParam(required = false) String test)
  {
    // Add templates to converter by the reader
    FactoidToBiopax converter = new FactoidToBiopax();
    try {
      converter.addToModel(body);
    } catch (IllegalStateException | JsonSyntaxException | JsonIOException e){
      throw new ConverterException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      throw new ConverterException(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
    }

    // Convert the model to biopax string
    return converter.convertToBiopax();
  }

}
