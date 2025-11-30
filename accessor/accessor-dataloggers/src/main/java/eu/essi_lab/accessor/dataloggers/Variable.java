package eu.essi_lab.accessor.dataloggers;

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

