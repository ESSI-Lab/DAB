package eu.essi_lab.gssrv.conf.task.bluecloud;

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

import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.lib.utils.ExpiringCache;

public class ReportResult {

    public static final Integer MAX = 30;

    private int count = 0;

    private int total ;
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    private ExpiringCache<String> cache = new ExpiringCache<>();

    public ReportResult() {
	cache.setDuration(TimeUnit.DAYS.toMillis(10));
	cache.setMaxSize(MAX);
    }

    public int getCount() {
	return count;
    }

    public void setCount(int count) {
	this.count = count;
    }

    public ExpiringCache<String> getCache() {
	return cache;
    }

    public void setCache(ExpiringCache<String> cache) {
	this.cache = cache;
    }

    public void addValue(List<String> values) {
	for (String value : values) {
	    this.cache.put(value, value);   
	}	
	this.count++;
	
    }

}
