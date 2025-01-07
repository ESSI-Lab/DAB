package eu.essi_lab.accessor.dws.client;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class DWSPrimaryData {

    private List<DWSData> levelDatas = null;
    private List<DWSData> dischargeDatas = null;

    public List<DWSData> getLevelData() {
	return levelDatas;
    }

    public List<DWSData> getDischargeData() {
	return dischargeDatas;
    }

    public DWSPrimaryData(InputStream is) {
	BufferedReader reader;
	levelDatas = new ArrayList<>();
	dischargeDatas = new ArrayList<>();
	try {
	    reader = new BufferedReader(new InputStreamReader(is));
	    String line = null;
	    boolean dataStarted = false;
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmmss");	   
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));

	    while ((line = reader.readLine()) != null) {
		if (dataStarted) {

		    String dateString = line.substring(0, 15).trim();

		    try {
			Date date = dateFormat.parse(dateString);
			DWSData levelData = new DWSData(date);
			DWSData dischargeData = new DWSData(date);
			// LEVEL
			{
			    String valueString = line.substring(26, 35).trim();
			    if (!valueString.isEmpty()) {
				try {
				    BigDecimal value = new BigDecimal(valueString);
				    levelData.setValue(value);
				} catch (Exception e) {
				    GSLoggerFactory.getLogger(getClass()).error("Unable to parse value {}", valueString);
				}
			    }
			    String qualityString = line.substring(36, 39).trim();
			    if (!qualityString.isEmpty()) {
				levelData.setQualityCode(qualityString);
			    }
			    levelDatas.add(levelData);
			}
			// DISCHARGE
			{
			    String valueString = line.substring(51, 60).trim();
			    if (!valueString.isEmpty()) {
				try {
				    BigDecimal value = new BigDecimal(valueString);
				    dischargeData.setValue(value);
				} catch (Exception e) {
				    GSLoggerFactory.getLogger(getClass()).error("Unable to parse value {}", valueString);
				}
			    }
			    String qualityString = line.substring(60, 63).trim();
			    if (!qualityString.isEmpty()) {
				dischargeData.setQualityCode(qualityString);
			    }
			    dischargeDatas.add(dischargeData);
			}

		    } catch (ParseException e) {
			GSLoggerFactory.getLogger(getClass()).error("Unable to parse date {}", dateString);
		    }

		}
		if (line.toUpperCase().startsWith("ZZZ")) {
		    break;
		}
		if (line.toUpperCase().startsWith("DATE")) {
		    dataStarted = true;
		}

	    }
	    reader.close();
	} catch (

	IOException e) {
	    e.printStackTrace();
	}
    }

}
