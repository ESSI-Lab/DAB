package eu.essi_lab.demo.profiler;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import eu.essi_lab.demo.extensions.DemoProvider;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;

/**
 * 
 * @author Fabrizio
 *
 */
public class DemoInfoHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	JSONObject info = new JSONObject();

	JSONObject service = new JSONObject();
	service.put("name", "demo-service");
	service.put("version", "1.0.0");
	info.put("service", service);

	JSONObject provider = new JSONObject();
	provider.put("name", DemoProvider.getInstance().getOrganization());
	provider.put("email", DemoProvider.getInstance().getEmail());
	info.put("provider", provider);

	return info.toString(3);
    }

    @Override
    public MediaType getMediaType(WebRequest request) {

	return MediaType.APPLICATION_JSON_TYPE;
    }
}
