//package eu.essi_lab.configuration.test;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import eu.essi_lab.model.configuration.AbstractGSconfigurable;
//import eu.essi_lab.model.configuration.Subcomponent;
//import eu.essi_lab.model.configuration.option.GSConfOption;
//import eu.essi_lab.model.configuration.option.GSConfOptionSubcomponent;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author ilsanto
// */
//public class UpdatedConfigurableSubcomponentOpt extends AbstractGSconfigurable {
//
//    private Map<String, GSConfOption<?>> sup = new HashMap<>();
//
//    public UpdatedConfigurableSubcomponentOpt() {
//
//	GSConfOptionSubcomponent op1 = new GSConfOptionSubcomponent();
//
//	op1.setKey("op1");
//
//	List<Subcomponent> vals = new ArrayList<>();
//
//	vals.add(new Subcomponent("l1", "v1"));
//
//	vals.add(new Subcomponent("l2", "v2"));
//
//	op1.setAllowedValues(vals);
//
//	sup.put(op1.getKey(), op1);
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
