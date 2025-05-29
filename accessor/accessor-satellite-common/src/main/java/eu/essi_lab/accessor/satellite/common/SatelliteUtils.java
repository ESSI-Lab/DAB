/**
 * 
 */
package eu.essi_lab.accessor.satellite.common;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.RankingStrategy;
import eu.essi_lab.model.resource.SatelliteScene;
import eu.essi_lab.ommdk.GMDResourceMapper;

/**
 * @author Fabrizio
 */
public class SatelliteUtils {

    private static final InputStream PRISMA = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/prisma.xml");
    private static final InputStream LANDSAT_8 = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/landsat8.xml");
    
    private static final InputStream SENTINEL = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/sentinel.xml");
    private static final InputStream SENTINEL_1A = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/sentinel1a.xml");
    private static final InputStream SENTINEL_1B = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/sentinel1b.xml");
    private static final InputStream SENTINEL_2A = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/sentinel2a.xml");
    private static final InputStream SENTINEL_2B = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/sentinel2b.xml");
    private static final InputStream SENTINEL_3A = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/sentinel3a.xml");

    private static final InputStream CHINA_GEOSS = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/china-geoss.xml");
    private static final InputStream CHINA_GEOSS_CSES1 = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/cses1.xml");
    private static final InputStream CHINA_GEOSS_CBERS1 = SatelliteUtils.class.getClassLoader()
	    .getResourceAsStream("collections/cbers1.xml");
    private static final InputStream CHINA_GEOSS_CBERS2 = SatelliteUtils.class.getClassLoader()
	    .getResourceAsStream("collections/cbers2.xml");
    private static final InputStream CHINA_GEOSS_CBERS2B = SatelliteUtils.class.getClassLoader()
	    .getResourceAsStream("collections/cbers2b.xml");
    private static final InputStream CHINA_GEOSS_FY2G = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/fy2g.xml");
    private static final InputStream CHINA_GEOSS_FY2H = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/fy2h.xml");
    private static final InputStream CHINA_GEOSS_FY3A = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/fy3a.xml");
    private static final InputStream CHINA_GEOSS_FY3B = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/fy3b.xml");
    private static final InputStream CHINA_GEOSS_FY3C = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/fy3c.xml");
    private static final InputStream CHINA_GEOSS_FY3D = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/fy3d.xml");
    private static final InputStream CHINA_GEOSS_GF1 = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/gf1.xml");
    private static final InputStream CHINA_GEOSS_GLASS = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/glass.xml");
    private static final InputStream CHINA_GEOSS_HJ1A = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/hj1a.xml");
    private static final InputStream CHINA_GEOSS_HJ1B = SatelliteUtils.class.getClassLoader().getResourceAsStream("collections/hj1b.xml");
    private static final InputStream CHINA_GEOSS_TANSAT = SatelliteUtils.class.getClassLoader()
	    .getResourceAsStream("collections/tansat.xml");
    private static final InputStream CHINA_GEOSS_GEOARC = SatelliteUtils.class.getClassLoader()
	    .getResourceAsStream("collections/geoarc.xml");
    private static final InputStream CHINA_GEOSS_GEODOI = SatelliteUtils.class.getClassLoader()
	    .getResourceAsStream("collections/geodoi.xml");
    private static final InputStream CHINA_GEOSS_HY1C = SatelliteUtils.class.getClassLoader()
	    .getResourceAsStream("collections/hy1c.xml");
    private static final InputStream CHINA_GEOSS_JL1GP01 = SatelliteUtils.class.getClassLoader()
	    .getResourceAsStream("collections/jl1gp01.xml");
    private static final InputStream CHINA_GEOSS_JL1GP02 = SatelliteUtils.class.getClassLoader()
	    .getResourceAsStream("collections/jl1gp02.xml");

    private static ClonableInputStream l8Stream;
    private static ClonableInputStream prismaStream;
    
    private static ClonableInputStream sStream;
    private static ClonableInputStream s1aStream;
    private static ClonableInputStream s1bStream;
    private static ClonableInputStream s2aStream;
    private static ClonableInputStream s2bStream;
    private static ClonableInputStream s3aStream;

