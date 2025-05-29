package eu.essi_lab.profiler.wof.discovery.sites;

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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.cuahsi.waterml._1.SeriesCatalogType;
import org.cuahsi.waterml._1.SiteInfoResponseType;
import org.cuahsi.waterml._1.SiteInfoResponseType.Site;

import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.profiler.wof.HydroServerProfiler;
import eu.essi_lab.profiler.wof.WOFRequest;
import eu.essi_lab.profiler.wof.WOFResultSetFormatter;

/**
 * @author boldrini
 */
public abstract class SitesResultSetFormatter extends WOFResultSetFormatter<Site> {

    /**
     * The encoding name of {@link #HYDRO_SERVER_SITES_FORMATTING_ENCODING}
     */
    public static final String HYDRO_SERVER_SITES_ENCODING = "HYDRO_SERVER_SITES_ENCODING";
    /**
     * The encoding version of {@link #HYDRO_SERVER_SITES_FORMATTING_ENCODING}
     */
    public static final String HYDRO_SERVER_SITES_ENCODING_VERSION = "1.1.0";

    /**
     * The {@link FormattingEncoding} of this formatter
     */
    public static final FormattingEncoding HYDRO_SERVER_SITES_FORMATTING_ENCODING = new FormattingEncoding();
    static {
	HYDRO_SERVER_SITES_FORMATTING_ENCODING.setEncoding(HYDRO_SERVER_SITES_ENCODING);
	HYDRO_SERVER_SITES_FORMATTING_ENCODING.setEncodingVersion(HYDRO_SERVER_SITES_ENCODING_VERSION);
	HYDRO_SERVER_SITES_FORMATTING_ENCODING.setMediaType(MediaType.TEXT_XML_TYPE);
    }

    private static final String HYDRO_SERVER_SITES_FORMATTER_ERROR = "HYDRO_SERVER_SITES_FORMATTER_ERROR";

    @Override
    public FormattingEncoding getEncoding() {

	return HYDRO_SERVER_SITES_FORMATTING_ENCODING;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected JAXBElement getResult(String url, WOFRequest request, ResultSet<Site> mappedResultSet) throws Exception {
	SiteInfoResponseType sirt = new SiteInfoResponseType();
	sirt.setQueryInfo(HydroServerProfiler.getQueryInfo(url, request));

	HashMap<String, Site> siteMap = new LinkedHashMap<>();
	List<Site> siteRecords = mappedResultSet.getResultsList();

	for (Site siteRecord : siteRecords) {
	    String siteCode = siteRecord.getSiteInfo().getSiteCode().get(0).getValue();
	    Site site = siteMap.get(siteCode);
	    if (site == null) {
		siteMap.put(siteCode, siteRecord);
	    } else {
		List<SeriesCatalogType> catalogs = site.getSeriesCatalog();
		if (catalogs.isEmpty()) {
		    site.getSeriesCatalog().addAll(siteRecord.getSeriesCatalog());
		} else {
		    if (!siteRecord.getSeriesCatalog().isEmpty()) {
			site.getSeriesCatalog().get(0).getSeries().addAll(siteRecord.getSeriesCatalog().get(0).getSeries());
		    }
		}
	    }
	}
	sirt.getSite().addAll(siteMap.values());

	String wmlNS = "http://www.cuahsi.org/waterML/1.1/";
	JAXBElement<SiteInfoResponseType> jaxbElement = new JAXBElement<SiteInfoResponseType>(new QName(wmlNS, "sitesResponse"),
		SiteInfoResponseType.class, sirt);
	return jaxbElement;
    }
}
