package eu.essi_lab.accessor.nextgeoss.distributed;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
public class NextGEOSSGranulesTemplate {

    private String baseURL;
    private String startDateParam;
    private String endDateParam;
    private String bboxParam;
    private String keywordParam;
    private String startRecordParam;
    private String maximumRecordsParam;
    private String productTypeParam;

    private String startDateValue;
    private String endDateValue;
    private String bboxValue;
    private String keywordValue;
    private String startRecordValue;
    private String maximumRecordsValue;
    private String productTypeValue;
    private static final String AND = "&";
    private static final String EQUAL = "=";

    // "productType={eo:productType?}&rows={opensearch:count?}&timerange_end={time:end?}&metadata_modified={eo:modificationDate?}&clientId={referrer:source?}&q={opensearch:searchTerms?}&geom={geo:geometry?}&bbox={geo:box?}&identifier={geo:uid?}&timerange_start={time:start?}&page={opensearch:startPage?}&start_index={opensearch:startIndex?}";
    // bbox={geo:box?}&geometry={geo:geometry?}&name={geo:name?}&startDate={time:start?}&endDate={time:end?}&startPage={startPage?}&startRecord=1&maximumRecords=10&uid={geo:uid?}&lat={geo:lat?}&lon={geo:lon?}&radius={geo:radius?}&recordSchema={sru:recordSchema?}
    public void setProductType(String productType) {

	if (productTypeParam != null)
	    productTypeValue = productType;
    }

    public void setBBox(String south, String north, String west, String east) {

	if (bboxParam != null)
	    bboxValue = west + "," + south + "," + east + "," + north;
    }

    public void setKeyword(String keyword) {
	if(keywordParam != null)
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

    public String getRequestURL() {

	StringBuilder builder = new StringBuilder(baseURL);

	if (productTypeValue != null)
	    builder.append(productTypeParam).append(EQUAL).append(productTypeValue).append(AND);

	if (startDateValue != null)
	    builder.append(startDateParam).append(EQUAL).append(startDateValue).append(AND);

	if (endDateValue != null)
	    builder.append(endDateParam).append(EQUAL).append(endDateValue).append(AND);

	if (maximumRecordsValue != null)
	    builder.append(maximumRecordsParam).append(EQUAL).append(maximumRecordsValue).append(AND);

	if (startRecordValue != null)
	    builder.append(startRecordParam).append(EQUAL).append(startRecordValue).append(AND);
	
	if (keywordValue != null)
	    builder.append(keywordParam).append(EQUAL).append(keywordValue).append(AND);

	if (bboxValue != null)
	    builder.append(bboxParam).append(EQUAL).append(bboxValue).append(AND);

	return builder.toString();
    }

    public NextGEOSSGranulesTemplate(String templateUrl) {

	parse(templateUrl);

    }

    private void parse(String template) {

	String[] splittedURL = template.split("&");
	
	String b = finBaseURL(template);
	
	String staticParams = findStaticParams(template.replace(b, ""));
	
	baseURL = b + staticParams;

	//baseURL = (splittedURL.length < 2) ? splittedURL[0] + "&" : splittedURL[0] + "&" + splittedURL[1] + "&";

	productTypeParam = findProductType(template);

	startDateParam = findTimeStart(template);

	endDateParam = findTimeEnd(template);

	bboxParam = findBBox(template);

	keywordParam = findKeyword(template);

	startRecordParam = findStart(template);

	maximumRecordsParam = findCount(template);
	
	keywordParam = findKeyword(template);

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

	String[] finalsplit = splitted[0].split("&");
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
