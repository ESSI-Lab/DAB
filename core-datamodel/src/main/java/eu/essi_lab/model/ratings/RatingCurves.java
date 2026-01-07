package eu.essi_lab.model.ratings;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

public class RatingCurves {
    public Collection<RatingCurve> getCurves() {
	return curves;
    }

    private final Collection<RatingCurve> curves;

    public RatingCurves() {
	this(new ArrayList<>());
    }

    public RatingCurves(Collection<RatingCurve> curves) {
	this.curves = curves;
    }

    /**
     * Generates a WaterML 2.0 Part 2 XML encoded document from the rating curves.
     * The format follows the WaterML Part 2 specification for Ratings, Gaugings and Sections (RGS).
     * 
     * @return WaterML 2.0 Part 2 XML as a String
     */
    public String toWaterML2() {
	StringBuilder xml = new StringBuilder();
	DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
	
	xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	xml.append("<rgs:ConversionGroup xmlns:gml=\"http://www.opengis.net/gml/3.2\"\n");
	xml.append("  xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n");
	xml.append("  xmlns:swe=\"http://www.opengis.net/swe/2.0\"\n");
	xml.append("  xmlns:wml2=\"http://www.opengis.net/waterml/2.0\"\n");
	xml.append("  xmlns:om=\"http://www.opengis.net/om/2.0\"\n");
	xml.append("  xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"\n");
	xml.append("  xmlns:gco=\"http://www.isotc211.org/2005/gco\"\n");
	xml.append("  xmlns:sams=\"http://www.opengis.net/samplingSpatial/2.0\"\n");
	xml.append("  xmlns:sam=\"http://www.opengis.net/sampling/2.0\"\n");
	xml.append("  xmlns:gsr=\"http://www.isotc211.org/2005/gsr\"\n");
	xml.append("  xmlns:gts=\"http://www.isotc211.org/2005/gts\"\n");
	xml.append("  xmlns:gss=\"http://www.isotc211.org/2005/gss\"\n");
	xml.append("  xmlns:rgs=\"http://www.opengis.net/waterml/part2/1.0\"\n");
	xml.append("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
	xml.append("  xsi:schemaLocation=\"http://www.opengis.net/waterml/part2/1.0 ");
	xml.append("http://schemas.opengis.net/waterml/part2/1.0/waterml2-part2.xsd\"\n");
	xml.append("  gml:id=\"conversion-group-1\">\n");
	
	xml.append("  <gml:description>Rating curves conversion group</gml:description>\n");
	xml.append("  <rgs:monitoringPoint></rgs:monitoringPoint>\n");
	xml.append("  <rgs:fullConversion>false</rgs:fullConversion>\n");
	
	int curveIndex = 0;
	for (RatingCurve curve : curves) {
	    curveIndex++;
	    
	    xml.append("  <rgs:period>\n");
	    xml.append("    <rgs:ConversionPeriod>\n");
	    
	    // Period start
	    xml.append("      <rgs:periodStart>\n");
	    xml.append("        <gml:TimeInstant gml:id=\"ti-start-").append(curveIndex).append("\">\n");
	    xml.append("          <gml:timePosition>").append(curve.getBeginDate().format(dateFormatter)).append("</gml:timePosition>\n");
	    xml.append("        </gml:TimeInstant>\n");
	    xml.append("      </rgs:periodStart>\n");
	    
	    // Period end
	    xml.append("      <rgs:periodEnd>\n");
	    xml.append("        <gml:TimeInstant gml:id=\"ti-end-").append(curveIndex).append("\">\n");
	    xml.append("          <gml:timePosition>").append(curve.getEndDate().format(dateFormatter)).append("</gml:timePosition>\n");
	    xml.append("        </gml:TimeInstant>\n");
	    xml.append("      </rgs:periodEnd>\n");
	    
	    // Conversion table
	    xml.append("      <rgs:applicableConversion>\n");
	    xml.append("        <rgs:ConversionTable gml:id=\"table-").append(curveIndex).append("\">\n");
	    xml.append("        <rgs:monitoringPoint></rgs:monitoringPoint>\n");
	    // Input and output properties
	    xml.append("          <rgs:inputProperty xlink:href=\"http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/12252\" xlink:title=\"Water level\"/>\n");
	    xml.append("          <rgs:outputProperty xlink:href=\"http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/171\" xlink:title=\"Stream discharge\"/>\n");
	    
	    // Metadata
	    xml.append("          <rgs:metadata>\n");
	    xml.append("            <rgs:ConversionMetadata gml:id=\"con-md-").append(curveIndex).append("\">\n");
	    xml.append("              <rgs:developmentMethod></rgs:developmentMethod>\n");
	    xml.append("              <rgs:releaseStatus/>\n");
	    xml.append("            </rgs:ConversionMetadata>\n");
	    xml.append("          </rgs:metadata>\n");
	    
	    // Rating table points
	    for (RatingCurvePoint point : curve.getPoints()) {
		xml.append("          <rgs:point>\n");
		xml.append("            <rgs:TableTuple>\n");
		
		// Input value (level/stage)
		xml.append("              <rgs:inputValue>\n");
		xml.append("                <swe:Quantity>\n");
		xml.append("                  <swe:uom code=\"m\"/>\n");
		xml.append("                  <swe:value>").append(point.getLevel()).append("</swe:value>\n");
		xml.append("                </swe:Quantity>\n");
		xml.append("              </rgs:inputValue>\n");
		
		// Output value (discharge)
		xml.append("              <rgs:outputValue>\n");
		xml.append("                <swe:Quantity>\n");
		xml.append("                  <swe:uom code=\"m3/s\"/>\n");
		xml.append("                  <swe:value>").append(point.getDischarge()).append("</swe:value>\n");
		xml.append("                </swe:Quantity>\n");
		xml.append("              </rgs:outputValue>\n");
		
		xml.append("            </rgs:TableTuple>\n");
		xml.append("          </rgs:point>\n");
	    }
	    
	    xml.append("        </rgs:ConversionTable>\n");
	    xml.append("      </rgs:applicableConversion>\n");
	    xml.append("    </rgs:ConversionPeriod>\n");
	    xml.append("  </rgs:period>\n");
	}
	
	xml.append("</rgs:ConversionGroup>\n");
	
	return xml.toString();
    }

    public static void main(String[] args) {
	RatingCurves rcs = new RatingCurves();
	RatingCurve rc = new RatingCurve(LocalDate.now().minusDays(1), LocalDate.now());
	rc.addPoint(new RatingCurvePoint(BigDecimal.ONE, BigDecimal.TWO));
	rc.addPoint(new RatingCurvePoint(BigDecimal.TWO, BigDecimal.ZERO));
	rcs.getCurves().add(rc);
	RatingCurve rc2Curve = new RatingCurve(LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
	rc2Curve.addPoint(new RatingCurvePoint(BigDecimal.ONE, BigDecimal.TWO));
	rc2Curve.addPoint(new RatingCurvePoint(BigDecimal.TWO, BigDecimal.ZERO));
	rcs.getCurves().add(rc2Curve);
	System.out.println(rcs);
	System.out.println("\n--- WaterML 2.0 XML ---\n");
	System.out.println(rcs.toWaterML2());
    }
}
