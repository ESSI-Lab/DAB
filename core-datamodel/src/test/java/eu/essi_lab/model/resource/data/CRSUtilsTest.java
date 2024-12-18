package eu.essi_lab.model.resource.data;

import static org.junit.Assert.assertEquals;

import java.util.AbstractMap.SimpleEntry;

import org.junit.Test;

public class CRSUtilsTest {

    @Test
    public void test() throws Exception {
	SimpleEntry<Double, Double> lower = new SimpleEntry<Double, Double>(0., 0.);
	SimpleEntry<Double, Double> upper = new SimpleEntry<Double, Double>(45., 45.);
	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> sourceCorners = new SimpleEntry<SimpleEntry<Double,Double>, SimpleEntry<Double,Double>>(lower, upper);
	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> result2 = CRSUtils.translateBBOX(sourceCorners , CRS.EPSG_4326(), CRS.EPSG_3857());
	SimpleEntry<Double, Double> lower2 = result2.getKey();	
	SimpleEntry<Double, Double> upper2 = result2.getValue();
	System.out.println(lower2);
	System.out.println(upper2);
	assertEquals(0.0, lower2.getKey(),0.00001);
	assertEquals(0.0, lower2.getValue(),0.00001);
	assertEquals(5009377.085697311, upper2.getKey(),0.00001);
	assertEquals(5621521.486192066, upper2.getValue(),0.00001);
	
	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> result3 = CRSUtils.translateBBOX(result2, CRS.EPSG_3857(), CRS.EPSG_4326());
	SimpleEntry<Double, Double> lower3 = result3.getKey();	
	SimpleEntry<Double, Double> upper3 = result3.getValue();
	System.out.println(lower3);
	System.out.println(upper3);
	assertEquals(0.0, lower3.getKey(),0.00001);
	assertEquals(0.0, lower3.getValue(),0.00001);
	assertEquals(45.0, upper3.getKey(),0.00001);
	assertEquals(45.0, upper3.getValue(),0.00001);
    }
    
    public static void main(String[] args) throws Exception {    
    	
//    	Original bbox: minx 0.0 miny -85.05112877979998 maxx 2.0037508342789244E7 maxy 2.003742329165193E7
//    	Created request with bbox south(-7.640272891266931E-4),west(0.0),north(85.05106286921993),east(179.9999999999)
    	
    	SimpleEntry<Double, Double> lower = new SimpleEntry<Double, Double>(0.0,-85.05112877979998);//-10018754.171394622 0
    	SimpleEntry<Double, Double> upper = new SimpleEntry<Double, Double>(2.0037508342789244E7,2.003742329165193E7);//0 10018754.171394628
    	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> result2 = new SimpleEntry<>(lower,upper);
    	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> result3 = CRSUtils.translateBBOX(result2, CRS.EPSG_3857(), CRS.EPSG_4326());
    	SimpleEntry<Double, Double> lower3 = result3.getKey();	
    	SimpleEntry<Double, Double> upper3 = result3.getValue();
    	System.out.println(lower3);
    	System.out.println(upper3);
    	
    	SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> result4 = CRSUtils.translateBBOX(result3, CRS.EPSG_4326(), CRS.EPSG_3857());
    	SimpleEntry<Double, Double> lower4 = result4.getKey();	
    	SimpleEntry<Double, Double> upper4 = result4.getValue();
    	System.out.println(lower4);
    	System.out.println(upper4);
    	
    	
	}

}
