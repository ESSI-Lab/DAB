package eu.essi_lab.accessor.copernicus.dataspace.distributed;

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

import java.util.Arrays;
import java.util.List;

/**
 * @author roncella
 */
public class CopernicusDataspaceGranulesTemplate {

    private String baseURL;
    private String startDateParam;
    private String endDateParam;
    private String bboxParam;
    private String keywordParam;
    private String startRecordParam;
    private String maximumRecordsParam;
    private String productTypeParam;
    private String platformParam;
    private String instrumentParam;
    private String orbitNumberParam;
    private String cloudCoverParam; // sentinel 2 and //sentinel 3
    private String exactCountParam;
    private String orbitDirectionParam;
    private String sensorModeParam;
    private String timelinessParam; // sentinel 1 and //sentinel 3
    private String relativeOrbitNumberParam;
    private String polarisationParam; // sentinel 1
    private String swathParam; // sentinel 1
    private String sortParam;
    private String sortOrderParam;
    private String processingLevelParam;
    // sortParam={resto:sortParam?}&sortOrder={resto:sortOrder?}
    // sortParam={resto:sortParam?}&sortOrder={resto:sortOrder?}

    private String startDateValue;
    private String endDateValue;
    private String bboxValue;
    private String keywordValue;
    private String startRecordValue;
    private String maximumRecordsValue;
    private String productTypeValue;
    private String platformValue;
    private String instrumentValue;
    private String orbitNumberValue;
    // private String cloudCoverValue; // sentinel 2 and //sentinel 3
    private String cloudCoverMinValue;
    private String cloudCoverMaxValue;
    private String sensorModeValue;
    private boolean exactCountValue;
    private String orbitDirectionValue;
    private String timelinessValue; // sentinel 1 and //sentinel 3
    private String relativeOrbitNumberValue;
    private String polarisationValue; // sentinel 1
    private String swathValue; // sentinel 1
    private String sortValue;
    private String sortOrderValue;
    private String processingLevelValue;
    private static final String AND = "&";
    private static final String EQUAL = "=";
    private static final String OPEN_SQUARE = "[";
    private static final String CLOSE_SQUARE = "]";

    // "productType={eo:productType?}&rows={opensearch:count?}&timerange_end={time:end?}&metadata_modified={eo:modificationDate?}&clientId={referrer:source?}&q={opensearch:searchTerms?}&geom={geo:geometry?}&bbox={geo:box?}&identifier={geo:uid?}&timerange_start={time:start?}&page={opensearch:startPage?}&start_index={opensearch:startIndex?}";
    // bbox={geo:box?}&geometry={geo:geometry?}&name={geo:name?}&startDate={time:start?}&endDate={time:end?}&startPage={startPage?}&startRecord=1&maximumRecords=10&uid={geo:uid?}&lat={geo:lat?}&lon={geo:lon?}&radius={geo:radius?}&recordSchema={sru:recordSchema?}
    public void setProductType(String productType) {

	if (productTypeParam != null)
	    productTypeValue = productType;
    }

    public void setExactCount(boolean condition) {

	if (exactCountParam != null)
	    exactCountValue = condition;
    }

    public void setBBox(String south, String north, String west, String east) {

	if (bboxParam != null)
	    bboxValue = west + "," + south + "," + east + "," + north;
    }

    public void setKeyword(String keyword) {
	if (keywordParam != null)
	    keywordValue = keyword;
    }

    public void setStartTime(String startDateString) {

	if (startDateParam != null)
	    startDateValue = startDateString;

    }

    public void setEndTime(String endDateString) {

	if (endDateParam != null)
	    endDateValue = endDateString;

    }

    public void setStart(Integer startIndex) {

	if (startRecordParam != null)
	    startRecordValue = startIndex.toString();
    }

    public void setCount(int count) {

	if (maximumRecordsParam != null)
	    maximumRecordsValue = String.valueOf(count);
    }

    public void setInstrument(String instrument) {

	if (instrumentParam != null)
	    instrumentValue = instrument;
    }

    public void setOrbitNumber(String orbitNumber) {

	if (orbitNumberParam != null)
	    orbitNumberValue = orbitNumber;
    }

    public void setOrbitDirection(String orbitDirection) {

	if (orbitDirectionParam != null)
	    orbitDirectionValue = orbitDirection;
    }

    public void setMinCloudCover(String cloudCover) {

	if (cloudCoverParam != null)
	    cloudCoverMinValue = cloudCover;
    }

