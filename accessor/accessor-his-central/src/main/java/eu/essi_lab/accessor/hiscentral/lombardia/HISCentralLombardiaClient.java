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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.stax.StAXDocumentParser;

public class HISCentralLombardiaClient {

    private static String giProxyEndpoint = null;

    public static String getGiProxyEndpoint() {
	if (giProxyEndpoint == null) {
	    giProxyEndpoint = ConfigurationWrapper.getSystemSettings().getProxyEndpoint().orElse(null);
	}
	return giProxyEndpoint;
    }

    public static void setGiProxyEndpoint(String endpoint) {
	giProxyEndpoint = endpoint;
    }

    public enum ID_FUNZIONE {
	ID_1_RILEVATO(1, "Dato rilevato"), //
	ID_3_CALCOLATO(3, "Dato calcolato");

	private Integer id;

	private String label;

	public Integer getId() {
	    return id;
	}

	public String getLabel() {
	    return label;
	}

	ID_FUNZIONE(Integer id, String label) {
	    this.id = id;
	    this.label = label;
	}

	public static ID_FUNZIONE decode(String id) {
	    for (ID_FUNZIONE idFunzione : values()) {
		if (idFunzione.getId().toString().equals(id)) {
		    return idFunzione;
		}
	    }
	    return null;
	}
    }

    public enum ID_OPERATORE {
	ID_1_MEDIA(1, "Media"), //
	ID_2_MINIMO(2, "Minimo"), //
	ID_3_MASSIMO(3, "Massimo"), //
	ID_4_CUMULATA(4, "Cumulata"), //
	ID_10_MEDIA_MOBILE_8_ORE(10, "Media mobile 8 ore"), //
	ID_12_MASSIMO_MEDI_GIORNALIERO(12, "Massimo dei medi (solo giornaliero)"), //
	ID_13_MINIMO_MEDI_GIORNALIERO(13, "Minimo dei medi (solo giornaliero)");

	private Integer id;

	private String label;

	public Integer getId() {
	    return id;
	}

	public String getLabel() {
	    return label;
	}

	ID_OPERATORE(Integer id, String label) {
	    this.id = id;
	    this.label = label;
	}

	public static ID_OPERATORE decode(String id) {
	    for (ID_OPERATORE idOperatore : values()) {
		if (idOperatore.getId().toString().equals(id)) {
		    return idOperatore;
		}
	    }
	    return null;
	}
    }

    public enum ID_PERIODO {
	ID_1_T10M(1, "10 minuti"), //
	ID_2_T30M(2, "30 minuti"), //
	ID_3_T60M(3, "60 minuti"), //
	ID_4_T1D(4, "giornaliero"), //
	ID_5_T1M(5, "1 minuto"), //
	ID_6_T3H(6, "3 ore"), //
	ID_8_T2H(8, "2 ore"), //
	ID_9_T4H(9, "4 ore"), //
	ID_10_T5M(10, "5 minuti");

	private Integer id;

	private String label;

	public Integer getId() {
	    return id;
	}

	public String getLabel() {
	    return label;
	}

	ID_PERIODO(Integer id, String label) {
	    this.id = id;
	    this.label = label;
	}

	public static ID_PERIODO decode(String id) {
	    for (ID_PERIODO idPeriodo : values()) {
		if (idPeriodo.getId().toString().equals(id)) {
		    return idPeriodo;
		}
	    }
	    return null;
	}

    }

