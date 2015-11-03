package org.lemanoman;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LancamentoModel {
    private Date date;
    private String descr;
    private Double valor;
    private ObjectMapper mapper;
    public LancamentoModel(Date date,String descr,Double valor){
	mapper = new ObjectMapper();
	mapper.enable(SerializationFeature.INDENT_OUTPUT);
	
	this.date = date;
	this.descr = descr;
	this.valor = valor;
    }
    
    public LancamentoModel(ObjectNode node){
	this.descr = node.get("descr").asText();
	this.valor = node.get("valor").asDouble();
	this.date = new Date(node.get("date").asLong());
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
    
    public ObjectNode getObjectNode(){
	ObjectNode lancamentoNode = mapper.createObjectNode();
	lancamentoNode.put("descr", getDescr());
	lancamentoNode.put("valor", getValor());
	lancamentoNode.put("date", getDate().getTime());
	return lancamentoNode;
	
    }
    
    
}
