package eu.essi_lab.gssrv.rest;

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

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.configuration.*;
import eu.essi_lab.gssrv.health.*;
import eu.essi_lab.lib.utils.Chronometer.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.web.*;
import eu.essi_lab.model.*;
import eu.essi_lab.pdk.*;
import eu.essi_lab.rip.*;
import eu.essi_lab.shared.driver.es.stats.*;
import jakarta.jws.*;
import jakarta.servlet.http.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.*;
import jakarta.xml.ws.*;

import java.io.*;
import java.lang.management.*;
import java.nio.file.*;
import java.text.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
@WebService
@Path("/")
public class HealthCheckService implements RuntimeInfoProvider {

    /**
     *
     */
    private static final Integer DEFAULT_DISK_SIZE_THRESHOLD = 5;

    /**
     *
     */
    private static final Integer DEFAULT_THREADS_COUNT_THRESHOLD = 300;

    /**
     *
     */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

    static {

	DECIMAL_FORMAT.setGroupingUsed(true);
	DECIMAL_FORMAT.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ITALIAN));
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/status")
    public Response status(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @Context WebServiceContext wsContext) {

	StringBuilder html = new StringBuilder(32768);

	html.append("""
		<!DOCTYPE html>
		<html>
		<head>
		<meta charset="UTF-8">
		<title>JVM Diagnostic</title>
		<style>
		    body {
			font-family: Arial;
			font-size: 13px;
			margin:20px;
		    }
		
		    .ok {
			background-color: #d4edda;
		    }
		
		    .warning {
			background-color: #fff3cd;
		    }
		
		    .error {
			background-color: #f8d7da;
		    }
		
		    .right {
			text-align: right;
		    }
		
		    .center {
			text-align: center;
		    }
		
		    table {
			border-collapse: collapse;
			margin-bottom:25px;
			min-width:900px;
		    }
		
		    th, td {
			border:1px solid #ccc;
			padding:4px 8px;
			text-align:left;
		    }
		
		    th {
			background:#efefef;
			cursor: pointer;
		    }		   
		
		    h2 {
			background:#1f4e79;
			color:white;
			padding:6px;
		    }
		
		    h3 {
			margin-top:25px;
		    }
		
		    .right {
			text-align:right;
		    }
		</style>
		</head>
		<body>
		<script>
		    function sortTable(n, tableId) {
		      var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
		      table = document.getElementById(tableId);
		      switching = true;
		      //Set the sorting direction to ascending:
		      dir = "asc";\s
		      /*Make a loop that will continue until
		      no switching has been done:*/
		      while (switching) {
			//start by saying: no switching is done:
			switching = false;
			rows = table.rows;
			/*Loop through all table rows (except the
			first, which contains table headers):*/
			for (i = 1; i < (rows.length - 1); i++) {
			  //start by saying there should be no switching:
			  shouldSwitch = false;
			  /*Get the two elements you want to compare,
			  one from current row and one from the next:*/
			  x = rows[i].getElementsByTagName("TD")[n];
			  y = rows[i + 1].getElementsByTagName("TD")[n];
			  /*check if the two rows should switch place,
			  based on the direction, asc or desc:*/
		
			  var xval = x.innerHTML.toLowerCase();
			  var yval = y.innerHTML.toLowerCase();
		
			  if (dir == "asc") {
		
			      if(isNaN(Number(xval))){
		
				if (xval > yval) {
		
				  shouldSwitch= true;
				  break;
				}
		
			     } else {
		
			        var xnum = Number(xval);
			        var ynum = Number(yval);
		
			        if (xnum > ynum) {
		
				  shouldSwitch= true;
				  break;
				}
			     }
		
			  } else if (dir == "desc") {
		
			      if(isNaN(Number(xval))){
		
				if (xval < yval) {
		
				  shouldSwitch= true;
				  break;
				}
		
			     } else {
		
			        var xnum = Number(xval);
			        var ynum = Number(yval);
		
			        if (xnum < ynum) {
		
				  shouldSwitch= true;
				  break;
				}
			     }
		
			  }
			}
			if (shouldSwitch) {
			  /*If a switch has been marked, make the switch
			  and mark that a switch has been done:*/
			  rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
			  switching = true;
			  //Each time a switch is done, increase this count by 1:
			  switchcount ++;     \s
			} else {
			  /*If no switching has been done AND the direction is "asc",
			  set the direction to "desc" and run the while loop again.*/
			  if (switchcount == 0 && dir == "asc") {
			    dir = "desc";
			    switching = true;
			  }
			}
		      }
		    }
		</script>
		""");

	html.append("<h1>JVM Diagnostic</h1>");
	html.append("<p>Generated: ").append(new Date()).append("</p>");

	RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
	ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
	MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
	ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
	CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();

	com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

	Runtime rt = Runtime.getRuntime();

	//------------------------------------------
	// Runtime
	//------------------------------------------

	html.append("<h2>Runtime</h2><table>");

	row(html, "Name", runtime.getName());
	row(html, "Host", HostNamePropertyUtils.getHostNameProperty());
	row(html, "Uptime", formatMillis(runtime.getUptime()));
	row(html, "Java Version", System.getProperty("java.version"));
	row(html, "Vendor", System.getProperty("java.vendor"));
	row(html, "VM", runtime.getVmName());
	row(html, "Start Time", new Date(runtime.getStartTime()));
	row(html, "Arguments", String.join("<br>", runtime.getInputArguments()));
	row(html, "System properties", "<details><summary></summary><pre>" + System.getProperties().keySet(). //
		stream().//
		sorted().//
		map(k -> k + " = " + System.getProperties().getProperty(k.toString()) + "<br>").//
		toList().toString().replace("[", "").replace("]", "").replace(",", "") + "</pre></details>");

	row(html, "Environment", "<details><summary></summary><pre>" + System.getenv().keySet(). //
		stream().//
		sorted().//
		map(k -> k + " = " + System.getProperties().getProperty(k) + "<br>").//
		toList().toString().replace("[", "").replace("]", "").replace(",", "") + "</pre></details>");

	html.append("</table>");

	//------------------------------------------
	// OS
	//------------------------------------------

	html.append("<h2>Operating System</h2><table>");

	row(html, "Processors", os.getAvailableProcessors());
	row(html, "System Load Average", os.getSystemLoadAverage());
	row(html, "CPU Load", percent(os.getCpuLoad()));
	row(html, "Process CPU Load", percent(os.getProcessCpuLoad()));

	row(html, "Physical Memory", human(os.getTotalMemorySize()));

	row(html, "Free Physical Memory", human(os.getFreeMemorySize()));

	html.append("</table>");

	//------------------------------------------
	// Heap
	//------------------------------------------

	html.append("<h2>Memory</h2><table>");

	MemoryUsage heap = memoryBean.getHeapMemoryUsage();
	MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();

	html.append(row("Heap used", human(heap.getUsed()), memoryClass(heap.getUsed(), heap.getMax())));

	row(html, "Heap Committed", human(heap.getCommitted()));
	row(html, "Heap Max", human(heap.getMax()));

	row(html, "Non Heap Used", human(nonHeap.getUsed()));
	row(html, "Non Heap Committed", human(nonHeap.getCommitted()));

	html.append(row("Runtime used", human(rt.totalMemory() - rt.freeMemory()),
		memoryClass(rt.totalMemory() - rt.freeMemory(), rt.maxMemory())));

	row(html, "Runtime Total", human(rt.totalMemory()));

	row(html, "Runtime Max", human(rt.maxMemory()));

	html.append("</table>");

	//------------------------------------------
	// Memory pools
	//------------------------------------------

	html.append("<h2>Memory Pools</h2>");
	html.append("<table>");
	html.append("<tr><th>Name</th><th>Used</th><th>Committed</th><th>Max</th></tr>");

	for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {

	    MemoryUsage u = pool.getUsage();

	    html.append("<tr>");
	    td(html, pool.getName());
	    td(html, human(u.getUsed()));
	    td(html, human(u.getCommitted()));
	    td(html, human(u.getMax()));
	    html.append("</tr>");
	}

	html.append("</table>");

	//------------------------------------------
	// Thread summary
	//------------------------------------------

	html.append("<h2>Threads</h2><table>");

	html.append(
		row("Current", DECIMAL_FORMAT.format(threadBean.getThreadCount()), counterClass(threadBean.getThreadCount(), 200, 500)));

	row(html, "Daemon", DECIMAL_FORMAT.format(threadBean.getDaemonThreadCount()));
	row(html, "Peak", DECIMAL_FORMAT.format(threadBean.getPeakThreadCount()));
	row(html, "Total Started", DECIMAL_FORMAT.format(threadBean.getTotalStartedThreadCount()));

	html.append("</table>");

	//------------------------------------------
	// Thread
	//------------------------------------------

	Map<String, Integer> groups = new TreeMap<>();

	ThreadInfo[] infos = threadBean.dumpAllThreads(false, false);

	for (ThreadInfo info : infos) {

	    String name = info.getThreadName();

	    String prefix = name.replaceAll("-\\d+$", "");

	    int p = prefix.indexOf('-');

	    if (p > 0) {
		prefix = prefix.substring(0, p);
	    }

	    groups.merge(prefix, 1, Integer::sum);
	}

	html.append("<h3>Thread groups</h3>");
	html.append("<table id='thGroups'>");
	html.append("<tr><th onclick=\"sortTable(0,'thGroups')\">Prefix</th><th onclick=\"sortTable(1,'thGroups')\">Count</th></tr>");

	groups.entrySet().stream().sorted((a, b) -> Integer.compare(b.getValue(), a.getValue())).forEach(e -> {

	    html.append("<tr>");
	    td(html, e.getKey());
	    td(html, e.getValue());
	    html.append("</tr>");
	});

	html.append("</table>");

	html.append(renderThreadsDump(ManagementFactory.getThreadMXBean()));

	//------------------------------------------
	// Filesystem
	//------------------------------------------

	html.append("<h2>Filesystem</h2>");
	html.append("<table>");
	html.append("<tr><th>Root</th><th>Total</th><th>Free</th><th>Usable</th></tr>");

	for (File f : File.listRoots()) {

	    html.append("<tr>");
	    td(html, f.getAbsolutePath());
	    td(html, human(f.getTotalSpace()));
	    td(html, human(f.getFreeSpace()));
	    td(html, human(f.getUsableSpace()));
	    html.append("</tr>");
	}

	html.append("</table>");

	//------------------------------------------
	// GC
	//------------------------------------------

	html.append("<h2>Garbage Collectors</h2>");
	html.append("<table>");
	html.append("<tr><th>Name</th><th>Collections</th><th>Time(ms)</th></tr>");

	for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {

	    html.append("<tr>");
	    td(html, gc.getName());
	    td(html, gc.getCollectionCount());
	    td(html, gc.getCollectionTime());
	    html.append("</tr>");
	}

	html.append("</table>");

	//------------------------------------------
	// Buffer pools
	//------------------------------------------

	html.append("<h2>Buffer Pools</h2>");
	html.append("<table>");
	html.append("<tr><th>Name</th><th>Count</th><th>Memory</th></tr>");

	for (BufferPoolMXBean b : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {

	    html.append("<tr>");
	    td(html, b.getName());
	    td(html, DECIMAL_FORMAT.format(b.getCount()));
	    td(html, human(b.getMemoryUsed()));
	    html.append("</tr>");
	}

	html.append("</table>");

	//------------------------------------------
	// Classes
	//------------------------------------------

	html.append("<h2>Class Loading</h2><table>");

	row(html, "Loaded", DECIMAL_FORMAT.format(classBean.getLoadedClassCount()));
	row(html, "Total Loaded", DECIMAL_FORMAT.format(classBean.getTotalLoadedClassCount()));
	row(html, "Unloaded", DECIMAL_FORMAT.format(classBean.getUnloadedClassCount()));

	html.append("</table>");

	//------------------------------------------
	// Container
	//------------------------------------------

	html.append("<h2>Container detection</h2><table>");

	boolean docker = new File("/.dockerenv").exists();

	boolean k8s = System.getenv("KUBERNETES_SERVICE_HOST") != null;

	boolean ecs = System.getenv("ECS_CONTAINER_METADATA_URI") != null;

	row(html, "Docker", docker);
	row(html, "Kubernetes", k8s);
	row(html, "ECS", ecs);

	html.append("</table>");

	//------------------------------------------
	// JIT
	//------------------------------------------

	html.append("<h2>JIT</h2><table>");

	row(html, "Compiler", compilationBean.getName());
	row(html, "Compilation Time (seconds)", TimeUnit.MILLISECONDS.toSeconds(compilationBean.getTotalCompilationTime()));

	html.append("</table>");

	html.append("</body></html>");

	return Response.ok(html.toString()).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/check")
    public jakarta.ws.rs.core.Response health(@Context HttpServletRequest hsr, @Context UriInfo uriInfo,
	    @Context WebServiceContext wsContext) {

	WebRequest webRequest = new WebRequest();

	webRequest.setUriInfo(uriInfo);

	try {
	    webRequest.setServletRequest(hsr);

	} catch (IOException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	//
	//
	//

	ChronometerInfoProvider chronometer = new ChronometerInfoProvider(TimeFormat.MIN_SEC_MLS);
	chronometer.start();

	HealthCheck healthCheck = new HealthCheck();

	boolean healthy = new HealthCheck().isHealthy();

	checkDiskSpace();

	checkThreadsCount();

	Status status = responseStatus(healthy);

	Response response = Response.//
		status(status).//
		type(MediaType.APPLICATION_JSON_TYPE).//
		entity(healthCheck.toJson(healthy, false)).build();

	String elapsedTime = chronometer.formatElapsedTime();

	GSLoggerFactory.getLogger(getClass()).trace("HealthCheck elapsed time: {}", elapsedTime);

	//
	//
	//

	Optional<ElasticsearchInfoPublisher> publisher = ElasticsearchInfoPublisher.create(webRequest);

	if (publisher.isPresent()) {

	    try {

		publisher.get().publish(webRequest);
		publisher.get().publish(chronometer);
		publisher.get().publish(new RuntimeInfoProvider() {

		    @Override
		    public HashMap<String, List<String>> provideInfo() {
			HashMap<String, List<String>> ret = new HashMap<String, List<String>>();
			ret.put(RuntimeInfoElement.RESPONSE_STATUS.getName(), List.of("" + status.getStatusCode()));
			return ret;
		    }

		    @Override
		    public String getName() {
			return getBaseType();
		    }
		});

		publisher.get().write();

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	}

	return response;
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	return new HashMap<>();
    }

    @Override
    public String getName() {

	return getClass().getSimpleName();
    }

    @Override
    public String getBaseType() {

	return "HealthCheck";
    }

    /**
     * @param bytes
     * @return
     */
    private long getGB(long bytes) {
	bytes /= 1024;
	bytes /= 1024;
	bytes /= 1024;
	return bytes;
    }

    /**
     * @param healthy
     * @return
     */
    private Response.Status responseStatus(boolean healthy) {

	if (healthy) {
	    return Response.Status.OK;
	}

	return Response.Status.INTERNAL_SERVER_ERROR;
    }

    /**
     * @param info
     * @return
     */
    private String threadClass(ThreadInfo info) {

	Thread.State state = info.getThreadState();

	if (state == Thread.State.BLOCKED) {
	    return "error";
	}

	if (state == Thread.State.WAITING) {
	    return "warning";
	}

	return "";
    }

    /**
     * @param value
     * @param warning
     * @param error
     * @return
     */
    private String counterClass(long value, long warning, long error) {

	if (value >= error) {
	    return "error";
	}

	if (value >= warning) {
	    return "warning";
	}

	return "ok";
    }

    /**
     * @param bean
     * @return
     */
    private String renderThreadsDump(ThreadMXBean bean) {

	StringBuilder sb = new StringBuilder(20000);

	if (!bean.isObjectMonitorUsageSupported() || !bean.isSynchronizerUsageSupported()) {
	    bean = ManagementFactory.getThreadMXBean();
	}

	ThreadInfo[] infos = bean.dumpAllThreads(true, true);

	sb.append("<h3>Threads (full dump)</h3>");
	sb.append("<table id='thDump'>");
	sb.append("<tr>").append("<th onclick=\"sortTable(0,'thDump')\">ID</th>"). //
		append("<th onclick=\"sortTable(1,'thDump')\">Name</th>").//
		append("<th onclick=\"sortTable(2,'thDump')\">State</th>").//
		append("<th onclick=\"sortTable(3,'thDump')\">CPU(ms)</th>").//
		append("<th onclick=\"sortTable(4,'thDump')\">Blocked</th>").//
		append("<th onclick=\"sortTable(5,'thDump')\">Waited</th>").//
		append("<th>Stack</th>").append("</tr>");

	for (ThreadInfo ti : infos) {

	    if (ti == null) {
		continue;
	    }

	    long id = ti.getThreadId();

	    sb.append("<tr class=\"").append(threadClass(ti)).append("\">");

	    sb.append("<td>").append(id).append("</td>");
	    sb.append("<td>").append(ti.getThreadName()).append("</td>");

	    sb.append("<td>").append(ti.getThreadState()).append("</td>");

	    sb.append("<td>").append(bean.isThreadCpuTimeSupported() ? bean.getThreadCpuTime(id) / 1_000_000 : "-").append("</td>");

	    sb.append("<td>").append(ti.getBlockedCount()).append("</td>");
	    sb.append("<td>").append(ti.getWaitedCount()).append("</td>");

	    sb.append("<td><details><summary>stack</summary><pre>");

	    for (StackTraceElement el : ti.getStackTrace()) {
		sb.append(el).append("\n");
	    }

	    sb.append("</pre></details></td>");
	    sb.append("</tr>");
	}

	sb.append("</table>");

	return sb.toString();
    }

    /**
     *
     */
    private void checkThreadsCount() {

	int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();

	GSLoggerFactory.getLogger(getClass()).info("Threads count: {}", threadCount);

	Integer threshold = ConfigurationWrapper.getSystemSettings().//
		readKeyValue(SystemSetting.KeyValueOptionKeys.THREADS_COUNT_THRESHOLD).//
		map(Integer::parseInt).//
		orElse(DEFAULT_THREADS_COUNT_THRESHOLD);

	if (threadCount > threshold) {

	    Map<String, Long> byPrefix = Thread.getAllStackTraces(). //
		    keySet().//
		    stream().//
		    collect(Collectors.groupingBy(Thread::getName, Collectors.counting()));

	    ExecutionMode executionMode = ExecutionMode.get();

	    String hostname = HostNamePropertyUtils.getHostNameProperty();

	    StringBuilder stringBuilder = new StringBuilder();

	    String warn = "Warning: the number of threads " + threadCount + " is greater than " + threshold + ". " + hostname + " ("
		    + executionMode + ")\n\n";

	    stringBuilder.append(warn);

	    byPrefix.entrySet().//
		    stream().//
		    sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).//
		    forEach(entry -> stringBuilder.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n"));

	    ConfiguredSMTPClient.sendEmail(ConfiguredSMTPClient.MAIL_ALARM, "Threads count alert \n\n" + stringBuilder.toString());
	}
    }

    /**
     *
     */
    private void checkDiskSpace() {

	try {

	    java.nio.file.Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
	    FileStore fileStore = Files.getFileStore(tempDir);

	    long availableSpace = fileStore.getUsableSpace();
	    long gb = getGB(availableSpace);

	    GSLoggerFactory.getLogger(getClass()).info("Available space on temporary folder: {} GB", gb);

	    Integer threshold = ConfigurationWrapper.getSystemSettings().//
		    readKeyValue(SystemSetting.KeyValueOptionKeys.DISK_SIZE_THRESHOLD).//
		    map(Integer::parseInt).//
		    orElse(DEFAULT_DISK_SIZE_THRESHOLD);

	    if (gb < threshold) {

		GSLoggerFactory.getLogger(getClass()).error("Warning: the disk free space is below {} GB", threshold);

		if (gb < 1) {

		    ExecutionMode executionMode = ExecutionMode.get();

		    String hostname = HostNamePropertyUtils.getHostNameProperty();

		    String alarmMessage =
			    "Warning: the disk free space is below " + threshold + "GB. " + hostname + " (" + executionMode + ")";

		    GSLoggerFactory.getLogger(getClass()).error(alarmMessage);

		    ConfiguredSMTPClient.sendEmail(ConfiguredSMTPClient.MAIL_ALARM, "Disk space alert \n\n" + alarmMessage);
		}
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
    }

    /**
     * @param sb
     * @param k
     * @param v
     */
    private void row(StringBuilder sb, String k, Object v) {

	sb.append("<tr><td>").append(k).append("</td><td>").append(v).append("</td></tr>");
    }

    /**
     * @param label
     * @param value
     * @param cssClass
     * @return
     */
    private String row(String label, Object value, String cssClass) {

	String v = value == null ? "-" : escape(String.valueOf(value));

	return "<tr class=\"" + cssClass + "\">" + "<td>" + escape(label) + "</td>" + "<td>" + v + "</td>" + "</tr>";
    }

    /**
     * @param used
     * @param max
     * @return
     */
    private String memoryClass(long used, long max) {

	if (max <= 0) {
	    return "";
	}

	double p = (double) used / max;

	if (p >= 0.90) {
	    return "error";
	}

	if (p >= 0.75) {
	    return "warning";
	}

	return "ok";
    }

    /**
     * @param s
     * @return
     */
    private String escape(String s) {

	if (s == null) {

	    return "-";
	}

	return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * @param sb
     * @param v
     */
    private void td(StringBuilder sb, Object v) {

	sb.append("<td>").append(v).append("</td>");
    }

    /**
     * @param bytes
     * @return
     */
    private String human(long bytes) {

	if (bytes < 0) {

	    return "-";
	}

	double value = bytes;

	String[] units = { "B", "KB", "MB", "GB", "TB" };

	int i = 0;

	while (value > 1024 && i < units.length - 1) {

	    value /= 1024;
	    i++;
	}

	return String.format("%.2f %s", value, units[i]);
    }

    /**
     * @param value
     * @return
     */
    private String percent(double value) {

	if (value < 0) {
	    return "-";
	}

	return String.format("%.1f %%", value * 100);
    }

    /**
     * @param millis
     * @return
     */
    private String formatMillis(long millis) {

	Duration d = Duration.ofMillis(millis);

	long days = d.toDays();

	d = d.minusDays(days);

	long hours = d.toHours();

	d = d.minusHours(hours);

	long minutes = d.toMinutes();

	d = d.minusMinutes(minutes);

	long seconds = d.getSeconds();

	return String.format("%d days %02d:%02d:%02d", days, hours, minutes, seconds);
    }

}
