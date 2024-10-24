package eu.essi_lab.accessor.inumet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.model.exceptions.GSException;

public class INUMETClientExternalTestIT {

    @Test
    public void test() throws GSException {

	String endpoint = "https://api.inumet.gub.uy";
	INUMETClient client = new INUMETClient(endpoint);
	INUMETClient.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));
	INUMETClient.setUser("prohmsat");
	INUMETClient.setPassword("fg92,1U+q123");

	client.login();
	String token = client.getToken();
	System.out.println(token);
	assertNotNull(token);
	assertTrue(token.length() > 5);

	List<Map<String, String>> variables = client.getVariables();
	assertTrue(variables.size() == 1);
	Map<String, String> variable = variables.get(0);
	assertEquals("46", variable.get("id"));
	assertEquals("mm", variable.get("unidad"));

	List<Map<String, String>> stations = client.getStations();
	assertTrue(!stations.isEmpty());
	Map<String, String> station = stations.get(0);
	assertNotNull(station.get("id"));

	List<SimpleEntry<Date, BigDecimal>> data = client.getLastData("46", "2");
	assertTrue(!data.isEmpty());
	SimpleEntry<Date, BigDecimal> d = data.get(0);
	Date key = d.getKey();
	assertNotNull(key);
	BigDecimal value = d.getValue();
	assertNotNull(value);

    }

    public static void main(String[] args) {
	String json = "{\"variable\":{\"id\":46,\"idStr\":\"R3\",\"nombre\":\"R3\",\"abrevVariab\":\"R3\",\"unidad\":\"mm\",\"descripVariab\":\"Precipitación acumulada 24 horas de 10 a 10 UTC\\r\\n\\r\\n\\r\\n\",\"tipoIngreso\":4,\"nDecim\":1,\"nDigitosAlImprimir\":0,\"entera\":false,\"periodicidad\":9,\"tamanioColumnaCodigo\":6,\"periodoDeRegistro\":\"De 10UTC del día de registro a 9:59 UTC del día siguiente. La precipitación del día i a las 10 UTC hasta el día i + 1 a las 9:59 se guarda en el día i.\\r\\n\\r\\n\\r\\n\",\"tipoDeMedicion\":3,\"grupoVista\":\"Pluviometro\"},\"estaciones\":[{\"id\":12,\"idStr\":\"ARBOLITO\",\"nombre\":\"Arbolito\",\"codigoOMM\":null,\"codigoPluviometrico\":\"1841\",\"idOACI\":null,\"idAutomatica\":null,\"latitud\":-32.612,\"longitud\":-54.214,\"altitud\":null,\"cotaBarom\":null,\"tipoMet\":false,\"tipoPluvio\":true,\"tipoExterna\":false,\"tipoAutomatica\":false,\"tipoAeronautica\":false,\"husoHorario\":\"UTC-03:00\",\"gerencia\":\"MI\",\"estado\":\"CL\",\"usarDatosEnProductos\":true,\"wigosSerieId\":null,\"wigosEmisorId\":null,\"wigosNroExpedicion\":null,\"wigosLocalId\":null},12],\"datos\":[{\"estacion\":12,\"datos\":[{\"fecha\":\"2020-10-02T00:00:00.000Z\",\"valor\":\"5.0\"},{\"fecha\":\"2020-10-03T00:00:00.000Z\",\"valor\":\"6.0\"},{\"fecha\":\"2020-10-04T00:00:00.000Z\",\"valor\":\"0.0\"},{\"fecha\":\"2020-10-05T00:00:00.000Z\",\"valor\":\"5.0\"},{\"fecha\":\"2020-10-06T00:00:00.000Z\",\"valor\":\"0.0\"},{\"fecha\":\"2020-10-07T00:00:00.000Z\",\"valor\":\"0.0\"}],\"datosPorFecha\":{\"2020-10-02T00:00:00.000Z\":{\"fecha\":\"2020-10-02T00:00:00.000Z\",\"valor\":\"5.0\"},\"2020-10-03T00:00:00.000Z\":{\"fecha\":\"2020-10-03T00:00:00.000Z\",\"valor\":\"6.0\"},\"2020-10-04T00:00:00.000Z\":{\"fecha\":\"2020-10-04T00:00:00.000Z\",\"valor\":\"0.0\"},\"2020-10-05T00:00:00.000Z\":{\"fecha\":\"2020-10-05T00:00:00.000Z\",\"valor\":\"5.0\"},\"2020-10-06T00:00:00.000Z\":{\"fecha\":\"2020-10-06T00:00:00.000Z\",\"valor\":\"0.0\"},\"2020-10-07T00:00:00.000Z\":{\"fecha\":\"2020-10-07T00:00:00.000Z\",\"valor\":\"0.0\"}}},{\"datos\":[],\"datosPorFecha\":{}}],\"iEstaciones\":{\"12\":0,\"undefined\":1},\"fieldIdEstacion\":\"id\"}";
	JSONObject obj = new JSONObject(json);
    }
}
