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

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_FUNZIONE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_OPERATORE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_PERIODO;

public class Sensore {

    private String id;
    private String nome;
    private String idStato;
    private String idTipoSensore;
    private String idStazione;
    private String unitaMisura;
    private String frequenza;
    private BigDecimal quota;
    private BigDecimal utmNord;
    private BigDecimal utmEst;
    private ID_FUNZIONE funzione;
    private ID_OPERATORE operatore;
    private ID_PERIODO periodo;
    private Date from;
    private Date to;

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getNome() {
	return nome;
    }

    public void setNome(String nome) {
	this.nome = nome;
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

    public String getTipoSensore() {
	return HISCentralLombardiaClient.getTipoSensore(getIdTipoSensore());
    }

    public String getIdTipoSensore() {
	return idTipoSensore;
    }

    public void setIdTipoSensore(String idTipoSensore) {
	this.idTipoSensore = idTipoSensore;
    }

    public String getIdStazione() {
	return idStazione;
    }

    public void setIdStazione(String idStazione) {
	this.idStazione = idStazione;
    }

    public String getUnitaMisura() {
	return unitaMisura;
    }

    public void setUnitaMisura(String unitaMisura) {
	this.unitaMisura = unitaMisura;
    }

    public String getFrequenza() {
	return frequenza;
    }

    public void setFrequenza(String frequenza) {
	this.frequenza = frequenza;
    }

    public BigDecimal getQuota() {
	return quota;
    }

    public void setQuota(BigDecimal quota) {
	this.quota = quota;
    }

    public void setQuota(String quota) {
	if (quota != null && !quota.isEmpty()) {
	    this.quota = new BigDecimal(quota);
	}
    }

    public BigDecimal getUtm32TNord() {
	return utmNord;
    }

    public void setUtm32TNord(BigDecimal utmNord) {
	this.utmNord = utmNord;
    }

    public void setUtm32TNord(String utm) {
	if (utm != null && !utm.isEmpty()) {
	    this.utmNord = new BigDecimal(utm);
	}
    }

    public BigDecimal getUtmEst() {
	return utmEst;
    }

    public void setUtm32TEst(BigDecimal utmEst) {
	this.utmEst = utmEst;
    }

    public void setUtm32TEst(String utm) {
	if (utm != null && !utm.isEmpty()) {
	    this.utmEst = new BigDecimal(utm);
	}
    }

    public ID_FUNZIONE getFunzione() {
	return funzione;
    }

    public void setIdFunzione(ID_FUNZIONE idFunzione) {
	this.funzione = idFunzione;
    }

    public void setIdFunzione(String idFunzione) {
	this.funzione = ID_FUNZIONE.decode(idFunzione);
    }

    public ID_OPERATORE getOperatore() {
	return operatore;
    }

    public void setIdOperatore(ID_OPERATORE idOperatore) {
	this.operatore = idOperatore;
    }

    public void setIdOperatore(String idOperatore) {
	this.operatore = ID_OPERATORE.decode(idOperatore);
    }

    public ID_PERIODO getPeriodo() {
	return periodo;
    }

    public void setIdPeriodo(ID_PERIODO idPeriodo) {
	this.periodo = idPeriodo;
    }

    public void setIdPeriodo(String idPeriodo) {
	this.periodo = ID_PERIODO.decode(idPeriodo);
    }

    public Date getFrom() {
	return from;
    }

    public void setFrom(Date from) {
	this.from = from;
    }

    public void setFrom(String date) throws ParseException {
	if (date != null) {
	    this.from = HISCentralLombardiaClient.sdf.parse(date);
	}
    }

    public Date getTo() {
	return to;
    }

    public void setTo(Date to) {
	this.to = to;
    }

    public void setTo(String date) throws ParseException {
	if (date != null) {
	    this.to = HISCentralLombardiaClient.sdf.parse(date);
	}
    }

    @Override
    public String toString() {
	return "Sensore [id=" + id + ", nome=" + nome + ", idStato=" + idStato + ", idTipoSensore=" + idTipoSensore
		+ ", idStazione=" + idStazione + ", unitaMisura=" + unitaMisura + ", frequenza=" + frequenza
		+ ", quota=" + quota + ", utmNord=" + utmNord + ", utmEst=" + utmEst + ", funzione=" + funzione
		+ ", operatore=" + operatore + ", periodo=" + periodo + ", from=" + from + ", to=" + to + "]";
    }

}
