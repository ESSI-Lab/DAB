//package eu.essi_lab.configuration.test;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
//import eu.essi_lab.model.configuration.IGSConfigurable;
//import eu.essi_lab.model.configuration.option.GSConfOption;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author ilsanto
// */
//public class UpdatedConfigurableComposed extends AbstractGSconfigurableComposed {
//
//    public UpdatedConfigurableComposed() {
//
//	IGSConfigurable u1 = new UpdatedConfigurable();
//
//	getConfigurableComponents().put("mysub1", u1);
//
//	getConfigurableComponents().put("mysub2", new UpdatedConfigurable());
//
//    }
//
//    @Override
//    public Map<String, GSConfOption<?>> getSupportedOptions() {
//	return new HashMap<>();
//    }
//
//    @Override
//    public void onOptionSet(GSConfOption<?> opt) throws GSException {
//
//    }
//
//    @Override
//    public void onFlush() throws GSException {
//
//    }
//}
