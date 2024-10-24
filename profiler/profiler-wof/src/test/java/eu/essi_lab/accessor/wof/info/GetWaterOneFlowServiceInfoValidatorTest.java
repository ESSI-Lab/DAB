//package eu.essi_lab.accessor.wof.info;
//
//import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
//import eu.essi_lab.messages.ValidationMessage;
//import eu.essi_lab.messages.ValidationMessage.ValidationResult;
//import eu.essi_lab.messages.web.WebRequest;
//import eu.essi_lab.model.exceptions.GSException;
//import eu.essi_lab.pdk.validation.WebRequestValidator;
//
///**
// * @author boldrini
// */
//public class GetWaterOneFlowServiceInfoValidatorTest implements WebRequestValidator {
//
//    public GetWaterOneFlowServiceInfoValidatorTest() {
//    }
//
//    @Override
//    public ValidationMessage validate(WebRequest request) throws GSException {
//
//	if (request.isGetRequest()) {
//
//	    if (request.getRequestPath().contains("GetWaterOneFlowServiceInfo")) {
//		ValidationMessage message = new ValidationMessage();
//		message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
//		return message;
//	    }
//	}
//
//	return getErrorMessage("Post request issued instead of GET");
//    }
//
//    private ValidationMessage getErrorMessage(String error) {
//	ValidationMessage message = new ValidationMessage();
//	message.setResult(ValidationResult.VALIDATION_FAILED);
//	message.setError(error);
//	message.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.getCode());
//	return message;
//    }
//
//}
