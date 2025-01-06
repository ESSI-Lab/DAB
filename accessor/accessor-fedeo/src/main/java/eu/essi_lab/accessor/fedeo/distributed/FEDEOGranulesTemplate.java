package eu.essi_lab.accessor.fedeo.distributed;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author roncella
 */
public class FEDEOGranulesTemplate {

    private String baseURL;
    private String dateParam;
    private String startDateParam;
    private String endDateParam;
    private String bboxParam;
    private String latParam;
    private String lonParam;
    private String keywordParam;
    private String startRecordParam;
    private String maximumRecordsParam;

    private String dateValue;
    private String startDateValue;
    private String endDateValue;
    private String bboxValue;
    private String latValue;
    private String lonValue;
    private String keywordValue;
    private String startRecordValue;
    private String maximumRecordsValue;
    private static final String AND = "&";
    private static final String EQUAL = "=";
    private static final String SLASH = "/";

    // bbox={geo:box?}&geometry={geo:geometry?}&name={geo:name?}&startDate={time:start?}&endDate={time:end?}&startPage={startPage?}&startRecord=1&maximumRecords=10&uid={geo:uid?}&lat={geo:lat?}&lon={geo:lon?}&radius={geo:radius?}&recordSchema={sru:recordSchema?}

    public void setBBox(String south, String north, String west, String east) {

	bboxValue = west + "," + south + "," + east + "," + north;
    }

    public void setLat(String minLat) {
	latValue = minLat;
    }

    public void setLon(String minLon) {
	lonValue = minLon;
    }

    public void setKeyword(String keyword) {

	keywordValue = keyword;
    }

    public void setStartTime(String startDateString) {

	startDateValue = startDateString;
    }

    public void setDateTime(String startDateString, String endDateString) {

	dateValue = startDateString + "/" + endDateString;
    }

    public void setEndTime(String endDateString) {

	endDateValue = endDateString;

    }

    public void setStart(Integer startIndex) {

	startRecordValue = startIndex.toString();
    }

    public void setCount(int count) {

	maximumRecordsValue = String.valueOf(count);
    }

    public String getRequestURL() {

	StringBuilder builder = new StringBuilder(baseURL);

	if (endDateParam == null && dateParam != null) {
	    if (startDateValue != null) {
		builder.append(dateParam).append(EQUAL).append(startDateValue).append(SLASH);
		if (endDateValue != null) {
		    builder.append(endDateValue).append(AND);
		} else {
		    builder.append(AND);
		}
	    } else if (endDateValue != null) {
		builder.append(dateParam).append(EQUAL).append(SLASH).append(endDateValue).append(AND);
	    }
	} else {
	    if (startDateValue != null && startDateParam != null) {

		builder.append(startDateParam).append(EQUAL).append(startDateValue).append(AND);
	    } else if (startDateValue != null && startDateParam == null) {
		return null;
	    }

	    if (endDateValue != null && endDateParam != null) {
		builder.append(endDateParam).append(EQUAL).append(endDateValue).append(AND);
	    } else if (endDateValue != null && endDateParam == null) {
		return null;
	    }
	}

	if (maximumRecordsValue != null && maximumRecordsParam != null) {
	    builder.append(maximumRecordsParam).append(EQUAL).append(maximumRecordsValue).append(AND);
	} else if (maximumRecordsValue != null && maximumRecordsParam == null) {
	    return null;
	}

	if (startRecordValue != null && startRecordParam != null) {
	    builder.append(startRecordParam).append(EQUAL).append(startRecordValue).append(AND);
	} else if (startRecordValue != null && startRecordParam == null) {
	    return null;
	}

	if (bboxParam == null) {
	    if (latValue != null && latParam != null) {
		builder.append(latParam).append(EQUAL).append(latValue).append(AND);
	    } else if (latValue != null && latParam == null) {
		return null;
	    }
	    if (lonValue != null && lonParam != null) {
		builder.append(lonParam).append(EQUAL).append(lonValue).append(AND);
	    } else if (lonValue != null && lonParam == null) {
		return null;
	    }
	} else {
	    if (bboxValue != null && bboxParam != null) {
		builder.append(bboxParam).append(EQUAL).append(bboxValue).append(AND);
	    } else if (bboxValue != null && bboxParam == null) {
		return null;
	    }
	}

	if (keywordValue != null && keywordParam != null) {
	    builder.append(keywordParam).append(EQUAL).append(keywordValue).append(AND);
	} else if (keywordValue != null && keywordParam == null) {
	    GSLoggerFactory.getLogger(FEDEOGranulesTemplate.class).info("KEYWORD PARAM NOT SUPPORTED");
	    return null;
	}

	builder.append("clientId").append(EQUAL).append("geo-dab").append(AND);

	return builder.toString();
    }