    private static ClonableInputStream chinaGeossStream;
    private static ClonableInputStream cses1Stream;
    private static ClonableInputStream cbers1Stream;
    private static ClonableInputStream cbers2Stream;
    private static ClonableInputStream cbers2bStream;
    private static ClonableInputStream fy3aStream;
    private static ClonableInputStream fy2gStream;
    private static ClonableInputStream fy2hStream;
    private static ClonableInputStream fy3bStream;
    private static ClonableInputStream fy3cStream;
    private static ClonableInputStream fy3dStream;
    private static ClonableInputStream gf1Stream;
    private static ClonableInputStream glassStream;
    private static ClonableInputStream hj1aStream;
    private static ClonableInputStream hj1bStream;
    private static ClonableInputStream tansatStream;
    private static ClonableInputStream geoarcStream;
    private static ClonableInputStream geodoiStream;
    private static ClonableInputStream jl1gp01Stream;
    private static ClonableInputStream jl1gp02Stream;
    private static ClonableInputStream hy1cStream;

    static {

	try {
	    prismaStream = new ClonableInputStream(PRISMA);
	    l8Stream = new ClonableInputStream(LANDSAT_8);
	 
	    
	    sStream = new ClonableInputStream(SENTINEL);
	    s1aStream = new ClonableInputStream(SENTINEL_1A);
	    s1bStream = new ClonableInputStream(SENTINEL_1B);
	    s2aStream = new ClonableInputStream(SENTINEL_2A);
	    s2bStream = new ClonableInputStream(SENTINEL_2B);
	    s3aStream = new ClonableInputStream(SENTINEL_3A);

	    chinaGeossStream = new ClonableInputStream(CHINA_GEOSS);
	    cses1Stream = new ClonableInputStream(CHINA_GEOSS_CSES1);
	    cbers1Stream = new ClonableInputStream(CHINA_GEOSS_CBERS1);
	    cbers2Stream = new ClonableInputStream(CHINA_GEOSS_CBERS2);
	    cbers2bStream = new ClonableInputStream(CHINA_GEOSS_CBERS2B);
	    fy2gStream = new ClonableInputStream(CHINA_GEOSS_FY2G);
	    fy2hStream = new ClonableInputStream(CHINA_GEOSS_FY2H);
	    fy3aStream = new ClonableInputStream(CHINA_GEOSS_FY3A);
	    fy3bStream = new ClonableInputStream(CHINA_GEOSS_FY3B);
	    fy3cStream = new ClonableInputStream(CHINA_GEOSS_FY3C);
	    fy3dStream = new ClonableInputStream(CHINA_GEOSS_FY3D);
	    gf1Stream = new ClonableInputStream(CHINA_GEOSS_GF1);
	    glassStream = new ClonableInputStream(CHINA_GEOSS_GLASS);
	    hj1aStream = new ClonableInputStream(CHINA_GEOSS_HJ1A);
	    hj1bStream = new ClonableInputStream(CHINA_GEOSS_HJ1B);
	    tansatStream = new ClonableInputStream(CHINA_GEOSS_TANSAT);
	    geoarcStream = new ClonableInputStream(CHINA_GEOSS_GEOARC);
	    geodoiStream = new ClonableInputStream(CHINA_GEOSS_GEODOI);
	    hy1cStream = new ClonableInputStream(CHINA_GEOSS_HY1C);
	    jl1gp01Stream = new ClonableInputStream(CHINA_GEOSS_JL1GP01);
	    jl1gp02Stream = new ClonableInputStream(CHINA_GEOSS_JL1GP02);

	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(SatelliteUtils.class).error(e.getMessage(), e);
	}
    }

    /**
     * @param landsatSource
     * @param setIndexes
     * @return
     * @throws Exception
     */
    public static GSResource getLandsatCollection(GSSource landsatSource, boolean setIndexes) throws Exception {

	return SatelliteUtils.mapCollection(l8Stream.clone(), landsatSource, true, setIndexes);
    }

