package eu.essi_lab.accessor.ittrentinofloods;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class TrentinoFloodsClientTest {

    static final String REGISTRY = "IdSensore,Descrizione,Tipologia,Latitudine WGS,Logitudine WGS,X WGS,Y WGS,Bacino,Corpo Idrico,Url\r\n"
	    + "5,Borgo Valsugana,Idrometro-Livello (m),46.054072,11.460107,690304,5102998,Brenta,fiume Brenta,"
	    + "http://www.floods.it/public/Opendata/ODdownload.php?Dataset=1&Sensore=5\r\n"
	    + "12,Trento-Fersina,Idrometro-Portata (mc/s),46.043983,11.117988,663869,5101115,Adige\\Fersina,torr. Fersina,"
	    + "http://www.floods.it/public/Opendata/ODdownload.php?Dataset=2&Sensore=12\r\n"
	    + "2,Trento-Fersina,Idrometro-Livello (m),46.043983,11.117988,663869,5101115,Adige\\Fersina,torr. Fersina,"
	    + "http://www.floods.it/public/Opendata/ODdownload.php?Dataset=1&Sensore=2\r\n"
	    + "99,Unknown,Pluviometro (mm),46.0,11.0,0,0,Adige,,\r\n";

    static final String DATA = "IdSensore,Data/Ora,Valore,Validato,OraSolare\r\n" //
	    + "5,04/01/2024 00:00,,NO,SI\r\n" //
	    + "5,04/01/2024 01:00,0.23,NO,SI\r\n" //
	    + "5,04/01/2024 02:00,n.d.,NO,SI\r\n" //
	    + "5,04/01/2024 03:00,0.24,NO,SI\r\n" //
	    + "5,15/07/2024 12:00,0.30,SI,NO\r\n";

    /**
     * @return
     */
    static List<TrentinoFloodsEntity> createSeries() {

	return TrentinoFloodsClient.parseRegistry(REGISTRY);
    }

    @Test
    public void registryTest() {

	List<TrentinoFloodsEntity> series = createSeries();

	// the rain gauge is discarded, sorted by sensor
	assertEquals(3, series.size());
	assertEquals("2", series.get(0).getSensorId());
	assertEquals("5", series.get(1).getSensorId());
	assertEquals("12", series.get(2).getSensorId());

	TrentinoFloodsEntity discharge = series.get(2);
	assertEquals("2|12", discharge.getSeriesId());
	assertEquals(TrentinoFloodsProperty.DISCHARGE, discharge.getProperty().get());
	assertEquals("Trento-Fersina", discharge.getStationName());
	assertEquals("torr. Fersina", discharge.getString(TrentinoFloodsClient.WATERBODY));
	assertEquals("Adige\\Fersina", discharge.getString(TrentinoFloodsClient.BASIN));
	assertEquals(new BigDecimal("46.043983"), discharge.getObject().getBigDecimal(TrentinoFloodsClient.LAT));
	assertEquals(new BigDecimal("11.117988"), discharge.getObject().getBigDecimal(TrentinoFloodsClient.LON));
    }

    @Test
    public void datasetTest() {

	assertEquals("1", TrentinoFloodsClient.getDataset("http://www.floods.it/public/Opendata/ODdownload.php?Dataset=1&Sensore=5", ""));
	assertEquals("2", TrentinoFloodsClient.getDataset("", "Idrometro-Portata (mc/s)"));
	assertEquals("1", TrentinoFloodsClient.getDataset("", "Idrometro-Livello (m)"));
	assertEquals("", TrentinoFloodsClient.getDataset("", "Pluviometro (mm)"));
    }

    @Test
    public void dataTest() {

	TrentinoFloodsClient.SensorData data = TrentinoFloodsClient.parseData(DATA);
	TreeMap<Date, BigDecimal> values = data.values;

	// missing and not numeric values are skipped
	assertEquals(3, values.size());
	assertFalse(data.validated);

	// solar time is UTC+1
	assertEquals("2024-01-04T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(values.firstKey()));
	assertEquals(new BigDecimal("0.23"), values.firstEntry().getValue());

	// summer time is UTC+2
	assertEquals("2024-07-15T10:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(values.lastKey()));
    }

    @Test
    public void seriesIdTest() {

	TrentinoFloodsEntity series = TrentinoFloodsClient.parseSeriesId("1|5");
	assertEquals("1", series.getDataset());
	assertEquals("5", series.getSensorId());
	assertEquals(TrentinoFloodsProperty.LEVEL, series.getProperty().get());

	assertNull(TrentinoFloodsClient.parseSeriesId("3|5"));
	assertNull(TrentinoFloodsClient.parseSeriesId("1|"));
	assertNull(TrentinoFloodsClient.parseSeriesId("15"));
	assertNull(TrentinoFloodsClient.parseSeriesId(null));
    }

    @Test
    public void endpointTest() {

	assertEquals(TrentinoFloodsClient.DEFAULT_ENDPOINT, TrentinoFloodsClient.normalizeEndpoint("https://www.floods.it/public/Opendata"));
	assertEquals(TrentinoFloodsClient.DEFAULT_ENDPOINT,
		TrentinoFloodsClient.normalizeEndpoint("https://www.floods.it/public/Opendata/ODdownload.php?Dataset=0"));
	assertEquals(TrentinoFloodsClient.DEFAULT_ENDPOINT, TrentinoFloodsClient.normalizeEndpoint(null));
    }

    @Test
    public void decodeTest() {

	assertEquals("Malè", TrentinoFloodsClient.decode("Malè".getBytes(Charset.forName("windows-1252"))));
	assertEquals("Malè", TrentinoFloodsClient.decode("Malè".getBytes(Charset.forName("UTF-8"))));
	assertTrue(TrentinoFloodsClient.decode("Il file non esiste!".getBytes()).startsWith(TrentinoFloodsClient.NOT_EXISTING));
    }
}
