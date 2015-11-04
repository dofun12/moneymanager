package org.lemanoman;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import org.lemanoman.fileutils.FileUtilsLemanoman;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Start {
	private ObjectMapper mapper = new ObjectMapper();
	public String mainNodePath = "data.js";
	private ObjectNode mainNode = null;

	public static void main(String[] args) {
		new Start();
	}

	public Start() {
		init();
		updateJsonData();
		SimpleDateFormat spdf = new SimpleDateFormat("dd/MM");
		String dateStart = "";
		String dateEnd = "";

		long menordate = 0;
		long maiordate = 0;
		for (CategoriaModel model : getCategorias()) {
			Double total = 0d;

			int i = 0;

			for (LancamentoModel lm : model.getLancamentos()) {
				if (menordate == 0)
					menordate = lm.getDate().getTime();
				if (maiordate == 0)
					maiordate = lm.getDate().getTime();

				if (lm.getDate().getTime() < menordate) {
					menordate = lm.getDate().getTime();
				}

				if (lm.getDate().getTime() > maiordate) {
					maiordate = lm.getDate().getTime();
				}

				total = total + lm.getValor();
				i++;
			}
			if (!model.getId().equals("0000")) {
				System.out.println(String.format("%s - %s : %.2f", model.getId(), model.getName(), total));
			}

		}
		dateStart = spdf.format(new Date(menordate));
		dateEnd = spdf.format(new Date(maiordate));
		System.out.println("Periodo: " + dateStart + " - " + dateEnd);

	}

	public List<CategoriaModel> getCategorias() {
		ArrayNode categorias = mapper.convertValue(mainNode.get("categorias"), ArrayNode.class);
		List<CategoriaModel> categoriasList = new ArrayList<CategoriaModel>();
		if (categorias != null) {
			for (Object obj : categorias) {
				List<LancamentoModel> models = new ArrayList<LancamentoModel>();
				ObjectNode categoria = mapper.convertValue(obj, ObjectNode.class);
				ArrayNode descricoes = mapper.convertValue(categoria.get("descricoes"), ArrayNode.class);

				if (descricoes != null) {
					for (Object objTmp : descricoes) {
						String descTmp = mapper.convertValue(objTmp, String.class);
						for (LancamentoModel lm : getLancamentoByDescr(descTmp)) {
							models.add(lm);
						}
					}
				}
				CategoriaModel categoriaModel = new CategoriaModel(categoria.get("id").asText(),
						categoria.get("name").asText(), models);
				categoriasList.add(categoriaModel);
			}
		}
		return categoriasList;
	}

	public List<ObjectNode> getCategorias(String descr) {
		List<ObjectNode> categoriasList = new ArrayList<ObjectNode>();
		String searchKeyStr = "" + descr;
		Integer searchKey = searchKeyStr.hashCode();

		ArrayNode categorias = mapper.convertValue(mainNode.get("categorias"), ArrayNode.class);
		if (categorias != null) {
			for (Object obj : categorias) {
				ObjectNode categoria = mapper.convertValue(obj, ObjectNode.class);

				ArrayNode descricoes = mapper.convertValue(categoria.get("descricoes"), ArrayNode.class);
				if (descricoes != null) {
					for (Object tmp : descricoes) {
						String descTmp = mapper.convertValue(tmp, String.class);
						if (searchKey.equals(descTmp.hashCode())) {
							categoriasList.add(categoria);
						}
					}
				}

			}
		}
		return categoriasList;
	}

	public ObjectNode getLancamento(String descr, Long date) {
		String searchKeyStr = "" + descr + date;
		Integer searchKey = searchKeyStr.hashCode();

		ArrayNode lancamentos = mapper.convertValue(mainNode.get("lancamentos"), ArrayNode.class);
		for (Object obj : lancamentos) {
			ObjectNode lancamento = mapper.convertValue(obj, ObjectNode.class);

			String keyStr = "" + lancamento.get("descr").asText() + lancamento.get("date").asLong();
			Integer key = keyStr.hashCode();

			if (searchKey.equals(key)) {
				return lancamento;
			}
		}
		return null;
	}

	public void updateJsonData() {
		
		Integer addedLancs = 0;
		Integer addedCategorias = 0;
		Map<Integer, String> uniqueDescrs = new HashMap<Integer, String>();

		if (mainNode != null) {
			ArrayNode lancamentos = mapper.convertValue(mainNode.get("lancamentos"), ArrayNode.class);
			for (LancamentoModel lancModel : getLancamentos()) {
				uniqueDescrs.put(lancModel.getDescr().hashCode(), lancModel.getDescr());
				if (getLancamento(lancModel.getDescr(), lancModel.getDate().getTime()) == null) {
					lancamentos.add(lancModel.getObjectNode());
					addedLancs++;
				}
			}
		}

		ArrayNode categorias = mapper.convertValue(mainNode.get("categorias"), ArrayNode.class);
		ObjectNode categoria = null;
		ArrayNode descricoes = null;
		for (Object tmpCat : categorias) {
			ObjectNode tmpCategoriaNode = mapper.convertValue(tmpCat, ObjectNode.class);
			if ("0000".equals(tmpCategoriaNode.get("id").asText())) {
				categoria = tmpCategoriaNode;
				descricoes = mapper.convertValue(tmpCategoriaNode.get("descricoes"), ArrayNode.class);
			}
		}

		for (Integer key : uniqueDescrs.keySet()) {
			if (getCategorias(uniqueDescrs.get(key)).isEmpty()) {
				descricoes.add(uniqueDescrs.get(key));
				addedCategorias++;
			}
		}
		categoria.set("descricoes", descricoes);
		mainNode.set("categorias", categorias);

		System.out.println("Adicionados " + addedLancs + " novos lancamentos");
		System.out.println("Adicionadas " + addedCategorias + " novas categorias");
		saveNode();
	}

	public void init() {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		File jsonData = new File(mainNodePath);

		ObjectNode mainNode = null;

		if (!jsonData.exists()) {
			mainNode = createMainNode();
		} else {
			try {
				mainNode = mapper.readValue(jsonData, ObjectNode.class);
				if (mainNode == null) {
					mainNode = createMainNode();
				}
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.mainNode = mainNode;

	}

	public ObjectNode createMainNode() {
		ObjectNode mainNode = mapper.createObjectNode();

		ArrayNode categorias = mapper.createArrayNode();
		ArrayNode lancamentos = mapper.createArrayNode();
		ObjectNode categoria = null;
		ArrayNode descricoes = null;

		categorias = mapper.createArrayNode();
		categoria = mapper.createObjectNode();
		descricoes = mapper.createArrayNode();

		categoria.put("id", "0000");
		categoria.put("name", "Sem Categoria");
		categoria.set("descricoes", descricoes);
		categorias.add(categoria);

		mainNode.set("categorias", categorias);
		mainNode.set("lancamentos", lancamentos);

		try {
			mapper.writeValue(new File(mainNodePath), mainNode);
			return mainNode;
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void saveNode() {
		if (mainNode != null) {
			try {
				mapper.writeValue(new File(mainNodePath), mainNode);
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public List<LancamentoModel> getLancamentoByDescr(String descr) {
		List<LancamentoModel> list = new ArrayList<LancamentoModel>();
		ArrayNode lancamentos = mapper.convertValue(mainNode.get("lancamentos"), ArrayNode.class);
		for (Object obj : lancamentos) {
			ObjectNode lancamento = mapper.convertValue(obj, ObjectNode.class);
			LancamentoModel model = new LancamentoModel(lancamento);
			if (model.getDescr().equals(descr)) {
				list.add(model);
			}
		}
		return list;
	}

	public List<LancamentoModel> getLancamentos() {
		JFileChooser chooser = new JFileChooser();
		chooser.setVisible(true);
		chooser.showOpenDialog(null);
		File extratoFile = chooser.getSelectedFile();
		List<LancamentoModel> lancamentos = new ArrayList<LancamentoModel>();
		if(extratoFile.exists()){
			List<String> lines = FileUtilsLemanoman.readFile(extratoFile.getPath());
			for (String line : lines) {
				String[] data = line.split(";");
	
				String dateStr = data[0].replaceAll("[^0-9\\/]", "");
	
				SimpleDateFormat spdf = new SimpleDateFormat("dd/MM/yyyy");
				Date date = null;
				try {
					date = spdf.parse(dateStr);
				} catch (ParseException e) {
					date = new Date();
				}
	
				String descr = data[1];
				if (!descr.substring(0, 3).equals("TBI")) {
					descr = descr.replaceAll("[0-9].+[0-9]", "");
				}
				descr = descr.replaceAll("\\s", "");
	
				String valorStr = data[2].replaceAll("[^A-z0-9\\-\\,]", "").replaceAll(",", ".");
	
				Double valor = Double.parseDouble(valorStr);
	
				lancamentos.add(new LancamentoModel(date, descr, valor));
	
			}
		}	
		return lancamentos;

	}

}
