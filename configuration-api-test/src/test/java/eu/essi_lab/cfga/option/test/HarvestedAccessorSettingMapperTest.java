package eu.essi_lab.cfga.option.test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.option.mapper.HarvestedAccessorSettingMapper;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSettingLoader;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.option.OptionValueMapper;

/**
 * @author Fabrizio
 */
public class HarvestedAccessorSettingMapperTest {

    @Test
    public void test() {

	HarvestedAccessorSettingMapper mapper = new HarvestedAccessorSettingMapper();

	List<AccessorSetting> harvested = AccessorSettingLoader.loadHarvested();

	harvested.forEach(s -> System.out.println(s.getName()));

	{
	    Option<AccessorSetting> option = new Option<>(AccessorSetting.class);

	    option.setMapper(mapper);

	    harvested.forEach(acc -> option.addValue(acc));

	    test(option, harvested);
	    test(new Option<AccessorSetting>(option.getObject()), harvested);
	    test(new Option<AccessorSetting>(option.getObject().toString()), harvested);
	}

	//
	// Using builder
	//

	{

	    Option<AccessorSetting> buildedOption = OptionBuilder.get(AccessorSetting.class).//
		    withMapper(mapper).//
		    build();

	    harvested.forEach(acc -> buildedOption.addValue(acc));

	    test(buildedOption, harvested);
	    test(new Option<AccessorSetting>(buildedOption.getObject()), harvested);
	    test(new Option<AccessorSetting>(buildedOption.getObject().toString()), harvested);
	}
    }

    /**
     * @param option
     */
    private void test(Option<AccessorSetting> option, List<AccessorSetting> harvested) {

	//
	// mapper must be found
	//

	Optional<OptionValueMapper<AccessorSetting>> optionMapper = option.getOptionMapper();
	Assert.assertTrue(optionMapper.isPresent());

	Class<AccessorSetting> valueClass = optionMapper.get().getValueClass();
	Assert.assertEquals(AccessorSetting.class, valueClass);

	List<String> internalValues = option.//
		getObject().//
		getJSONArray("values").//
		toList().//
		stream().//
		map(o -> o.toString()).//
		sorted().//
		collect(Collectors.toList());

	List<String> settingNames = harvested.//
		stream().//
		map(a -> a.getName()).//
		sorted((s1, s2) -> s1.compareTo(s2)).//
		collect(Collectors.toList());

	Assert.assertEquals(internalValues, settingNames);

	//
	// external values test
	//

	List<AccessorSetting> list = harvested.//
		stream().//
		// map(a -> (AccessorSetting) a.getSetting()).//
		peek(a -> a.setIdentifier("id")).// newly created accessor settings (excluding GBIF, FEDEO, and the
						 // other fixed) have random id, so it must be set static in order to
						 // let the equality test working
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		collect(Collectors.toList());

	List<AccessorSetting> values = option.//
		getValues().//
		stream().//
		peek(a -> a.setIdentifier("id")).// newly created accessor settings (excluding GBIF, FEDEO, and the
		// other fixed) have random id, so it must be set static in order to
		// let the equality test working
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		collect(Collectors.toList());

	Assert.assertEquals(list, values);
    }
}
