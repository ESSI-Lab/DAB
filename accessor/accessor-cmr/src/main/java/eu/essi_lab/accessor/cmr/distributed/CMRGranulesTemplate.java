package eu.essi_lab.accessor.cmr.distributed;

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
public class CMRGranulesTemplate {

    private String baseURL;
    private String timeStartParam;
    private String timeEndParam;
    private String bboxParam;
    private String keywordParam;
    private String startParam;
    private String countParam;
    private String datasetIdParam;
    private String shortNameParam;
    private String dataCenterParam;

    private String timeStartValue;
    private String timeEndValue;
    private String bboxValue;
    private String keywordValue;
    private String startValue;
    private String countValue;
    private String datasetIdValue;
    private String shortNameValue;
    private String dataCenterValue;
    private static final String AND = "&";
    private static final String EQUAL = "=";

    public CMRGranulesTemplate(String templateUrl) {

	parse(templateUrl);

    }

    public void setBBox(String south, String north, String west, String east) {

	if (bboxParam != null)
	    bboxValue = west + "," + south + "," + east + "," + north;
    }

    public void setKeyword(String keyword) {
	// TODO
    }

    public void setStartTime(String startDateString) {

	if (timeStartParam != null)
	    timeStartValue = startDateString;

    }

    public void setEndTime(String endDateString) {

	if (timeEndParam != null)
	    timeEndValue = endDateString;

    }

    public void setStart(Integer startIndex) {

	if (startParam != null)
	    startValue = startIndex.toString();
    }

    public void setCount(int count) {

	if (countParam != null)
	    countValue = String.valueOf(count);
    }

    public void setDatasetId(String id) {

	if (datasetIdParam != null)
	    datasetIdValue = String.valueOf(id);
    }

    public void setDataCenter(String dataCenter) {

	if (dataCenterParam != null)
	    dataCenterValue = String.valueOf(dataCenter);

    }

    public void setShortName(String shortName) {

	if (shortNameParam != null)
	    shortNameValue = String.valueOf(shortName);

    }

    public String getRequestURL() {

	StringBuilder builder = new StringBuilder(baseURL);

	if (dataCenterValue != null)
	    builder.append(dataCenterParam).append(EQUAL).append(dataCenterValue).append(AND);

	if (shortNameValue != null)
	    builder.append(shortNameParam).append(EQUAL).append(shortNameValue).append(AND);

	if (datasetIdValue != null)
	    builder.append(datasetIdParam).append(EQUAL).append(datasetIdValue).append(AND);

	if (timeStartValue != null)
	    builder.append(timeStartParam).append(EQUAL).append(timeStartValue).append(AND);

	if (timeEndValue != null)
	    builder.append(timeEndParam).append(EQUAL).append(timeEndValue).append(AND);

	if (countValue != null)
	    builder.append(countParam).append(EQUAL).append(countValue).append(AND);

	if (startValue != null)
	    builder.append(startParam).append(EQUAL).append(startValue).append(AND);

	if (bboxValue != null)
	    builder.append(bboxParam).append(EQUAL).append(bboxValue).append(AND);

	return builder.toString();
    }

    private void parse(String template) {

	String b = finBaseURL(template);

	String staticParams = findStaticParams(template.replace(b, ""));

	baseURL = b + staticParams;

	timeStartParam = findTimeStart(template);

	timeEndParam = findTimeEnd(template);

	bboxParam = findBBox(template);

	keywordParam = findKeyword(template);

	startParam = findStart(template);

	countParam = findCount(template);

	datasetIdParam = findDatasetId(template);

	dataCenterParam = findDataCenterParam(template);

	shortNameParam = findShortNameParam(template);

    }

    private String findShortNameParam(String template) {
	String[] splitted = template.split("=\\{shortName\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{shortName\\}");

	}
	String[] finalsplit = splitted[0].split("&");
	if (finalsplit.length < 2) {
	    finalsplit = splitted[0].split("\\?");
	}
	return finalsplit[finalsplit.length - 1];
    }

    private String findDataCenterParam(String template) {
	String[] splitted = template.split("=\\{dataCenter\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{dataCenter\\}");

	}
	String[] finalsplit = splitted[0].split("&");
	if (finalsplit.length < 2) {
	    finalsplit = splitted[0].split("\\?");
	}
	return finalsplit[finalsplit.length - 1];
    }

    private String findDatasetId(String template) {
	String[] splitted = template.split("=\\{datasetId\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{datasetId\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:datasetId\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:datasetId\\?\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{echo:datasetId\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{echo:datasetId\\?\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	if (finalsplit.length < 2) {
	    finalsplit = splitted[0].split("\\?");
	}
	return finalsplit[finalsplit.length - 1];
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
	String[] splitted = template.split("=\\{count\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{count\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:count\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:count\\?\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findStart(String template) {
	String[] splitted = template.split("=\\{startIndex\\?\\}");

	if (splitted.length < 2) {

	    splitted = template.split("=\\{startIndex\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:startIndex\\}");

	}

	if (splitted.length < 2) {

	    splitted = template.split("=\\{os:startIndex\\?\\}");

	}

	String[] finalsplit = splitted[0].split("&");
	return finalsplit[finalsplit.length - 1];
    }

    private String findKeyword(String template) {

	// TODO
	return null;
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
