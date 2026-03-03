package eu.essi_lab.accessor.hiscentral.test;

import eu.essi_lab.accessor.hiscentral.lombardia.*;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_FUNZIONE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_OPERATORE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_PERIODO;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_VALIDITY_FLAG;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.wml._2.WML2QualityCategory;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class HISCentralLombardiaClient2ExternalTestIT {

    @Test
    public void test() throws Exception {

	HISCentralLombardiaClient.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));
	URL endpoint = new URL(System.getProperty("HIS-CENTRAL-LOMBARDIA-ENDPOINT"));
	String keystorePassword = System.getProperty("HIS-CENTRAL-LOMBARDIA-KEYSTORE-PASSWORD");
	String username = System.getProperty("HIS-CENTRAL-LOMBARDIA-USERNAME");
	String password = System.getProperty("HIS-CENTRAL-LOMBARDIA-PASSWORD");

	HISCentralLombardiaClient client = new HISCentralLombardiaClient(endpoint, keystorePassword, username, password);

	List<String> stationIdentifiers = client.getStationIdentifiers();
	Collections.sort(stationIdentifiers);
	int count = 0;
	Date start = ISO8601DateTimeUtils.parseISO8601ToDate("2025-01-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2026-01-02T00:00:00Z").get();
	System.out.println("Total stations: " + stationIdentifiers.size());
	List<String> block = new ArrayList<>();
	int blockSize = 5;
	int startFrom = 0;
	List<String>errors = new ArrayList<>();
	for (String stationIdentifier : stationIdentifiers) {
	    count++;
	    System.out.println("Processing station " + count + " of " + stationIdentifiers.size());
	    Stazione station = client.getStazione(stationIdentifier);
	    System.out.println(stationIdentifier + " - " + station.getNome());

	    if (station.getNome().contains("ModAria")){
		continue;
	    }
//	    System.out.println("  stato: " + station.getStato());
//	    System.out.println("  tipo: " + station.getTipoStazione());
//	    System.out.println("  comune: " + station.getComune());
//	    System.out.println("  indirizzo: " + station.getIndirizzo());
//	    System.out.println("  quota: " + station.getQuota());
//	    System.out.println("  UTM: " + station.getUtm32TEst() + " " + station.getUtm32TNord());

	    if (count<startFrom) {
		continue;
	    }
	    block.add(stationIdentifier);
	    if (block.size() < blockSize && count != stationIdentifiers.size() ) {
		continue;
	    }

	    List<String> sensorTypes = new ArrayList<>();
	    sensorTypes.add("2");
	    List<Sensore> sensors;
	    try {
		sensors = client.elencoSensori(block, sensorTypes);
	    }catch (Exception e) {
		errors.add("BLOCK SIZE "+blockSize+" FROM COUNT "+count);
		errors.addAll(block);
		for (String error : errors) {
		    System.err.println("ERROR FOR ID: "+error);
		}
		continue;
	    }
		block.clear();
	    for (Sensore sensor : sensors) {
		System.out.println("    " + sensor.getNome());

		ID_FUNZIONE funzione = sensor.getFunzione();
		ID_OPERATORE operatore = sensor.getOperatore();
		ID_PERIODO periodo = sensor.getPeriodo();

		System.out.println("      " + funzione);
		System.out.println("      " + operatore);
		System.out.println("      " + periodo);

		if (funzione == null || !funzione.equals(ID_FUNZIONE.ID_3_CALCOLATO)) {
		    continue;
		}
		if (operatore == null || !operatore.equals(ID_OPERATORE.ID_4_CUMULATA)) {
		    continue;
		}
		if (periodo == null || !periodo.equals(ID_PERIODO.ID_4_T1D)) {
		    continue;
		}
		String sensorType = sensor.getTipoSensore();
		if (!sensorType.equals("Precipitazione")) {
		    continue;
		}

		if (sensor.getFrom() == null || sensor.getFrom().after(end)) {
		    continue;
		}
		if (sensor.getTo() != null && sensor.getTo().before(start)) {
		    continue;
		}

		System.out.println("      from: " + sensor.getFrom());
		System.out.println("      to: " + sensor.getTo());

		RendiDatiResult dati = client.rendiDati(sensor.getId(), funzione, operatore, periodo, start, end);
		System.out.println("      dati: " + dati.getDati().size());

		File csvDir = new File("/tmp/hiscentral-lombardia-csv");
		csvDir.mkdirs();
		String currentStationId = sensor.getIdStazione();
		Stazione currentStation = client.getStazione(currentStationId);
		String safeStationId = currentStationId.replaceAll("[^a-zA-Z0-9_-]", "_");
		String safeSensorId = sensor.getId().replaceAll("[^a-zA-Z0-9_-]", "_");
		File csvFile = new File(csvDir, currentStation.getNome().replace("/","") + ".csv");
		try (PrintWriter out = new PrintWriter(csvFile)) {
		    out.println("time,value,station_name,parameter,Quality control level code");
		    String stationName = currentStation.getNome();
		    String parameter = sensorType;
		    BigDecimal noDataValue = new BigDecimal("-999.0");
		    for (Dato dato : dati.getDati()) {
			String time = dato.getDate() != null ? ISO8601DateTimeUtils.getISO8601DateTime(dato.getDate()) : "";
			String value = dato.getValue() != null ? dato.getValue().toPlainString() : "";
			String qcCode = toQualityControlLevelCode(dato, noDataValue);
			out.println(escapeCsv(time) + "," + escapeCsv(value) + "," + escapeCsv(stationName) + "," + escapeCsv(
				parameter) + "," + escapeCsv(qcCode));
		    }
		}
		System.out.println("      CSV: " + csvFile.getAbsolutePath());

	    }
	}

	for (String error : errors) {
	    System.err.println("ERROR FOR ID: "+error);
	}

	System.out.println("ENDED");
    }

    /**
     * Same mapping as {@link eu.essi_lab.downloader.hiscentral.HISCentralLombardiaDownloader}: noDataValue -> MISSING, V0 -> GOOD;
     * otherwise empty (no code).
     */
    private static String toQualityControlLevelCode(Dato dato, BigDecimal noDataValue) {
	WML2QualityCategory quality = null;
	if (dato.getValue() != null && dato.getValue().compareTo(noDataValue) == 0) {
	    quality = WML2QualityCategory.MISSING;
	}
	ID_VALIDITY_FLAG validityFlag = dato.getValidityFlag();
	if (validityFlag != null && validityFlag == ID_VALIDITY_FLAG.V0) {
	    quality = WML2QualityCategory.GOOD;
	}
	return quality != null ? quality.getUri() : "";
    }

    private static String escapeCsv(String value) {
	if (value == null) {
	    return "";
	}
	if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
	    return "\"" + value.replace("\"", "\"\"") + "\"";
	}
	return value;
    }

}
