package eu.essi_lab.access.augmenter;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Comparator;

import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;

public class DescriptorComparator implements Comparator<DataDescriptor> {

    @Override
    public int compare(DataDescriptor d1, DataDescriptor d2) {

	CRS c1 = d1.getCRS();
	CRS c2 = d2.getCRS();

	int ret = 0;

	if (c1 != null) {
	    ret = ret - 1;
	    if (c1.equals(CRS.EPSG_4326())) {
		ret = ret - 20;
	    }
	    if (c1.equals(CRS.OGC_84())) {
		ret = ret - 10;
	    }
	}
	if (c2 != null) {
	    ret = ret + 1;
	    if (c2.equals(CRS.EPSG_4326())) {
		ret = ret + 20;
	    }
	    if (c2.equals(CRS.OGC_84())) {
		ret = ret + 10;
	    }
	}

	DataFormat f1 = d1.getDataFormat();
	DataFormat f2 = d2.getDataFormat();

	if (f1 != null) {
	    ret = ret - 1;
	    if (f1.isSubTypeOf(DataFormat.NETCDF()) || f1.equals(DataFormat.NETCDF())) {
		ret = ret - 2;
	    }
	    if (f1.equals(DataFormat.IMAGE_GEOTIFF())) {
		ret = ret - 1;
	    }
	    if (f1.equals(DataFormat.WATERML_1_1())) {
		ret = ret - 2;
	    }
	    if (f1.equals(DataFormat.WATERML_2_0())) {
		ret = ret - 2;
	    }
	}
	if (f2 != null) {
	    ret = ret + 1;
	    if (f2.isSubTypeOf(DataFormat.NETCDF()) || f2.equals(DataFormat.NETCDF())) {
		ret = ret + 2;
	    }
	    if (f2.equals(DataFormat.IMAGE_GEOTIFF())) {
		ret = ret + 1;
	    }
	    if (f2.equals(DataFormat.WATERML_1_1())) {
		ret = ret + 2;
	    }
	    if (f2.equals(DataFormat.WATERML_2_0())) {
		ret = ret + 2;
	    }
	}

	return ret;

    }

}
