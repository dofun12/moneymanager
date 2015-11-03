package org.lemanoman;
import java.util.*;

public class CategoriaModel {
    private String id;
    private String name;
    private List<LancamentoModel> lancamentos;
    
    public CategoriaModel(String id,String name,List<LancamentoModel> lancamentos){
	this.id = id;
	this.name = name;
	this.lancamentos = lancamentos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LancamentoModel> getLancamentos() {
        return lancamentos;
    }

    public void setLancamentos(List<LancamentoModel> lancamentos) {
        this.lancamentos = lancamentos;
    }
    
    
}
