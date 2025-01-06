package eu.essi_lab.downloader.wcs;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.accessor.wcs_1_0_0_TDS.WCSConnector_100_TDS;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class WCSDownloader_100_TDS extends WCSDownloader_100 {

    @Override
    public WCSConnector createConnector() {
	return new WCSConnector_100_TDS();
    }

    public WCSConnector_100_TDS getConnector() {
	return (WCSConnector_100_TDS) connector;
    }

    protected void setConnector(WCSConnector_100_TDS connector) {
	this.connector = connector;
    }

    @Override
    public boolean canDownload() {

	NetProtocol protocol = NetProtocols.decodeFromIdentifier(online.getProtocol());

	return NetProtocols.WCS_1_0_0_TDS.equals(protocol);

    }

    @Override
    protected SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> fixUserLowerAndUpperCorners(
	    SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> userLowerAndUpperCorners,
	    SimpleEntry<Double, Double> userSpatialResolutions) {
	return userLowerAndUpperCorners;
    }

    protected void reduceDescriptor(DataDescriptor desc) {

	List<DataDimension> spatialDimensions = desc.getSpatialDimensions();
	DataDimension dimension1 = spatialDimensions.get(0);
	DataDimension dimension2 = spatialDimensions.get(1);

	double lower1 = dimension1.getContinueDimension().getLower().doubleValue();
	double res1 = dimension1.getContinueDimension().getResolution().doubleValue();
	dimension1.getContinueDimension().setLower(lower1);
	dimension1.getContinueDimension().setUpper(lower1 + res1 * 10);
	dimension1.getContinueDimension().setLowerTolerance(res1 * 2.0);
	dimension1.getContinueDimension().setUpperTolerance(res1 * 2.0);
	dimension1.getContinueDimension().setResolution(null);
	dimension1.getContinueDimension().setResolutionTolerance(null);
	dimension1.getContinueDimension().setSize(11l);
	double lower2 = dimension2.getContinueDimension().getLower().doubleValue();
	double res2 = dimension2.getContinueDimension().getResolution().doubleValue();
	dimension2.getContinueDimension().setLower(lower2);
	dimension2.getContinueDimension().setUpper(lower2 + res2 * 10);
	dimension2.getContinueDimension().setLowerTolerance(res2 * 2.0);
	dimension2.getContinueDimension().setUpperTolerance(res2 * 2.0);
	dimension2.getContinueDimension().setResolution(null);
	dimension2.getContinueDimension().setResolutionTolerance(null);
	dimension2.getContinueDimension().setSize(11l);

	DataDimension temporalDimension = desc.getTemporalDimension();
	if (temporalDimension != null) {
	    reduceDimensionToPoint(temporalDimension);
	}

	List<DataDimension> otherDimensions = desc.getOtherDimensions();
	for (DataDimension dataDimension : otherDimensions) {
	    reduceDimensionToPoint(dataDimension);
	}

    }
}
