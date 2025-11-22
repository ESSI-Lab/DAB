/**
 * 
 */
package eu.essi_lab.messages;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class StatisticsResponseTest {

    @Test
    public void test() throws UnsupportedEncodingException, JAXBException {

	StatisticsResponse response = new StatisticsResponse(ResourceProperty.SOURCE_ID.getName());

	//
	// adds one response item
	//
	ResponseItem responseItem = new ResponseItem("sourceId1");
	response.getItems().add(responseItem);

	// 2 min computation results
	ComputationResult min1 = new ComputationResult();
	min1.setValue("1");
	min1.setTarget(ResourceProperty.ACCESS_QUALITY.getName());
	responseItem.addMin(min1);

	ComputationResult min2 = new ComputationResult();
	min2.setValue("2");
	min2.setTarget(ResourceProperty.SSC_SCORE.getName());
	responseItem.addMin(min2);

	// 3 max computation results
	ComputationResult max1 = new ComputationResult();
	max1.setValue("1000");
	max1.setTarget(ResourceProperty.ACCESS_QUALITY.getName());
	responseItem.addMax(max1);

	ComputationResult max2 = new ComputationResult();
	max2.setValue("2000");
	max2.setTarget(ResourceProperty.EXECUTION_TIME.getName());
	responseItem.addMax(max2);

	ComputationResult max3 = new ComputationResult();
	max3.setValue("3000");
	max3.setTarget(ResourceProperty.ORIGINAL_ID.getName());
	responseItem.addMax(max3);

	// 1 count distinct computation result
	ComputationResult countDistinct = new ComputationResult();
	countDistinct.setValue("999");
	countDistinct.setTarget(ResourceProperty.COMPLIANCE_LEVEL.getName());
	responseItem.addCountDistinct(countDistinct);

	// 1 sum distinct computation result
	ComputationResult sum = new ComputationResult();
	sum.setValue("100");
	sum.setTarget(ResourceProperty.ACCESS_QUALITY.getName());
	responseItem.addSum(sum);

	// 1 avg computation result
	ComputationResult avg = new ComputationResult();
	avg.setValue("10");
	avg.setTarget(ResourceProperty.ACCESS_QUALITY.getName());
	responseItem.addAvg(avg);

	// 1 bboxUnion computation result
	ComputationResult bboxUnion = new ComputationResult();
	bboxUnion.setValue("0 1 2 3");
	bboxUnion.setTarget(MetadataElement.BOUNDING_BOX.getName());
	responseItem.setBBoxUnion(bboxUnion);

	//
	// adds another empty response item
	//
	ResponseItem responseItem2 = new ResponseItem();
	response.getItems().add(responseItem2);

	test(response);

	//
	// unmarshall test
	//
	String asString = response.asString(true);
	System.out.println(asString);
	response = StatisticsResponse.create(asString);
	test(response);
    }

    private void test(StatisticsResponse response) {

	String groupBy = response.getGroupBy().get();
	Assert.assertEquals(ResourceProperty.SOURCE_ID.getName(), groupBy);

	int itemsCount = response.getReturnedItemsCount();
	Assert.assertEquals(2, itemsCount);

	ResponseItem responseItem = response.getItems().getFirst();

	//
	// min
	//
	List<ComputationResult> min = responseItem.getMin();
	Assert.assertEquals(2, min.size());

	Assert.assertEquals("1", min.getFirst().getValue());
	Assert.assertFalse(min.get(0).getCardinalValues().isPresent());

	Assert.assertEquals(ResourceProperty.ACCESS_QUALITY.getName(), min.get(0).getTarget());

	Assert.assertEquals("2", min.get(1).getValue());
	Assert.assertFalse(min.get(1).getCardinalValues().isPresent());

	Assert.assertEquals(ResourceProperty.SSC_SCORE.getName(), min.get(1).getTarget());

	Optional<ComputationResult> minAQ = responseItem.getMin(ResourceProperty.ACCESS_QUALITY);
	Assert.assertEquals("1", minAQ.get().getValue());

	Optional<ComputationResult> minSSC = responseItem.getMin(ResourceProperty.SSC_SCORE);
	Assert.assertEquals("2", minSSC.get().getValue());

	//
	// max
	//
	List<ComputationResult> max = responseItem.getMax();
	Assert.assertEquals(3, max.size());

	Assert.assertEquals("1000", max.get(0).getValue());
	Assert.assertEquals(ResourceProperty.ACCESS_QUALITY.getName(), max.get(0).getTarget());

	Assert.assertEquals("2000", max.get(1).getValue());
	Assert.assertEquals(ResourceProperty.EXECUTION_TIME.getName(), max.get(1).getTarget());

	Assert.assertEquals("3000", max.get(2).getValue());
	Assert.assertEquals(ResourceProperty.ORIGINAL_ID.getName(), max.get(2).getTarget());

	Optional<ComputationResult> maxAQ = responseItem.getMax(ResourceProperty.ACCESS_QUALITY);
	Assert.assertEquals("1000", maxAQ.get().getValue());

	Optional<ComputationResult> maxEX = responseItem.getMax(ResourceProperty.EXECUTION_TIME);
	Assert.assertEquals("2000", maxEX.get().getValue());

	Optional<ComputationResult> maxORIG = responseItem.getMax(ResourceProperty.ORIGINAL_ID);
	Assert.assertEquals("3000", maxORIG.get().getValue());

	//
	// sum
	//
	List<ComputationResult> sum = responseItem.getSum();
	Assert.assertEquals(1, sum.size());

	Assert.assertEquals("100", sum.getFirst().getValue());
	Assert.assertEquals(ResourceProperty.ACCESS_QUALITY.getName(), sum.getFirst().getTarget());

	//
	// avg
	//
	List<ComputationResult> avg = responseItem.getAvg();
	Assert.assertEquals(1, avg.size());

	Assert.assertEquals("10", avg.getFirst().getValue());
	Assert.assertEquals(ResourceProperty.ACCESS_QUALITY.getName(), avg.getFirst().getTarget());

	//
	// countDistinct
	//
	List<ComputationResult> countDistinct = responseItem.getCountDistinct();
	Assert.assertEquals(1, countDistinct.size());

	Assert.assertEquals("999", countDistinct.getFirst().getValue());
	Assert.assertEquals(ResourceProperty.COMPLIANCE_LEVEL.getName(), countDistinct.getFirst().getTarget());

	Assert.assertFalse(countDistinct.getFirst().getCardinalValues().isPresent());

	//
	// bBoxUnion
	//
	ComputationResult bBoxUnion = responseItem.getBBoxUnion();

	Optional<CardinalValues> cardinalValues = bBoxUnion.getCardinalValues();

	Assert.assertEquals("0", cardinalValues.get().getWest());
	Assert.assertEquals("1", cardinalValues.get().getSouth());
	Assert.assertEquals("2", cardinalValues.get().getEast());
	Assert.assertEquals("3", cardinalValues.get().getNorth());
    }

}
