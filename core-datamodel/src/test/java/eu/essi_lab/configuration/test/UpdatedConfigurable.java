//package eu.essi_lab.configuration.test;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import eu.essi_lab.model.configuration.AbstractGSconfigurable;
//import eu.essi_lab.model.configuration.option.GSConfOption;
//import eu.essi_lab.model.configuration.option.GSConfOptionString;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author ilsanto
// */
//public class UpdatedConfigurable extends AbstractGSconfigurable {
//
//    private Map<String, GSConfOption<?>> sup = new HashMap<>();
//
//    public UpdatedConfigurable() {
//
//	GSConfOptionString op1 = new GSConfOptionString();
//
//	op1.setKey("op1");
//
//	sup.put(op1.getKey(), op1);
//
//	GSConfOptionString op2 = new GSConfOptionString();
//
//	op2.setKey("op2");
//
//	sup.put(op2.getKey(), op2);
//
//    }
//
//    @Override
//    public Map<String, GSConfOption<?>> getSupportedOptions() {
//	return sup;
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