    /**
     * @param prismaSource
     * @param setIndexes
     * @return
     * @throws Exception
     */
    public static GSResource getPrismaCollection(GSSource prismaSource, boolean setIndexes) throws Exception {

	return SatelliteUtils.mapCollection(prismaStream.clone(), prismaSource, true, setIndexes);
    }

    /**
     * @param sentinelSource
     * @param setIndexes
     * @return
     * @throws Exception
     */
    public static List<GSResource> getSentinelCollections(GSSource sentinelSource, boolean setIndexes) throws Exception {

	//
	// root collection with highest ranking
	//	
	GSResource sentinelCollection = SatelliteUtils.mapCollection(sStream.clone(), sentinelSource, true, setIndexes);
	sentinelCollection.getPropertyHandler().setMetadataQuality(RankingStrategy.MAX_VARIABLE_VALUE);
	sentinelCollection.getPropertyHandler().setAccessQuality(RankingStrategy.MAX_VARIABLE_VALUE);
	
	//
	// other collections with lowest ranking
	//	
	GSResource sentinel1aCollection = SatelliteUtils.mapCollection(s1aStream.clone(), sentinelSource, true, setIndexes);
	sentinel1aCollection.getPropertyHandler().setLowestRanking();
	
	GSResource sentinel1bCollection = SatelliteUtils.mapCollection(s1bStream.clone(), sentinelSource, true, setIndexes);
	sentinel1bCollection.getPropertyHandler().setLowestRanking();

	GSResource sentinel2aCollection = SatelliteUtils.mapCollection(s2aStream.clone(), sentinelSource, true, setIndexes);
	sentinel2aCollection.getPropertyHandler().setLowestRanking();

	GSResource sentinel2bCollection = SatelliteUtils.mapCollection(s2bStream.clone(), sentinelSource, true, setIndexes);
	sentinel2bCollection.getPropertyHandler().setLowestRanking();

	GSResource sentinel3aCollection = SatelliteUtils.mapCollection(s3aStream.clone(), sentinelSource, true, setIndexes);
	sentinel3aCollection.getPropertyHandler().setLowestRanking();

	return Arrays.asList(//
		sentinelCollection, //
		sentinel1aCollection, //
		sentinel1bCollection, //
		sentinel2aCollection, //
		sentinel2bCollection, //
		sentinel3aCollection);
    }