    public enum ID_VALIDITY_FLAG {
	V11210("validato utente da visto da invalidato automaticamente da numero campioni insufficiente"), //
	V11203("validato utente da visto da invalidato automaticamente da incerto"), //
	V11202("validato utente da visto da invalidato automaticamente da non disponibile / strumento non presente"), //
	V11201("validato utente da visto da invalidato automaticamente da invalido"), //
	V11200("validato utente da visto da invalidato automaticamente da valido"), //
	V11199("validato utente da visto da validato automaticamente ricostruito da procedure automatiche"), //
	V11103("validato utente da visto da validato automaticamente da incerto"), //
	V11101("validato utente da visto da validato automaticamente da invalido"), //
	V11100("validato utente da visto da validato automaticamente da valido"), //
	V11000("validato utente da visto da valido"), //
	V10210("validato utente da invalidato automaticamente da numero campioni insufficiente"), //
	V10203("validato utente da invalidato automaticamente da incerto"), //
	V10202("validato utente da invalidato automaticamente da non disponibile / strumento non presente"), //
	V10201("validato utente da invalidato automaticamente da invalido"), //
	V10200("validato utente da invalidato automaticamente da valido"), //
	V10199("validato utente da validato automaticamente ricostruito da procedure automatiche"), //
	V10103("validato utente da validato automaticamente da incerto"), //
	V10101("validato utente da validato automaticamente da invalido"), //
	V10100("validato utente da validato automaticamente da valido"), //
	V10010("validato utente da numero campioni insufficiente"), //
	V10003("validato utente da incerto"), //
	V10002("validato utente da non disponibile / strumento non presente"), //
	V10001("validato utente da invalido"), //
	V10000("validato utente da valido"), //
	V1199("visto da validato automaticamente ricostruito da procedure automatiche"), //
	V1103("visto da validato automaticamente da incerto"), //
	V1101("visto da validato automaticamente da invalido"), //
	V1100("visto da validato automaticamente da valido"), //
	V1000("visto da valido"), //
	V199("validato automaticamente ricostruito da procedure automatiche"), //
	V103("validato automaticamente da incerto"), //
	V101("validato automaticamente da invalido"), //
	V100("validato automaticamente da valido"), //
	V3("validato con numero campioni superiore a 80%"), //
	V0("valido"), //
	V_1("invalido"), //
	V_2("non disponibile / strumento non presente"), //
	V_3("incerto"), //
	V_10("numero campioni insufficiente"), //
	V_200("invalidato automaticamente da valido"), //
	V_201("invalidato automaticamente da invalido"), //
	V_202("invalidato automaticamente da non disponibile / strumento non presente"), //
	V_203("invalidato automaticamente da incerto"), //
	V_210("invalidato automaticamente da numero campioni insufficiente"), //
	V_1001("visto da invalido"), //
	V_1002("visto da non disponibile / strumento non presente"), //
	V_1003("visto da incerto"), //
	V_1010("visto da numero campioni insufficiente"), //
	V_1200("visto da invalidato automaticamente da valido"), //
	V_1201("visto da invalidato automaticamente da invalido"), //
	V_1202("visto da invalidato automaticamente da non disponibile / strumento non presente"), //
	V_1203("visto da invalidato automaticamente da incerto"), //
	V_1210("visto da invalidato automaticamente da numero campioni insufficiente"), //
	V_10000("invalidato utente da valido"), //
	V_10001("invalidato utente da invalido"), //
	V_10002("invalidato utente da non disponibile / strumento non presente"), //
	V_10003("invalidato utente da incerto"), //
	V_10010("invalidato utente da numero campioni insufficiente"), //
	V_10199("invalidato utente da validato automaticamente ricostruito da procedure automatiche"), //
	V_10200("invalidato utente da invalidato automaticamente da valido"), //
	V_10201("invalidato utente da invalidato automaticamente da invalido"), //
	V_10202("invalidato utente da invalidato automaticamente da non disponibile / strumento non presente"), //
	V_10203("invalidato utente da invalidato automaticamente da incerto"), //
	V_10210("invalidato utente da invalidato automaticamente da numero campioni insufficiente"), //
	V_11001("invalidato utente da visto da invalido"), //
	V_11002("invalidato utente da visto da non disponibile / strumento non presente"), //
	V_11003("invalidato utente da visto da incerto"), //
	V_11010("invalidato utente da visto da numero campioni insufficiente"), //
	V_11199("invalidato utente da visto da validato automaticamente ricostruito da procedure automatiche"), //
	V_11200("invalidato utente da visto da invalidato automaticamente da valido"), //
	V_11201("invalidato utente da visto da invalidato automaticamente da invalido"), //
	V_11202("invalidato utente da visto da invalidato automaticamente da non disponibile / strumento non presente"), //
	V_11203("invalidato utente da visto da invalidato automaticamente da incerto"), //
	V_11210("invalidato utente da visto da invalidato automaticamente da numero campioni insufficiente"), //
	V_10100("invalidato utente da validato automaticamente");

	private String label;

	private String id;

	public String getId() {
	    return id;
	}

	public String getLabel() {
	    return label;
	}

	ID_VALIDITY_FLAG(String label) {
	    String i = this.toString().replace("V", "").replace("_", "-");
	    this.id = i;
	    this.label = label;
	}

	public static ID_VALIDITY_FLAG decode(String id) {
	    for (ID_VALIDITY_FLAG ivf : values()) {
		if (ivf.getId().equals(id)) {
		    return ivf;
		}
	    }
	    return null;
	}

    }

    public enum Path {
	AUTENTICAZIONE("Autenticazione.svc"), //
	ANAGRAFICA("Anagrafica.svc"), //
	DATI("Dati.svc"); //

	private String path;

	private Path(String path) {
	    this.path = path;
	}

	private String getPath() {
	    return path;
	}

    }

    public enum Operation {
	// WSDL1
	LOGIN(Path.AUTENTICAZIONE, "Login"), //
	LOGOUT(Path.AUTENTICAZIONE, "Logout"), //
	// WSDL2
	ELENCO_COMUNI(Path.ANAGRAFICA, "ElencoComuni"), //
	ELENCO_PROVINCE(Path.ANAGRAFICA, "ElencoProvince"), //
	ELENCO_SENSORI(Path.ANAGRAFICA, "ElencoSensori"), //
	ELENCO_STATI_STAZIONE(Path.ANAGRAFICA, "ElencoStatiStazione"), //
	ELENCO_STAZIONI(Path.ANAGRAFICA, "ElencoStazioni"), //
	ELENCO_TIPI_STAZIONE(Path.ANAGRAFICA, "ElencoTipiStazione"), //
	ELENCO_TIPOLOGIE_SENSORE(Path.ANAGRAFICA, "ElencoTipologieSensore"), //
	// WSDL3
	RENDI_DATI_TEMPO_REALE(Path.DATI, "RendiDatiTempoReale"), //
	RENDI_DATI(Path.DATI, "RendiDati"), //
	RENDI_DATI_TEMPO_REALE_METEO(Path.DATI, "RendiDatiTempoRealeMeteo");

