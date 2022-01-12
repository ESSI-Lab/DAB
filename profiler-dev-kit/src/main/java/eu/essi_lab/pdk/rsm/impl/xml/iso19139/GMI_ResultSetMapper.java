package eu.essi_lab.pdk.rsm.impl.xml.iso19139;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.MappingSchema;
public class GMI_ResultSetMapper extends GMD_ResultSetMapper {

    /**
     * The schema uri of {@link #GMI_MAPPING_SCHEMA}
     */
    public static final String GMI_SCHEMA_URI = CommonNameSpaceContext.GMI_NS_URI;

    /**
     * The schema name of {@link #GMI_MAPPING_SCHEMA}
     */
    public static final String GMI_SCHEMA_NAME = "GMI";

    /**
     * The schema version of {@link #GMI_MAPPING_SCHEMA}
     */
    public static final String GMI_SCHEMA_VERSION = "1.0";

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema GMI_MAPPING_SCHEMA = new MappingSchema();

    static {
	GMI_MAPPING_SCHEMA.setUri(GMI_SCHEMA_URI);
	GMI_MAPPING_SCHEMA.setName(GMI_SCHEMA_NAME);
	GMI_MAPPING_SCHEMA.setVersion(GMI_SCHEMA_VERSION);
    }

    public GMI_ResultSetMapper(ElementSetType setType) {
	super(setType);
    }

    public GMI_ResultSetMapper(List<QName> elementNames) {
	super(elementNames);
    }

    public GMI_ResultSetMapper() {
	super();
    }

    protected String getTargetNamespace() {

	return CommonNameSpaceContext.GMI_NS_URI;
    }

    protected String getCoreMetadata(GSResource res) throws JAXBException, UnsupportedEncodingException {

	CoreMetadata coreMetadata = res.getHarmonizedMetadata().getCoreMetadata();
	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	return miMetadata.asString(true);
    }

    @Override
    public MappingSchema getMappingSchema() {

	return GMI_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