    /**
     * @param chinaGeossSource
     * @param setIndexes
     * @return
     * @throws Exception
     */
    public static List<GSResource> getChinaGeossCollections(GSSource chinaGeossSource, boolean setIndexes) throws Exception {


	//
	// root collection with highest ranking
	//
	GSResource chinaGeossCollection = SatelliteUtils.mapCollection(chinaGeossStream.clone(), chinaGeossSource, true, setIndexes);
	chinaGeossCollection.getPropertyHandler().setMetadataQuality(RankingStrategy.MAX_VARIABLE_VALUE);
	chinaGeossCollection.getPropertyHandler().setAccessQuality(RankingStrategy.MAX_VARIABLE_VALUE);
	
	//
	// other collections with lowest ranking
	//
	GSResource cses1Collection = SatelliteUtils.mapCollection(cses1Stream.clone(), chinaGeossSource, true, setIndexes);
	cses1Collection.getPropertyHandler().setLowestRanking();
		
	GSResource cbers1Collection = SatelliteUtils.mapCollection(cbers1Stream.clone(), chinaGeossSource, true, setIndexes);
	cbers1Collection.getPropertyHandler().setLowestRanking();
	
	GSResource cbers2Collection = SatelliteUtils.mapCollection(cbers2Stream.clone(), chinaGeossSource, true, setIndexes);
	cbers2Collection.getPropertyHandler().setLowestRanking();
	
	GSResource cbers2bCollection = SatelliteUtils.mapCollection(cbers2bStream.clone(), chinaGeossSource, true, setIndexes);
	cbers2bCollection.getPropertyHandler().setLowestRanking();
	
	GSResource fy2gCollection = SatelliteUtils.mapCollection(fy2gStream.clone(), chinaGeossSource, true, setIndexes);	
	fy2gCollection.getPropertyHandler().setLowestRanking();

	GSResource fy2hCollection = SatelliteUtils.mapCollection(fy2hStream.clone(), chinaGeossSource, true, setIndexes);
	fy2hCollection.getPropertyHandler().setLowestRanking();

	GSResource fy3aCollection = SatelliteUtils.mapCollection(fy3aStream.clone(), chinaGeossSource, true, setIndexes);
	fy3aCollection.getPropertyHandler().setLowestRanking();

	GSResource fy3bCollection = SatelliteUtils.mapCollection(fy3bStream.clone(), chinaGeossSource, true, setIndexes);
	fy3bCollection.getPropertyHandler().setLowestRanking();

	GSResource fy3cCollection = SatelliteUtils.mapCollection(fy3cStream.clone(), chinaGeossSource, true, setIndexes);
	fy3cCollection.getPropertyHandler().setLowestRanking();

	GSResource fy3dCollection = SatelliteUtils.mapCollection(fy3dStream.clone(), chinaGeossSource, true, setIndexes);
	fy3dCollection.getPropertyHandler().setLowestRanking();

	GSResource gf1Collection = SatelliteUtils.mapCollection(gf1Stream.clone(), chinaGeossSource, true, setIndexes);
	gf1Collection.getPropertyHandler().setLowestRanking();

	GSResource glassCollection = SatelliteUtils.mapCollection(glassStream.clone(), chinaGeossSource, true, setIndexes);
	glassCollection.getPropertyHandler().setLowestRanking();

	GSResource hj1aCollection = SatelliteUtils.mapCollection(hj1aStream.clone(), chinaGeossSource, true, setIndexes);
	hj1aCollection.getPropertyHandler().setLowestRanking();

	GSResource hj1bCollection = SatelliteUtils.mapCollection(hj1bStream.clone(), chinaGeossSource, true, setIndexes);
	hj1bCollection.getPropertyHandler().setLowestRanking();

	GSResource tansatCollection = SatelliteUtils.mapCollection(tansatStream.clone(), chinaGeossSource, true, setIndexes);
	tansatCollection.getPropertyHandler().setLowestRanking();

	GSResource geoarcCollection = SatelliteUtils.mapCollection(geoarcStream.clone(), chinaGeossSource, true, setIndexes);
	geoarcCollection.getPropertyHandler().setLowestRanking();
	
	GSResource geodoiCollection = SatelliteUtils.mapCollection(geodoiStream.clone(), chinaGeossSource, true, setIndexes);
	geodoiCollection.getPropertyHandler().setLowestRanking();
	
	GSResource hy1cCollection = SatelliteUtils.mapCollection(hy1cStream.clone(), chinaGeossSource, true, setIndexes);
	geodoiCollection.getPropertyHandler().setLowestRanking();
	
	GSResource jl1gp01Collection = SatelliteUtils.mapCollection(jl1gp01Stream.clone(), chinaGeossSource, true, setIndexes);
	geodoiCollection.getPropertyHandler().setLowestRanking();
	
	GSResource jl1gp02Collection = SatelliteUtils.mapCollection(jl1gp02Stream.clone(), chinaGeossSource, true, setIndexes);
	geodoiCollection.getPropertyHandler().setLowestRanking();

	return Arrays.asList(//
		chinaGeossCollection, //
		cses1Collection, //
		cbers1Collection, //
		cbers2Collection, //
		cbers2bCollection, //
		fy2gCollection, //
		fy2hCollection, //
		fy3aCollection, //
		fy3bCollection, //
		fy3cCollection, //
		fy3dCollection, //
		gf1Collection, //
		glassCollection, //
		hj1aCollection, //
		hj1bCollection, //
		tansatCollection, //
		geoarcCollection,
		geodoiCollection,
		hy1cCollection,
		jl1gp01Collection,
		jl1gp02Collection);

    }

