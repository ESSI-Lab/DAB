package eu.essi_lab.accessor.savahis;

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

import java.time.Duration;

public enum SavaHISFrequency {

    HOURLY("Hourly", "Hourly", Duration.ofHours(1)), //
    //
    DAILY("Daily", "Daily", Duration.ofDays(1)), //
    DAILY_SUM("DailySum", "Daily Sum", Duration.ofDays(1)), //
    DAILY_AVG("DailyAvg", "Daily Average", Duration.ofDays(1)), //
    DAILY_MIN("DailyMin", "Daily Minimum", Duration.ofDays(1)), //
    DAILY_MAX("DailyMax", "Daily Maximum", Duration.ofDays(1)), //
    //
    MONTHLY("Monthly", "Monthly", Duration.ofDays(30)), //
    MONTHLY_SUM("MonthlySum", "Monthly Sum", Duration.ofDays(30)), //
    MONTHLY_AVG("MonthlyAvg", "Monthly Average", Duration.ofDays(30)), //
    MONTHLY_MIN("MonthlyMin", "Monthly Minimum", Duration.ofDays(30)), //
    MONTHLY_MAX("MonthlyMax", "Monthly Maximum", Duration.ofDays(30)), //
    //
    YEARLY("Yearly", "Yearly", Duration.ofDays(365)), //
    YEARLY_SUM("YearlySum", "Yearly Sum", Duration.ofDays(365)), //
    YEARLY_AVG("YearlyAvg", "Yearly Average", Duration.ofDays(365)), //
    YEARLY_MIN("YearlyMin", "Yearly Minimum", Duration.ofDays(365)), //
    YEARLY_MAX("YearlyMax", "Yearly Maximum", Duration.ofDays(365)); //

    private String description;

    public String getDescription() {
	return description;
    }

    private String name;
    private Duration duration;

    public String getName() {
	return name;
    }

    public Duration getDuration() {
	return duration;
    }

    SavaHISFrequency(String name, String description, Duration duration) {
	this.name = name;
	this.description = description;
	this.duration = duration;
    }

    public static SavaHISFrequency decode(String string) {
	Integer max = null;
	SavaHISFrequency ret = null;
	for (SavaHISFrequency freq : SavaHISFrequency.values()) {
	    if (string.contains(freq.getName())) {
		if (max == null || max < freq.getName().length()) {
		    max = freq.getName().length();
		    ret = freq;
		}
	    }
	}
	return ret;
    }

}