	private String name;

	private Path path;

	private Operation(Path path, String name) {
	    this.path = path;
	    this.name = name;
	}

	private String getName() {
	    return name;
	}

	private Path getPath() {
	    return path;
	}

    }

    private URL endpoint;

    private String keystorePassword;

    private String token = null;
    private String username;
    private String password;

    private static HashMap<Operation, String> soapActions = new HashMap<>();
    private static HashMap<Path, XMLDocumentReader> wsdls = new HashMap<>();
    private static HashMap<String, String> statiStazione = new HashMap<>(); // id -> nome
    private static HashMap<String, String> tipiStazione = new HashMap<>(); // id -> nome
    private static HashMap<String, String> tipiSensore = new HashMap<>(); // id -> nome
    private static HashMap<String, String> province = new HashMap<>(); // sigla -> nome
    private static HashMap<String, Comune> comuni = new HashMap<>(); // id -> comune
    private static ExpiringCache<Stazione> stazioni = new ExpiringCache<>();
    static {
	stazioni.setDuration(1000 * 60 * 60 * 24l);
    }

    public static String getStatiStazione(String id) {
	return statiStazione.get(id);
    }

    public static String getTipiStazione(String id) {
	return tipiStazione.get(id);
    }

    public static String getTipoSensore(String idTipoSensore) {
	return tipiSensore.get(idTipoSensore);
    }

    public static String getProvince(String id) {
	return province.get(id);
    }

    public static Comune getComune(String id) {
	return comuni.get(id);
    }

    public List<String> getStationIdentifiers() throws Exception {
	elencoStazioni();
	List<String> ret = new ArrayList<>(stazioni.keySet());
	ret.sort(new Comparator<String>() {

	    @Override
	    public int compare(String o1, String o2) {
		try {
		    Integer i1 = Integer.parseInt(o1);
		    Integer i2 = Integer.parseInt(o2);
		    return i1.compareTo(i2);
		} catch (Exception e) {
		    return o1.compareTo(o2);
		}
	    }
	});
	return ret;
    }

    public Stazione getStazione(String id) throws Exception {
	elencoStazioni();
	return stazioni.get(id);

    }

    public HISCentralLombardiaClient(URL endpoint) throws Exception {
	this(endpoint, ConfigurationWrapper.getCredentialsSetting().getLombardiaKeystorePassword().orElse(null), //
		ConfigurationWrapper.getCredentialsSetting().getLombardiaUsername().orElse(null), //
		ConfigurationWrapper.getCredentialsSetting().getLombardiaPassword().orElse(null));
    }