    /**
     * @param collection
     * @param source
     * @param gdc
     * @param setIndexes
     * @return
     * @throws Exception
     */
    private static GSResource mapCollection(InputStream collection, GSSource source, boolean gdc, boolean setIndexes) throws Exception {

	GMDResourceMapper resourceMapper = new GMDResourceMapper();

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(IOStreamUtils.asUTF8String(collection));
	originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);

	GSResource gsCollection = resourceMapper.map(originalMetadata, source);

	if (gdc) {
	    gsCollection.getPropertyHandler().setIsGDC(true);
	}

	String queryables = "prodType";
	String origin = "none";

	String publicId = gsCollection.getPublicId();

	if (publicId.equals("USGS_LANDSAT_8")) {

	    queryables += ",cloudcp,";
	    queryables += "pubDatefrom,";
	    queryables += "pubDateuntil,";
	    queryables += "row,";
	    queryables += "path";

	    origin = "landsat";

	} else if (publicId.contains("SENTINEL")) {

	    queryables += ",pubDatefrom,";
	    queryables += "pubDateuntil,";

	    if (publicId.contains("1A") || publicId.contains("1B")) {

		queryables += "sensorOpMode,";
		queryables += "sensorSwath,";
		queryables += "relOrbit,";
		queryables += "sarPolCh";

	    } else if (publicId.contains("2A") || publicId.contains("2B")) {

		queryables += "prodType,";
		queryables += "sensorOpMode,";
		queryables += "relOrbit,";
		queryables += "cloudcp";

	    } else { // 3A

		queryables += "prodType,";
		queryables += "s3InstrumentIdx,";
		queryables += "s3ProductLevel,";
		queryables += "s3Timeliness,";
		queryables += "relOrbit";
	    }

	} else if (publicId.contains("ChinaGEO")) {

	    //
	    // only product type
	    //
	    origin = "chinaGEOSS";
	} else if (publicId.equals("ASI_PRISMA")) {

	    queryables += ",cloudcp,";
	    queryables += "pubDatefrom,";
	    queryables += "pubDateuntil,";
	    origin = "prisma";
	}

	SatelliteScene satelliteScene = new SatelliteScene();
	satelliteScene.setOrigin(origin);
	satelliteScene.setCollectionQueryables(queryables);

	gsCollection.getExtensionHandler().setSatelliteScene(satelliteScene);

	if (setIndexes) {
	    //
	    // writes indexes
	    //
	    IndexedElementsWriter.write(gsCollection);
	}

	//
	// set the fileIdentifer as private id
	//
	gsCollection.setPrivateId(publicId);

