package eu.essi_lab.accessor.dataloggers;

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

public class Variable {
    private Integer varId;
    private String varCod;
    private Integer uomId;
    private String uomCod;

    public Integer getVarId() {
        return varId;
    }

    public void setVarId(Integer varId) {
        this.varId = varId;
    }

    public String getVarCod() {
        return varCod;
    }

    public void setVarCod(String varCod) {
        this.varCod = varCod;
    }

    public Integer getUomId() {
        return uomId;
    }

    public void setUomId(Integer uomId) {
        this.uomId = uomId;
    }

    public String getUomCod() {
        return uomCod;
    }

    public void setUomCod(String uomCod) {
        this.uomCod = uomCod;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "varId=" + varId +
                ", varCod='" + varCod + '\'' +
                ", uomId=" + uomId +
                ", uomCod='" + uomCod + '\'' +
                '}';
    }
}

