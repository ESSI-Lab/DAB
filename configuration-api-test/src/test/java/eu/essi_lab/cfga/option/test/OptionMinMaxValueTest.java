package eu.essi_lab.cfga.option.test;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.DoubleOptionBuilder;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;

/**
 * @author Fabrizio
 */
public class OptionMinMaxValueTest {

    @Test
    public void integerTest() {

	Option<Integer> option = IntegerOptionBuilder.get().build();

	Optional<Number> minValue = option.getMinValue();
	Assert.assertFalse(minValue.isPresent());

	Optional<Number> maxValue = option.getMaxValue();
	Assert.assertFalse(maxValue.isPresent());

	option = IntegerOptionBuilder.get().//
		withMinValue(1).//
		withMaxValue(5).//
		build();

	minValue = option.getMinValue();
	Assert.assertEquals(1, minValue.get());

	maxValue = option.getMaxValue();
	Assert.assertEquals(5, maxValue.get());
    }
    
    @Test
    public void doubleTest() {

	Option<Double> option = DoubleOptionBuilder.get().build();

	Optional<Number> minValue = option.getMinValue();
	Assert.assertFalse(minValue.isPresent());

	Optional<Number> maxValue = option.getMaxValue();
	Assert.assertFalse(maxValue.isPresent());

	option = DoubleOptionBuilder.get().//
		withMinValue(1.2).//
		withMaxValue(5.3).//
		build();

	minValue = option.getMinValue();
	Assert.assertEquals(1.2, minValue.get());

	maxValue = option.getMaxValue();
	Assert.assertEquals(5.3, maxValue.get());
    }
}