    public void setMaxCloudCover(String cloudCover) {

	if (cloudCoverParam != null)
	    cloudCoverMaxValue = cloudCover;
    }

    public void setTimeliness(String timeless) {

	if (timelinessParam != null)
	    timelinessValue = timeless;
    }

    public void setRelativeOrbit(String relativeOrbit) {

	if (relativeOrbitNumberParam != null)
	    relativeOrbitNumberValue = relativeOrbit;
    }

    public void setPolarisation(String polarisation) {

	if (polarisationParam != null)
	    polarisationValue = polarisation;
    }

    public void setSwath(String swath) {

	if (swathParam != null)
	    swathValue = swath;
    }

    public void setOrder(String sortOrder) {

	if (sortOrderParam != null)
	    sortOrderValue = sortOrder;
    }

    public void setSort(String sort) {

	if (sortParam != null)
	    sortValue = sort;
    }

    public void setSensorMode(String sensorMode) {

	if (sensorModeParam != null)
	    sensorModeValue = sensorMode;
    }

    public void setProcessingLevel(String processingLevel) {

	if (processingLevelParam != null)
	    processingLevelValue = processingLevel;
    }

    public String getRequestURL() {

	StringBuilder builder = new StringBuilder(baseURL);

	if (productTypeValue != null)
	    builder.append(productTypeParam).append(EQUAL).append(productTypeValue).append(AND);

	if (exactCountValue)
	    builder.append(exactCountParam).append(EQUAL).append(exactCountValue).append(AND);

	if (startDateValue != null)
	    builder.append(startDateParam).append(EQUAL).append(startDateValue).append(AND);

	if (endDateValue != null)
	    builder.append(endDateParam).append(EQUAL).append(endDateValue).append(AND);

	if (maximumRecordsValue != null)
	    builder.append(maximumRecordsParam).append(EQUAL).append(maximumRecordsValue).append(AND);

	if (startRecordValue != null)
	    builder.append(startRecordParam).append(EQUAL).append(startRecordValue).append(AND);

	// if (keywordValue != null)
	// builder.append(keywordParam).append(EQUAL).append(keywordValue).append(AND);

	if (sensorModeValue != null)
	    builder.append(sensorModeParam).append(EQUAL).append(sensorModeValue).append(AND);

	if (platformValue != null)
	    builder.append(platformParam).append(EQUAL).append(platformValue).append(AND);

	if (instrumentValue != null)
	    builder.append(instrumentParam).append(EQUAL).append(instrumentValue).append(AND);

	if (orbitNumberValue != null)
	    builder.append(orbitNumberParam).append(EQUAL).append(orbitNumberValue).append(AND);

	if (orbitDirectionValue != null)
	    builder.append(orbitDirectionParam).append(EQUAL).append(orbitDirectionValue).append(AND);

	if (timelinessValue != null)
	    builder.append(timelinessParam).append(EQUAL).append(timelinessValue).append(AND);

	if (cloudCoverMinValue != null && cloudCoverMaxValue != null)
	    builder.append(cloudCoverParam).append(EQUAL).append(OPEN_SQUARE).append(cloudCoverMinValue).append(",")
		    .append(cloudCoverMaxValue).append(CLOSE_SQUARE).append(AND);

	if (relativeOrbitNumberValue != null)
	    builder.append(relativeOrbitNumberParam).append(EQUAL).append(relativeOrbitNumberValue).append(AND);

	if (polarisationValue != null)
	    builder.append(polarisationParam).append(EQUAL).append(polarisationValue).append(AND);

	if (swathValue != null)
	    builder.append(swathParam).append(EQUAL).append(swathValue).append(AND);

	if (bboxValue != null)
	    builder.append(bboxParam).append(EQUAL).append(bboxValue).append(AND);

	if (processingLevelValue != null)
	    builder.append(processingLevelParam).append(EQUAL).append(processingLevelValue).append(AND);

	if (sortValue != null)
	    builder.append(sortParam).append(EQUAL).append(sortValue).append(AND);

	if (sortOrderValue != null)
	    builder.append(sortOrderParam).append(EQUAL).append(sortOrderValue).append(AND);

	return builder.toString();
    }

    public CopernicusDataspaceGranulesTemplate(String templateUrl) {

	parse(templateUrl);

    }

