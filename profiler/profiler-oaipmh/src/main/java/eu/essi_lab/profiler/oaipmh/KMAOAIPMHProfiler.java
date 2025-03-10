/**
 * 
 */
package eu.essi_lab.profiler.oaipmh;

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

import eu.essi_lab.profiler.oaipmh.handler.discover.OAIPMHRequestTransformer;
import eu.essi_lab.profiler.oaipmh.handler.discover.OAIPMHResultSetMapper;
import eu.essi_lab.profiler.oaipmh.handler.kma.discover.KMAOAIPMHResultSetMapper;
import eu.essi_lab.profiler.oaipmh.handler.kma.discover.KMAOAIPMHWebRequestTransformer;
import eu.essi_lab.profiler.oaipmh.handler.kma.srvinfo.KMAOAIPMHListSetsHandler;
import eu.essi_lab.profiler.oaipmh.handler.srvinfo.OAIPMHListSetsHandler;

/**
 * @author Fabrizio
 */
public class KMAOAIPMHProfiler extends OAIPMHProfiler<KMAOAIPMHProfilerSetting> {

    public KMAOAIPMHProfiler() {
    }

    @Override
    protected OAIPMHListSetsHandler getListSetsHandler() {

	return new KMAOAIPMHListSetsHandler();
    }

    @Override
    protected OAIPMHResultSetMapper getResultSetMapper() {

	return new KMAOAIPMHResultSetMapper();
    }

    @Override
    protected OAIPMHRequestTransformer getWebRequestTransformer() {

	return new KMAOAIPMHWebRequestTransformer(getSetting());
    }

    @Override
    protected KMAOAIPMHProfilerSetting initSetting() {

	return new KMAOAIPMHProfilerSetting();
    }
}
