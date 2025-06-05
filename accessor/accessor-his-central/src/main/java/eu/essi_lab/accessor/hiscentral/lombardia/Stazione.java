package eu.essi_lab.accessor.hiscentral.lombardia;

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

import java.math.BigDecimal;

public class Stazione {
    private String id;
    private String idStato;
    private String idComune;
    private String idTipoStazione;
    private String nome;
    private BigDecimal utmNord;
    private BigDecimal utmEst;
    private BigDecimal quota;
    private String indirizzo;

    public String getIndirizzo() {
	return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
	this.indirizzo = indirizzo;
    }

    public Stazione() {

    }

    public String getId() {
	return id;
    }

    public String getNome() {
	return nome;
    }

    public Comune getComune() {
	return HISCentralLombardiaClient.getComune(getIdComune());
    } 
    
    public String getIdComune() {
	return idComune;
    }

    public String getTipoStazione() {
	return HISCentralLombardiaClient.getTipiStazione(getIdTipoStazione());
    }
    
    public String getIdTipoStazione() {
	return idTipoStazione;
    }

    public BigDecimal getUtm32TNord() {
	return utmNord;
    }

    public BigDecimal getUtm32TEst() {
	return utmEst;
    }

    public void setId(String id) {
	this.id = id;
    }

    public void setNome(String nome) {
	this.nome = nome;
    }

    public void setIdComune(String idComune) {
	this.idComune = idComune;
    }

    public void setIdTipoStazione(String idTipoStazione) {
	this.idTipoStazione = idTipoStazione;
    }

    public void setUtm32TNord(String utmNord) {
	if (utmNord != null && !utmNord.isEmpty()) {
	    this.utmNord = new BigDecimal(utmNord);
	}
    }

    public void setUtm32TEst(String utmEst) {
	if (utmEst != null && !utmEst.isEmpty()) {
	    this.utmEst = new BigDecimal(utmEst);
	}
    }
    
    public String getStato() {
	return HISCentralLombardiaClient.getStatiStazione(getIdStato());
    }

    public String getIdStato() {
	return idStato;
    }

    public void setIdStato(String idStato) {
	this.idStato = idStato;
    }

    public BigDecimal getQuota() {
	return quota;
    }

    public void setQuota(String quota) {
	if (quota != null && !quota.isEmpty()) {
	    this.quota = new BigDecimal(quota);
	}

    }
}
