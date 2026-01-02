package eu.essi_lab.accessor.hiscentral.lombardia;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_FUNZIONE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_OPERATORE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_PERIODO;
import eu.essi_lab.lib.xml.stax.StAXDocumentParser;

public class RendiDatiResult {

    private List<Dato> dati = new ArrayList<>();
    private ID_FUNZIONE idFunzione;
    private ID_OPERATORE idOperatore;
    private ID_PERIODO idPeriodo;
    private String idSensore;
    private String nomeSensore;
    private String units;

    public List<Dato> getDati() {
	return dati;
    }

    public ID_FUNZIONE getIdFunzione() {
	return idFunzione;
    }

    public ID_OPERATORE getIdOperatore() {
	return idOperatore;
    }

    public ID_PERIODO getIdPeriodo() {
	return idPeriodo;
    }

    public String getIdSensore() {
	return idSensore;
    }

    public String getNomeSensore() {
	return nomeSensore;
    }

    public String getUnits() {
	return units;
    }

    public RendiDatiResult(InputStream content) throws Exception {
	StAXDocumentParser sdp = new StAXDocumentParser(content);
	// sdp.add(new QName("IdFunzione"), v -> idFunzione = ID_FUNZIONE.decode(v));
	// sdp.add(new QName("IdOperatore"), v -> idOperatore = ID_OPERATORE.decode(v));
	// sdp.add(new QName("IdPeriodo"), v -> idPeriodo = ID_PERIODO.decode(v));
	// sdp.add(new QName("IdSensore"), v -> idSensore = v);
	// sdp.add(new QName("UnitaMisura"), v -> units = v);
	// sdp.parse();
	// <R D="2017-05-01 00:30:00" V="14.5" S="0" />
	List<String> rs = sdp.find(new QName("R"));
	for (String r : rs) {
	    Dato d = new Dato();
	    StAXDocumentParser childSdp = new StAXDocumentParser(r);
	    childSdp.add(new QName("R"), "D", v -> d.setDate(v));
	    childSdp.add(new QName("R"), "V", v -> d.setValue(v));
	    childSdp.add(new QName("R"), "S", v -> d.setValidityFlag(v));
	    childSdp.parse();
	    if (d.getDate() != null && d.getValue() != null) {
		dati.add(d);
	    }
	}

    }

}
