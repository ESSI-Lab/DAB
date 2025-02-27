package eu.essi_lab.accessor.hiscentral.test;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.lombardia.Dato;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_FUNZIONE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_OPERATORE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_PERIODO;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_VALIDITY_FLAG;
import eu.essi_lab.accessor.hiscentral.lombardia.RendiDatiResult;
import eu.essi_lab.accessor.hiscentral.lombardia.Sensore;
import eu.essi_lab.accessor.hiscentral.lombardia.Stazione;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class HISCentralLombardiaClientExternalTestIT {

    @Test
    public void test() throws Exception {

	HISCentralLombardiaClient.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));
	URL endpoint = new URL(System.getProperty("HIS-CENTRAL-LOMBARDIA-ENDPOINT"));
	String keystorePassword = System.getProperty("HIS-CENTRAL-LOMBARDIA-KEYSTORE-PASSWORD");
	String username = System.getProperty("HIS-CENTRAL-LOMBARDIA-USERNAME");
	String password = System.getProperty("HIS-CENTRAL-LOMBARDIA-PASSWORD");

	HISCentralLombardiaClient client = new HISCentralLombardiaClient(endpoint, keystorePassword, username, password);

	// client.elencoTipologieSensore();

	Stazione stazione = client.getStazione("100");

	System.out.println("Dati stazione");
	System.out.println("nome: " + stazione.getNome());
	System.out.println("stato: " + stazione.getStato());
	System.out.println("tipo: " + stazione.getTipoStazione());
	System.out.println("comune: " + stazione.getComune());
	System.out.println("indirizzo: " + stazione.getIndirizzo());
	System.out.println("quota: " + stazione.getQuota());
	System.out.println("UTM: " + stazione.getUtm32TEst() + " " + stazione.getUtm32TNord());

	System.out.println();

	List<Sensore> sensori = client.elencoSensori(stazione.getId());
	for (Sensore sensore : sensori) {
	    System.out.println("Dati sensore");
	    System.out.println("id: " + sensore.getId());
	    System.out.println("nome: " + sensore.getNome());
	    System.out.println("stato: " + sensore.getStato());
	    System.out.println("tipo: " + sensore.getTipoSensore());
	    System.out.println("unita: " + sensore.getUnitaMisura());
	    System.out.println("frequenza: " + sensore.getFrequenza());
	    System.out.println("quota: " + sensore.getQuota());
	    System.out.println("utm nord: " + sensore.getUtm32TNord());
	    System.out.println("utm est: " + sensore.getUtmEst());
	    System.out.println("funzione: " + sensore.getFunzione().getLabel());
	    System.out.println("operatore: " + (sensore.getOperatore() == null ? "nessuno" : sensore.getOperatore().getLabel()));
	    System.out.println("periodo label: " + sensore.getPeriodo().getLabel());
	    System.out.println("from-to: " + sensore.getFrom() + " " + sensore.getTo());
	    System.out.println();

	}

	RendiDatiResult dati = client.rendiDati("2001", ID_FUNZIONE.ID_3_CALCOLATO, ID_OPERATORE.ID_1_MEDIA, ID_PERIODO.ID_4_T1D,
		ISO8601DateTimeUtils.parseISO8601ToDate("2022-01-01T00:00:00Z").get(),
		ISO8601DateTimeUtils.parseISO8601ToDate("2022-01-10T00:00:00Z").get());

	assertTrue(!dati.getDati().isEmpty());
	for (Dato dato : dati.getDati()) {
	    System.out.println(ISO8601DateTimeUtils.getISO8601DateTime(dato.getDate()) + " " + dato.getValue() + " "
		    + dato.getValidityFlag().getLabel());
	    assertTrue(dato.getValidityFlag().equals(ID_VALIDITY_FLAG.V10000));

	}

    }

}