    public HISCentralLombardiaClient(URL endpoint, String keystorePassword, String username, String password) throws Exception {
	this.endpoint = endpoint;
	this.keystorePassword = keystorePassword;
	this.username = username;
	this.password = password;

	GSLoggerFactory.getLogger(getClass()).info("Creating Soap actions cache");
	synchronized (soapActions) {
	    if (soapActions.isEmpty()) {
		for (Operation operation : Operation.values()) {
		    String name = operation.getName();
		    Path path = operation.getPath();
		    if (name != null) {
			XMLDocumentReader wsdl = wsdls.get(path);
			if (wsdl == null) {
			    wsdl = wsdl(path);
			    wsdls.put(path, wsdl);
			}
			String soapAction = wsdl.evaluateString("//*:operation[@name='" + name + "']/*:operation/@soapAction");
			soapActions.put(operation, soapAction);
		    }
		}
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Creating enum cache (stati stazione)");
	    if (HISCentralLombardiaClient.statiStazione.isEmpty()) {
		XMLDocumentReader statiStazione = elencoStatiStazione();
		Node[] statiStazioneNodes = statiStazione.evaluateNodes("//*:StatoStazione");
		for (Node statiStazioneNode : statiStazioneNodes) {
		    String id = statiStazione.evaluateString(statiStazioneNode, "@Id");
		    String nome = statiStazione.evaluateString(statiStazioneNode, "@Nome");
		    HISCentralLombardiaClient.statiStazione.put(id, nome);
		}
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Creating enum cache (tipi stazione)");
	    if (HISCentralLombardiaClient.tipiStazione.isEmpty()) {

		XMLDocumentReader tipiStazione = elencoTipiStazione();
		Node[] tipiStazioneNodes = tipiStazione.evaluateNodes("//*:TipoStazione");
		for (Node tipiStazioneNode : tipiStazioneNodes) {
		    String id = tipiStazione.evaluateString(tipiStazioneNode, "@Id");
		    String nome = tipiStazione.evaluateString(tipiStazioneNode, "@Nome");
		    HISCentralLombardiaClient.tipiStazione.put(id, nome);
		}
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Creating enum cache (province)");
	    if (HISCentralLombardiaClient.province.isEmpty()) {
		XMLDocumentReader province = elencoProvince();
		Node[] provinceNodes = province.evaluateNodes("//*:Provincia");
		for (Node provincia : provinceNodes) {
		    String id = province.evaluateString(provincia, "@Sigla");
		    String nome = province.evaluateString(provincia, "@Nome");
		    HISCentralLombardiaClient.province.put(id, nome);
		}
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Creating enum cache (comuni)");
	    if (HISCentralLombardiaClient.comuni.isEmpty()) {
		XMLDocumentReader comuni = elencoComuni();
		Node[] comuniNodes = comuni.evaluateNodes("//*:Comune");
		for (Node comune : comuniNodes) {
		    String id = comuni.evaluateString(comune, "@Id");
		    String codIstat = comuni.evaluateString(comune, "@CodIstat");
		    String nome = comuni.evaluateString(comune, "@Nome");
		    String provincia = comuni.evaluateString(comune, "@Provincia");
		    HISCentralLombardiaClient.comuni.put(id, new Comune(id, codIstat, nome, provincia));
		}
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Creating enum cache (tipologie sensori)");
	    if (HISCentralLombardiaClient.tipiSensore.isEmpty()) {
		XMLDocumentReader sensori = elencoTipologieSensore();
		Node[] sensoriNodes = sensori.evaluateNodes("//*:TipoSensore");
		for (Node sensore : sensoriNodes) {
		    String id = sensori.evaluateString(sensore, "@IdTipoSensore");
		    String nome = sensori.evaluateString(sensore, "@NomeTipoSensore");
		    HISCentralLombardiaClient.tipiSensore.put(id, nome);
		}
	    }
	}

    }

    public XMLDocumentReader wsdl(Path path) throws Exception {
	HttpResponse<InputStream> response = submit(path.getPath() + "?wsdl", null, null);
	XMLDocumentReader reader = new XMLDocumentReader(response.body());

	return reader;
    }

    public XMLDocumentReader wsdlAutenticazione() throws Exception {
	return wsdl(Path.AUTENTICAZIONE);
    }

    public XMLDocumentReader wsdlAnagrafica() throws Exception {
	return wsdl(Path.ANAGRAFICA);
    }

    public XMLDocumentReader wsdlDati() throws Exception {
	return wsdl(Path.DATI);
    }

    private class LoginResult {
	String exitCode;
	String msg;
	String token;

	public String getExitCode() {
	    return exitCode;
	}

	public void setExitCode(String exitCode) {
	    this.exitCode = exitCode;
	}

	public String getMsg() {
	    return msg;
	}

	public void setMsg(String msg) {
	    this.msg = msg;
	}

	public String getToken() {
	    return token;
	}

	public void setToken(String token) {
	    this.token = token;
	}

    }

    private LoginResult loginExecution() {
	LoginResult ret = new LoginResult();
	try {
	    String body = getSOAPHeader()//
		    + "<Login xmlns=\"http://tempuri.org/\">\n"//
		    + "<xElInput>\n"//
		    + "<Login>\n"//
		    + "<Login>" + username + "</Login>\n"//
		    + "<Password>" + password + "</Password>\n"//
		    + "</Login >\n"//
		    + "</xElInput>\n"//
		    + "</Login>\n"//
		    + getSOAPFooter();

	    XMLDocumentReader bodyReader;
	    bodyReader = new XMLDocumentReader(body);
	    HttpResponse<InputStream> response = submit(Operation.LOGIN, bodyReader);
	    XMLDocumentReader reader = new XMLDocumentReader(response.body());
	    token = reader.evaluateString("//*:Token/*:Id");
	    ret.setToken(token);
	    String exit = reader.evaluateString("//*:Esito");
	    String msg = reader.evaluateString("//*:Messaggio");
	    ret.setExitCode(exit);
	    ret.setMsg(msg);
	    return ret;
	} catch (Exception e) {
	    e.printStackTrace();
	    ret.setExitCode("exception");
	    ret.setMsg(e.getMessage());
	    return ret;
	}
    }

    private void login() throws Exception {
	LoginResult ret = null;
	main: for (int i = 0; i < 10; i++) {
	    ret = loginExecution();
	    switch (ret.getExitCode()) {
	    case "0":
	    case "exception":
	    default:
		break main;
	    case "3":
		GSLoggerFactory.getLogger(getClass()).info("Another session is open, trying to close it");
		String previousToken = readToken();
		if (previousToken != null) {
		    logout(previousToken);
		}
		break;
	    }
	}

	String exit = ret.getExitCode();
	String msg = ret.getMsg();
	token = ret.getToken();
	if (exit.equals("0")) {
	    writeToken(token);
	    // GSLoggerFactory.getLogger(getClass()).info("Successful login, token: {}", token);
	} else {
	    String error = "Error during login. " + msg;
	    GSLoggerFactory.getLogger(getClass()).error(error);
	    throw new RuntimeException(error);
	}
    }

    private static final String TOKEN_FILE = "arpa-lombardia.token";

    private synchronized void writeToken(String token) throws IOException {
	String tmpdir = System.getProperty("java.io.tmpdir");
	File file = new File(tmpdir, TOKEN_FILE);
	if (file.exists()) {
	    file.delete();
	}
	file.createNewFile();
	FileOutputStream fos = new FileOutputStream(file);
	fos.write(token.getBytes(StandardCharsets.UTF_8));
	fos.close();
    }

    private synchronized void deleteToken(String token) throws IOException {
	
	String tmpdir = System.getProperty("java.io.tmpdir");
	File file = new File(tmpdir, TOKEN_FILE);
	if (file.exists()) {
	    file.delete();
	}
    }

    private synchronized String readToken() throws IOException {
	String tmpdir = System.getProperty("java.io.tmpdir");
	File file = new File(tmpdir, TOKEN_FILE);
	if (!file.exists()) {
	    return null;
	}
	FileInputStream fis = new FileInputStream(file);
	InputStreamReader reader = new InputStreamReader(fis);
	BufferedReader br = new BufferedReader(reader);
	String token = br.readLine();
	br.close();
	reader.close();
	fis.close();
	return token;
    }

    private void logout() throws Exception {
	logout(token);
    }

    private void logout(String token) throws Exception {

	String body = getSOAPHeader() //
		+ "<Logout xmlns=\"http://tempuri.org/\">\n" //
		+ "<xElInput>\n" //
		+ "<Logout>\n" //
		+ getTokenXML(token) //
		+ "</Logout>\n" //
		+ "</xElInput>\n" //
		+ "</Logout>\n" //
		+ getSOAPFooter();

	XMLDocumentReader bodyReader = new XMLDocumentReader(body);
	HttpResponse<InputStream> response = submit(Operation.LOGOUT, bodyReader);
	XMLDocumentReader reader = new XMLDocumentReader(response.body());
	String exit = reader.evaluateString("//*:Esito");
	if (exit.equals("0")) {
	    // GSLoggerFactory.getLogger(getClass()).info("Successful logout of session {}", token);

	    deleteToken(token);
	    token = null;

	    return;
	} else {
	    String msg = reader.evaluateString("//*:Messaggio");
	    String error = "Error during logout of session " + token + " " + msg;
	    GSLoggerFactory.getLogger(getClass()).error(error);
	    throw new RuntimeException(error);
	}
    }

    private String getTokenXML() {
	return getTokenXML(token);
    }

    private String getTokenXML(String token) {
	if (token == null) {
	    return "";
	}
	return "<Token>\n" //
		+ "<Id>" + token + "</Id>\n" //
		+ "</Token>\n";
    }

    public XMLDocumentReader elencoStatiStazione() throws Exception {
	login();
	try {
	    String body = getSOAPHeader() //
		    + "<ElencoStatiStazione xmlns=\"http://tempuri.org/\"\n" //
		    + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" //
		    + "    xsi:schemaLocation=\"http://tempuri.org/ file:/home/boldrini/his-central/xsd20.xsd\">\n" //
		    + "    <xInput>\n" //
		    + "        <ElencoStatiStazione>" //
		    + getTokenXML() //
		    + "        </ElencoStatiStazione>\n" //
		    + "    </xInput>\n" //
		    + "</ElencoStatiStazione>" //
		    + getSOAPFooter();

	    XMLDocumentReader bodyReader = new XMLDocumentReader(body);
	    HttpResponse<InputStream> response = submit(Operation.ELENCO_STATI_STAZIONE, bodyReader);
	    XMLDocumentReader reader = new XMLDocumentReader(response.body());
	    return reader;
	} catch (Exception e) {
	    throw e;
	} finally {
	    logout();
	}
    }

    public XMLDocumentReader elencoTipiStazione() throws Exception {
	login();
	try {
	    String body = getSOAPHeader() //
		    + "<ElencoTipiStazione xmlns=\"http://tempuri.org/\"\n" //
		    + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" //
		    + "    xsi:schemaLocation=\"http://tempuri.org/ file:/home/boldrini/his-central/xsd20.xsd\">\n" //
		    + "    <xInput>\n" //
		    + "        <ElencoTipiStazione>" //
		    + getTokenXML() //
		    + "        </ElencoTipiStazione>\n" //
		    + "    </xInput>\n" //
		    + "</ElencoTipiStazione>" //
		    + getSOAPFooter();

	    XMLDocumentReader bodyReader = new XMLDocumentReader(body);
	    HttpResponse<InputStream> response = submit(Operation.ELENCO_TIPI_STAZIONE, bodyReader);
	    XMLDocumentReader reader = new XMLDocumentReader(response.body());
	    return reader;
	} catch (Exception e) {
	    throw e;
	} finally {
	    logout();
	}
    }

    public XMLDocumentReader elencoProvince() throws Exception {
	login();
	try {
	    String body = getSOAPHeader() //
		    + "<ElencoProvince xmlns=\"http://tempuri.org/\"\n" //
		    + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" //
		    + "    xsi:schemaLocation=\"http://tempuri.org/ file:/home/boldrini/his-central/xsd20.xsd\">\n" //
		    + "    <xInput>\n" //
		    + "        <ElencoProvince>\n" + getTokenXML() //
		    + "        </ElencoProvince>\n" //
		    + "    </xInput>\n" //
		    + "</ElencoProvince>" //
		    + getSOAPFooter();

	    XMLDocumentReader bodyReader = new XMLDocumentReader(body);
	    HttpResponse<InputStream> response = submit(Operation.ELENCO_PROVINCE, bodyReader);
	    XMLDocumentReader reader = new XMLDocumentReader(response.body());
	    return reader;
	} catch (Exception e) {
	    throw e;
	} finally {
	    logout();
	}
    }

    public XMLDocumentReader elencoComuni() throws Exception {
	login();
	try {
	    String body = getSOAPHeader() //
		    + "<ElencoComuni xmlns=\"http://tempuri.org/\"\n" //
		    + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" //
		    + "    xsi:schemaLocation=\"http://tempuri.org/ file:/home/boldrini/his-central/xsd20.xsd\">\n" //
		    + "    <xInput>\n" //
		    + "        <ElencoComuni>" //
		    + getTokenXML() //
		    + "        </ElencoComuni>\n" //
		    + "    </xInput>\n" //
		    + "</ElencoComuni>" //
		    + getSOAPFooter();

	    XMLDocumentReader bodyReader = new XMLDocumentReader(body);
	    HttpResponse<InputStream> response = submit(Operation.ELENCO_COMUNI, bodyReader);
	    XMLDocumentReader reader = new XMLDocumentReader(response.body());
	    return reader;
	} catch (Exception e) {
	    throw e;
	} finally {
	    logout();
	}
    }

    public Set<Entry<String, Stazione>> elencoStazioni() throws Exception {
	synchronized (stazioni) {
	    if (stazioni.size() > 0) {
		return stazioni.entrySet();
	    }
	}
	login();
	try {
	    String body = getSOAPHeader() //
		    + "<ElencoStazioni xmlns=\"http://tempuri.org/\"\n" //
		    + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" //
		    + "    xsi:schemaLocation=\"http://tempuri.org/ file:/home/boldrini/his-central/xsd20.xsd\">\n" //
		    + "    <xInput>\n" //
		    + "        <ElencoStazioni>\n" + getTokenXML() //
		    + "        </ElencoStazioni>\n" //
		    + "    </xInput>\n" //
		    + "</ElencoStazioni>" //
		    + getSOAPFooter();

	    XMLDocumentReader bodyReader = new XMLDocumentReader(body);
	    HttpResponse<InputStream> response = submit(Operation.ELENCO_STAZIONI, bodyReader);

	    StAXDocumentParser sdp = new StAXDocumentParser(response.body());
	    List<String> founds = sdp.find(new QName("Stazione"));
	    synchronized (stazioni) {
		for (String found : founds) {
		    Stazione stazione = new Stazione();
		    StAXDocumentParser childSdp = new StAXDocumentParser(found);
		    childSdp.add(new QName("IdStazione"), v -> stazione.setId(v));
		    childSdp.add(new QName("NomeStazione"), v -> stazione.setNome(v));
		    childSdp.add(new QName("Stato"), "IdStato", v -> stazione.setIdStato(v));
		    childSdp.add(new QName("Comune"), "IdComune", v -> stazione.setIdComune(v));
		    childSdp.add(new QName("TipoStazione"), "idTipoStazione", v -> stazione.setIdTipoStazione(v));
		    childSdp.add(new QName("Indirizzo"), v -> stazione.setIndirizzo(v));
		    childSdp.add(new QName("Quota"), v -> stazione.setQuota(v));
		    childSdp.add(new QName("UTM_Nord"), v -> stazione.setUtm32TNord(v));
		    childSdp.add(new QName("UTM_Est"), v -> stazione.setUtm32TEst(v));
		    childSdp.parse();
		    stazioni.put(stazione.getId(), stazione);
		}
	    }
	    return stazioni.entrySet();
	} catch (Exception e) {
	    throw e;
	} finally {
	    logout();
	}
    }

    public XMLDocumentReader elencoTipologieSensore() throws Exception {
	return elencoTipologieSensore(null);
    }

    public XMLDocumentReader elencoTipologieSensore(String stationId) throws Exception {
	login();
	try {
	    String stationXML = "";
	    if (stationId != null) {
		stationXML = "<Stazioni>\n" //
			+ "<IdStazione>" + stationId + "</IdStazione>\n" //
			+ "</Stazioni>";
	    }
	    String body = getSOAPHeader() //
		    + "<ElencoTipologieSensore xmlns=\"http://tempuri.org/\"\n" //
		    + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" //
		    + "    xsi:schemaLocation=\"http://tempuri.org/ file:/home/boldrini/his-central/xsd20.xsd\">\n" //
		    + "    <xInput>\n" //
		    + "        <ElencoTipologieSensore>\n" //
		    + getTokenXML() //
		    + stationXML //
		    + "        </ElencoTipologieSensore>\n" //
		    + "    </xInput>\n" //
		    + "</ElencoTipologieSensore>" //
		    + getSOAPFooter();

	    XMLDocumentReader bodyReader = new XMLDocumentReader(body);
	    HttpResponse<InputStream> response = submit(Operation.ELENCO_TIPOLOGIE_SENSORE, bodyReader);
	    XMLDocumentReader reader = new XMLDocumentReader(response.body());
	    return reader;
	} catch (Exception e) {
	    throw e;
	} finally {
	    logout();
	}
    }

    public void elencoSensori() throws Exception {
	elencoSensori(null);
    }

    public List<Sensore> elencoSensori(String stationId) throws Exception {
	login();
	try {
	    String stationXML = "";
	    if (stationId != null) {
		stationXML = "<Stazioni>\n" //
			+ "<IdStazione>" + stationId + "</IdStazione>\n" //
			+ "</Stazioni>";
	    }
	    String body = getSOAPHeader() //
		    + "<ElencoSensori xmlns=\"http://tempuri.org/\"\n" //
		    + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" //
		    + "    xsi:schemaLocation=\"http://tempuri.org/ file:/home/boldrini/his-central/xsd20.xsd\">\n" //
		    + "    <xInput>\n" //
		    + "        <ElencoSensori>\n" //
		    + getTokenXML() //
		    + stationXML //
		    + "        </ElencoSensori>\n" //
		    + "    </xInput>\n" //
		    + "</ElencoSensori>" //
		    + getSOAPFooter();

	    XMLDocumentReader bodyReader = new XMLDocumentReader(body);
	    HttpResponse<InputStream> response = submit(Operation.ELENCO_SENSORI, bodyReader);
	    XMLDocumentReader reader = new XMLDocumentReader(response.body());
	    // System.out.println(reader.toString());
	    List<Sensore> ret = new ArrayList<>();
	    Node[] sensoreNodes = reader.evaluateNodes("//*:Sensore");
	    for (Node sensoreNode : sensoreNodes) {
		String id = reader.evaluateString(sensoreNode, "*:Anagrafica/*:IdSensore");
		String nome = reader.evaluateString(sensoreNode, "*:Anagrafica/*:NomeSensore");
		String idStato = reader.evaluateString(sensoreNode, "*:Anagrafica/*:Stato/@IdStato");
		String idTipoSensore = reader.evaluateString(sensoreNode, "*:Anagrafica/*:TipoSensore/@IdTipoSensore");
		String idStazione = reader.evaluateString(sensoreNode, "*:Anagrafica/*:Stazione/@IdStazione");
		String unitaMisura = reader.evaluateString(sensoreNode, "*:Anagrafica/*:UnitaMisura");
		String frequenza = reader.evaluateString(sensoreNode, "*:Anagrafica/*:Frequenza");
		String quota = reader.evaluateString(sensoreNode, "*:Anagrafica/*:Quota");
		String utmNord = reader.evaluateString(sensoreNode, "*:Anagrafica/*:UTM_Nord");
		String utmEst = reader.evaluateString(sensoreNode, "*:Anagrafica/*:UTM_Est");

		Node[] datiNodes = reader.evaluateNodes(sensoreNode, "*:DatiDisponibili/*:Standard/*:Dato");
		for (Node datiNode : datiNodes) {
		    Sensore sensore = new Sensore();
		    String idFunzione = reader.evaluateString(datiNode, "@IdFunzione");
		    String idOperatore = reader.evaluateString(datiNode, "@IdOperatore");
		    String idPeriodo = reader.evaluateString(datiNode, "@IdPeriodo");
		    String da = reader.evaluateString(datiNode, "*:Disponibile/@Da");
		    String a = reader.evaluateString(datiNode, "*:Disponibile/@A");
		    if (da == null || da.isEmpty()) {
			GSLoggerFactory.getLogger(getClass()).warn("Empty start date");
			continue;
		    }
		    if (a == null || a.isEmpty()) {
			GSLoggerFactory.getLogger(getClass()).warn("Empty end date");
			continue;
		    }
		    sensore.setId(id);
		    sensore.setNome(nome);
		    sensore.setIdStato(idStato);
		    sensore.setIdTipoSensore(idTipoSensore);
		    sensore.setIdStazione(idStazione);
		    sensore.setUnitaMisura(unitaMisura);
		    sensore.setFrequenza(frequenza);
		    sensore.setQuota(quota);
		    sensore.setUtm32TNord(utmNord);
		    sensore.setUtm32TEst(utmEst);
		    sensore.setIdFunzione(idFunzione);
		    sensore.setIdOperatore(idOperatore);
		    sensore.setIdPeriodo(idPeriodo);
		    sensore.setFrom(da);
		    sensore.setTo(a);
		    ret.add(sensore);
		}
	    }

	    return ret;
	} catch (Exception e) {
	    throw e;
	} finally {
	    logout();
	}
    }

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
	sdf.setTimeZone(TimeZone.getTimeZone(ZoneOffset.ofHours(1)));
    }

    public void rendiDatiTempoReale(String sensorId, ID_FUNZIONE functionId, ID_OPERATORE operatorId, ID_PERIODO periodId, Date start,
	    Date end) throws Exception {
	login();
	try {
	    String body = getSOAPHeader() //
		    + "<RendiDatiTempoReale xmlns=\"http://tempuri.org/\">\n" //
		    + "    <xInput>\n" //
		    + "        <RendiDatiTempoReale>\n" //
		    + getTokenXML() //
		    + "<Sensore>\n" //
		    + "<IdSensore>" + sensorId + "</IdSensore>\n" //
		    + "<IdFunzione>" + functionId.getId() + "</IdFunzione>\n" //
		    + "<IdOperatore>" + operatorId.getId() + "</IdOperatore>\n" //
		    + "<IdPeriodo>" + periodId.getId() + "</IdPeriodo>\n" //
		    + "<DataInizio>" + sdf.format(start) + "</DataInizio>\n" //
		    + "<DataFine>" + sdf.format(end) + "</DataFine>" //
		    + "</Sensore>\n" //
		    + "        </RendiDatiTempoReale>\n" //
		    + "    </xInput>\n" //
		    + "</RendiDatiTempoReale>" //
		    + getSOAPFooter();

	    XMLDocumentReader bodyReader = new XMLDocumentReader(body);
	    HttpResponse<InputStream> response = submit(Operation.RENDI_DATI_TEMPO_REALE, bodyReader);
	    XMLDocumentReader reader = new XMLDocumentReader(response.body());
	    System.out.println(reader.asString());
	} catch (Exception e) {
	    throw e;
	} finally {
	    logout();
	}
    }

    public RendiDatiResult rendiDati(String sensorId, ID_FUNZIONE functionId, ID_OPERATORE operatorId, ID_PERIODO periodId, Date start,
	    Date end) throws Exception {
	login();
	try {
	    String functionIdString = functionId == null ? "" : "" + functionId.getId();
	    String operatorIdString = operatorId == null ? "" : "" + operatorId.getId();
	    String periodIdString = periodId == null ? "" : "" + periodId.getId();
	    String body = getSOAPHeader() //
		    + "<RendiDati xmlns=\"http://tempuri.org/\">\n" //
		    + "    <xInput>\n" //
		    + "        <RendiDati>\n" //
		    + getTokenXML() //
		    + "<Sensore>\n" //
		    + "<IdSensore>" + sensorId + "</IdSensore>\n" //
		    + "<IdFunzione>" + functionIdString + "</IdFunzione>\n" //
		    + "<IdOperatore>" + operatorIdString + "</IdOperatore>\n" //
		    + "<IdPeriodo>" + periodIdString + "</IdPeriodo>\n" //
		    + "<DataInizio>" + sdf.format(start) + "</DataInizio>\n" //
		    + "<DataFine>" + sdf.format(end) + "</DataFine>" //
		    + "</Sensore>\n" //
		    + "        </RendiDati>\n" //
		    + "    </xInput>\n" //
		    + "</RendiDati>" //
		    + getSOAPFooter();

	    XMLDocumentReader bodyReader = new XMLDocumentReader(body);
	    HttpResponse<InputStream> response = submit(Operation.RENDI_DATI, bodyReader);
	    RendiDatiResult ret = new RendiDatiResult(response.body());
	    return ret;
	} catch (Exception e) {
	    throw e;
	} finally {
	    logout();
	}
    }

    private String getEndpoint(String path) {
	URL ret;
	try {
	    ret = new URL(endpoint, path);
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    return null;
	}
	return ret.toString().trim();
    }

    private HttpResponse<InputStream> submit(Operation operation, XMLDocumentReader body) throws Exception {
	String path = operation.getPath().getPath();
	String soapAction = soapActions.get(operation);
	return submit(path, soapAction, body);
    }

    private HttpResponse<InputStream> submit(String path, String soapAction, XMLDocumentReader body) throws Exception {
	String url = getEndpoint(path);
	url = URLEncoder.encode(url, "UTF-8");
	String proxyEndpoint = getGiProxyEndpoint();

	if (proxyEndpoint == null) {
	    throw new RuntimeException("GI-proxy not configured");
	}

	HttpRequest request = null;

	HashMap<String, String> headers = new HashMap<String, String>();
	headers.put("Content-Type", "text/xml;charset=UTF-8");

	if (soapAction != null) {

	    headers.put("SOAPAction", soapAction);
	}

	if (body == null) {
	    // HTTP GET
	    url = proxyEndpoint + "/get?url=" + url + "&keystorePassword=" + keystorePassword;
	    request = HttpRequestUtils.build(MethodNoBody.GET, url);

	} else {
	    // HTTP POST
	    url = proxyEndpoint + "/post?url=" + url + "&keystorePassword=" + keystorePassword;

	    request = HttpRequestUtils.build(MethodWithBody.POST, 

		    url, body.asString().getBytes(StandardCharsets.UTF_8),

		    HttpHeaderUtils.build(headers));
	}

	GSLoggerFactory.getLogger(getClass()).info("Sending request to HIS-Lombardia: {} {}", url, soapAction);

	Downloader executor = new Downloader();
	executor.setConnectionTimeout(TimeUnit.SECONDS, 60);
	executor.setResponseTimeout(TimeUnit.SECONDS, 240);
	return executor.downloadResponse(request);

    }

    private String getSOAPHeader() {
	return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:inc=\"http://www.service-now.com/incident\" xmlns:tem=\"http://tempuri.org/\">\n"
		+ "<soapenv:Header/>\n"//
		+ "<soapenv:Body>\n";

    }

    private String getSOAPFooter() {
	return "</soapenv:Body>\n"//
		+ "</soapenv:Envelope>\n";
    }

}