	return gsCollection;
    }

    /**
     * @param sceneId
     * @return
     */
    public static String getSentinelPlatformIndentifier(String sceneId) {

	String platformId = sceneId.substring(1, 3);

	return "satellitescene_collection_prefix_SENTINEL_" + platformId;
    }

    /**
     * @param satelliteID
     * @return
     */
    public static String getChinaGEOSSPlatformIndentifier(String satelliteID) {

	satelliteID = satelliteID.toLowerCase();

	if (satelliteID.contains("jl1gp01")) {

	    return "satellitescene_collection_prefix_ChinaGEO_JL1GP01";
	}
	if (satelliteID.contains("jl1gp02")) {

	    return "satellitescene_collection_prefix_ChinaGEO_JL1GP02";
	}
	if (satelliteID.contains("hy-1c") || satelliteID.contains("hy1c")) {

	    return "satellitescene_collection_prefix_ChinaGEO_HY1C";
	}
	
	if (satelliteID.contains("cses")) {

	    return "satellitescene_collection_prefix_ChinaGEO_CSES-01";
	}

	if (satelliteID.contains("01")) {

	    return "satellitescene_collection_prefix_ChinaGEO_CBERS-01";
	}
	if (satelliteID.contains("02")) {

	    return "satellitescene_collection_prefix_ChinaGEO_CBERS-02";
	}
	if (satelliteID.contains("02b")) {

	    return "satellitescene_collection_prefix_ChinaGEO_CBERS-02B";
	}
	if (satelliteID.contains("fy3a")) {

	    return "satellitescene_collection_prefix_ChinaGEO_FengYun3A";
	}
	if (satelliteID.contains("fy3b")) {

	    return "satellitescene_collection_prefix_ChinaGEO_FengYun3B";
	}
	if (satelliteID.contains("fy3c")) {

	    return "satellitescene_collection_prefix_ChinaGEO_FengYun3C";
	}
	if (satelliteID.contains("fy3d")) {

	    return "satellitescene_collection_prefix_ChinaGEO_FengYun3D";
	}
	if (satelliteID.contains("fy2g")) {

	    return "satellitescene_collection_prefix_ChinaGEO_FengYun2G";
	}
	if (satelliteID.contains("fy2h")) {

	    return "satellitescene_collection_prefix_ChinaGEO_FengYun2H";
	}
	if (satelliteID.contains("gf1")) {

	    return "satellitescene_collection_prefix_ChinaGEO_GF1";
	}
	if (satelliteID.contains("glass")) {

	    return "satellitescene_collection_prefix_ChinaGEO_GlobalLandSurfaceSatellite";
	}
	if (satelliteID.contains("hj1a")) {

	    return "satellitescene_collection_prefix_ChinaGEO_HJ1A";
	}
	if (satelliteID.contains("hj1b")) {

	    return "satellitescene_collection_prefix_ChinaGEO_HJ1B";
	}
	if (satelliteID.contains("tansat")) {

	    return "satellitescene_collection_prefix_ChinaGEO_TanSat";
	}
	if (satelliteID.contains("geoarc")) {

	    return "satellitescene_collection_prefix_ChinaGEO_GEOARC";
	}
	if (satelliteID.contains("geodoi")) {

	    return "satellitescene_collection_prefix_ChinaGEO_GEODOI";
	}

	return null;
    }

    /**
     * @param size
     * @return
     */
    public static String toMegaBytes(String size) {

	try {
	    String[] splitted = size.split(" ");

	    Double val = Double.valueOf(splitted[0]);

	    if (splitted[1].toLowerCase().contains("tb")) {
		return "" + (val * 1024 * 1024);
	    }

	    if (splitted[1].toLowerCase().contains("gb")) {
		return "" + (val * 1024);
	    }

	    if (splitted[1].toLowerCase().contains("mb")) {
		return "" + (val);
	    }

	    if (splitted[1].toLowerCase().contains("kb")) {
		return "" + (val / 1024.0);
	    }
	} catch (Throwable thr) {
	}

	return "0";
    }

    /**
     * @param polygon
     * @param firstIsLat
     * @return
     */
    public static String toBBOX(String polygon, boolean firstIsLat) {

	String[] coords = polygon.split(" ");

	Double[] lats = new Double[coords.length / 2];
	Double[] lons = new Double[coords.length / 2];

	for (int i = 0; i < coords.length; i++) {

	    if (i % 2 == 0) {

		if (firstIsLat) {
		    lats[i / 2] = Double.valueOf(coords[i]);
		} else {
		    lons[i / 2] = Double.valueOf(coords[i]);
		}
	    } else {

		if (!firstIsLat) {
		    lats[i / 2] = Double.valueOf(coords[i]);
		} else {
		    lons[i / 2] = Double.valueOf(coords[i]);
		}
	    }
	}

	Double minLat = lats[0];
	Double maxLat = lats[0];
	Double minLon = lons[0];
	Double maxLon = lons[0];

	for (int i = 1; i < lats.length; i++) {

	    if (lats[i] < minLat) {
		minLat = lats[i];
	    }
	    if (lats[i] > maxLat) {
		maxLat = lats[i];
	    }

	    if (lons[i] < minLon) {
		minLon = lons[i];
	    }
	    if (lons[i] > maxLon) {
		maxLon = lons[i];
	    }
	}

	return minLat + " " + minLon + " " + maxLat + " " + maxLon;
    }

    /**
     * @param collection
     * @param source
     * @return
     * @throws Exception
     */
    private static GSResource mapCollection(InputStream collection, GSSource source, boolean gdc) throws Exception {

	return mapCollection(collection, source, gdc, true);
    }
}
