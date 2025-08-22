package eu.essi_lab.gssrv.conf.task;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;

public class ISPRASourceFinder extends SourceFinder {

    public enum ItalianSIR {

	BASILICATA("Italy, Sistema Informativo Regionale Basilicata", "ita-sir-basilicata", "http://rbasil.dynalias.org/Datascape/v1/",
		"SIR_BASILICATA"), //
	EMILIA_ROMAGNA("Italy, Sistema Informativo Regionale Emilia-Romagna", "ita-sir-emilia-romagna",
		"https://dati-simc.arpae.it/opendata/osservati/meteo/realtime/realtime.jsonl", "SIR_EMILIA"), //
	FRIULI_VENEZIA_GIULIA("Italy, Sistema Informativo Regionale Friuli Venezia Giulia", "ita-sir-friuli-venezia-giulia",
		"https://api.meteo.fvg.it/", "SIR_FRIULI"), //
	LAZIO("Italy, Sistema Informativo Regionale Lazio", "ita-sir-lazio", "http://rlazio.dynalias.org/datascape/v1/", "SIR_LAZIO"), //
	LIGURIA("Italy, Sistema Informativo Regionale Liguria", "ita-sir-liguria", "https://aws.arpal.liguria.it/ords/ws/HIS_CENTRAL/",
		"SIR_LIGURIA"), //
	MARCHE("Italy, Sistema Informativo Regionale Marche", "ita-sir-marche", "http://app.protezionecivile.marche.it", "SIR_MARCHE"), //
	PIEMONTE("Italy, Sistema Informativo Regionale Piemonte", "ita-sir-piemonte", "https://utility.arpa.piemonte.it/meteoidro/",
		"SIR_PIEMONTE"), //
	PUGLIA("Italy, Sistema Informativo Regionale Puglia", "ita-sir-puglia", "http://93.57.89.5:9000/api/", "SIR_PUGLIA"), //
	SARDEGNA("Italy, Sistema Informativo Regionale Sardegna", "ita-sir-sardegna",
		"https://eu-central-1.aws.data.mongodb-api.com/app/hiscentral-dqluv/endpoint/", "SIR_SARDEGNA"), //
	TOSCANA("Italy, Sistema Informativo Regionale Toscana", "ita-sir-toscana",
		"http://www.sir.toscana.it/archivio/dati.php?D=json_stations", "SIR_TOSCANA"), //
	UMBRIA("Italy, Sistema Informativo Regionale Umbria", "ita-sir-umbria",
		"https://dati.regione.umbria.it/api/3/action/datastore_search", "SIR_UMBRIA"), //
	VAL_D_AOSTA("Italy, Sistema Informativo Regionale Valle d'Aosta", "ita-sir-val-d-aosta", "https://cf-api.regione.vda.it/ws2",
		"SIR_VALDAOSTA"), //
	VENETO("Italy, Sistema Informativo Regionale Veneto", "ita-sir-veneto", "https://api.arpa.veneto.it/REST/v1/", "SIR_VENETO"), //
	LOMBARDIA("Italy, Sistema Informativo Regionale, Lombardia", "ita-sir-lombardia", "https://remws.arpalombardia.it",
		"SIR_LOMBARDIA"), //
	BOLZANO("Italy, Sistema Informativo della Provincia Autonoma di Bolzano", "ita-sir-bolzano",
		"http://daten.buergernetz.bz.it/services/meteo/v1/", "SIR_BOLZANO"), //
	HIS_CENTRAL_SHAPE_FILES("HIS Central shape files", "his-central-shapes", "https://his-central.s3.amazonaws.com/polygons/",
		"S3-SHAPEFILE");//

	private String name;

	public String getName() {
	    return name;
	}

	public String getId() {
	    return id;
	}

	public String getEndpoint() {
	    return endpoint;
	}

	public String getAccessorType() {
	    return accessorType;
	}

	private String id;
	private String endpoint;
	private String accessorType;

	private ItalianSIR(String name, String id, String endpoint, String accessorType) {
	    this.name = name;
	    this.id = id;
	    this.endpoint = endpoint;
	    this.accessorType = accessorType;
	}
    }

    @Override
    public List<HarvestingSetting> getSources(String endpoint, String identifierPrefix) {
	List<HarvestingSetting> ret = new ArrayList<HarvestingSetting>();
	List<String> augmenterTypes = Arrays.asList(//
		"EasyAccessAugmenter", //
		"WHOSUnitsAugmenter", //
		"HISCentralVariableAugmenter");

	for (ItalianSIR sir : ItalianSIR.values()) {
	    List<String> augs = new ArrayList<String>(augmenterTypes);
	    if (sir.equals(ItalianSIR.HIS_CENTRAL_SHAPE_FILES)) {
		augs.clear();
	    }
	    HarvestingSetting harvestingSetting = createSetting(//
		    sir.getAccessorType(), //
		    Optional.empty(), //
		    sir.getId(), //
		    sir.getName(), //
		    sir.getEndpoint(), //
		    augs);
	    ret.add(harvestingSetting);

	}

	return ret;
    }

}