    public FEDEOGranulesTemplate(String templateUrl) {

	parse(templateUrl);

    }

    private void parse(String template) {

	String[] splittedURL = template.split("&");

	String b = finBaseURL(template);

	String staticParams = findStaticParams(template.replace(b, ""));

	baseURL = b + staticParams;

	// baseURL = (splittedURL.length < 2) ? splittedURL[0] + "&" : splittedURL[0] + "&" + splittedURL[1] + "&";

	dateParam = findTime(template);

	startDateParam = findTimeStart(template);

	endDateParam = findTimeEnd(template);

	bboxParam = findBBox(template);

	if (bboxParam == null) {
	    latParam = findLat(template);
	    lonParam = findLon(template);
	}

	keywordParam = findKeyword(template);

	startRecordParam = findStart(template);

	maximumRecordsParam = findCount(template);

    }

    private String finBaseURL(String template) {
	return template.split("\\?")[0] + "?";
    }

    private String findStaticParams(String template) {

	List<String> params = Arrays.asList(template.split("&"));

	StringBuilder builder = new StringBuilder("");

	params.stream().filter(p -> !p.contains("{")).map(p -> p + "&").forEach(builder::append);

	return builder.toString();
    }

    private String findCount(String template) {
	String[] splitted = template.split("=\\{maximumRecords\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{count\\?\\}");

	    if (splitted.length < 2) {

		splitted = template.split("=\\{maximumRecords\\}");

	    }
	    if (splitted.length < 2) {

		splitted = template.split("=\\{count\\}");

	    }

	}
	if (splitted.length < 2) {
	    return null;
	}
	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findStart(String template) {
	String[] splitted = template.split("=\\{startRecord\\?\\}");

	if (splitted.length < 2) {

	    // try startIndex
	    splitted = template.split("=\\{startIndex\\?\\}");

	    if (splitted.length < 2) {

		splitted = template.split("=\\{startRecord\\}");
	    }

	    if (splitted.length < 2) {

		splitted = template.split("=\\{startIndex\\}");
	    }

	}
	if (splitted.length < 2) {
	    return null;
	}
	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findKeyword(String template) {

	String[] splitted = template.split("=\\{searchTerms\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{searchTerms\\}");

	}
	if (splitted.length < 2) {

	    splitted = template.split("=\\{query\\?\\}");
	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{query\\}");
	}

	if (splitted.length < 2) {
	    return null;
	}
	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findBBox(String template) {
	String[] splitted = template.split("=\\{geo\\:box\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{geo\\:box\\}");

	}
	if (splitted.length < 2) {
	    return null;
	}
	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findLat(String template) {
	String[] splitted = template.split("=\\{geo\\:lat\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{geo\\:lat\\}");

	}
	if (splitted.length < 2) {
	    return null;
	}
	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findLon(String template) {
	String[] splitted = template.split("=\\{geo\\:lon\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{geo\\:lon\\}");

	}
	if (splitted.length < 2) {
	    return null;
	}
	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findTimeEnd(String template) {

	String[] splitted = template.split("=\\{time\\:end\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{time\\:end\\}");

	}
	if (splitted.length < 2) {
	    return null;
	}
	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findTimeStart(String template) {

	String[] splitted = template.split("=\\{time\\:start\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{time\\:start\\}");

	}
	if (splitted.length < 2) {
	    return null;
	}
	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];

    }

    private String findTime(String template) {

	String[] splitted = template.split("=\\{time\\:start\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{time\\:start\\}");

	}
	if (splitted.length < 2) {
	    return null;
	}
	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];

    }

}