    private void parse(String template) {

	String[] splittedURL = template.split("&");

	String b = finBaseURL(template);

	String staticParams = findStaticParams(template.replace(b, ""));

	baseURL = b + staticParams;

	// baseURL = (splittedURL.length < 2) ? splittedURL[0] + "&" : splittedURL[0] + "&" + splittedURL[1] + "&";

	startDateParam = findTimeStart(template);

	endDateParam = findTimeEnd(template);

	bboxParam = findBBox(template);

	// keywordParam = findKeyword(template);

	startRecordParam = findStart(template);

	maximumRecordsParam = findCount(template);

	productTypeParam = findProductType(template);

	platformParam = findGenericParam(template, "platform");
	instrumentParam = findGenericParam(template, "instrument");
	orbitNumberParam = findGenericParam(template, "orbitNumber");
	cloudCoverParam = findGenericParam(template, "cloudCover"); // sentinel 2 and //sentinel 3
	sensorModeParam = findGenericParam(template, "sensorMode");
	exactCountParam = findGenericParam(template, "exactCount");
	orbitDirectionParam = findGenericParam(template, "orbitDirection");
	timelinessParam = findGenericParam(template, "timeliness"); // sentinel 1 and //sentinel 3
	relativeOrbitNumberParam = findGenericParam(template, "relativeOrbitNumber");
	polarisationParam = findGenericParam(template, "polarisation"); // sentinel 1
	swathParam = findGenericParam(template, "swath"); // sentinel 1
	processingLevelParam = findGenericParam(template, "processingLevel"); // sentinel 3
	sortOrderParam = findGenericParam(template, "sortOrder");
	sortParam = findGenericParam(template, "sortParam");

    }

    private String findStaticParams(String template) {

	List<String> params = Arrays.asList(template.split("&"));

	StringBuilder builder = new StringBuilder("");

	params.stream().filter(p -> !p.contains("{")).map(p -> p + "&").forEach(builder::append);

	return builder.toString();
    }

    private String finBaseURL(String template) {
	return template.split("\\?")[0] + "?";
    }

    private String findProductType(String template) {

	String[] splitted = template.split("=\\{eo:productType\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{productType\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{eo:productType\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{productType\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{echo:productType\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{echo:productType\\?\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	if (finalsplit.length < 2) {
	    finalsplit = splitted[0].split("\\?");
	}
	return finalsplit[finalsplit.length - 1];

    }

    private String findPlatform(String template) {

	String[] splitted = template.split("=\\{eo:platform\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{platform\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{eo:platform\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{platform\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{echo:platform\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{echo:platform\\?\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	if (finalsplit.length < 2) {
	    finalsplit = splitted[0].split("\\?");
	}
	return finalsplit[finalsplit.length - 1];

    }

    private String findInstrument(String template) {

	String[] splitted = template.split("=\\{eo:instrument\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{instrument\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{eo:instrument\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{instrument\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{echo:instrument\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{echo:instrument\\?\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	if (finalsplit.length < 2) {
	    finalsplit = splitted[0].split("\\?");
	}
	return finalsplit[finalsplit.length - 1];

    }

    private String findGenericParam(String template, String value) {

	String[] splitted = template.split("=\\{eo:" + value + "\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{" + value + "\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{eo:" + value + "\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{" + value + "\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{resto:" + value + "\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{resto:" + value + "\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{sentinel:" + value + "\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{sentinel:" + value + "\\?\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	// if (finalsplit.length < 2) {
	// finalsplit = splitted[0].split("\\?");
	// }
	return finalsplit[finalsplit.length - 1];

    }

    private String findCount(String template) {
	String[] splitted = template.split("=\\{opensearch:count\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{opensearch:count\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:count\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:count\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{count\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{count\\}");

	}

	String[] finalsplit = splitted[0].split("\\?");
	return finalsplit[finalsplit.length - 1];
    }

    private String findStart(String template) {
	String[] splitted = template.split("=\\{opensearch:startPage\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{opensearch:startPage\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:startPage\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:startPage\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{startPage\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{startPage\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findKeyword(String template) {

	String[] splitted = template.split("=\\{opensearch:searchTerms\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{opensearch:searchTerms\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:searchTerms\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:searchTerms\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{searchTerms\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{searchTerms\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findBBox(String template) {
	String[] splitted = template.split("=\\{geo\\:box\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{geo\\:box\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findTimeEnd(String template) {

	String[] splitted = template.split("=\\{time\\:end\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{time\\:end\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findTimeStart(String template) {

	String[] splitted = template.split("=\\{time\\:start\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{time\\:start\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];

    }

}
