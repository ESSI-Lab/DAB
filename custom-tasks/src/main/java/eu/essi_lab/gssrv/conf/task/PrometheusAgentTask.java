package eu.essi_lab.gssrv.conf.task;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.FileUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.zip.TarExtractor;

/**
 * Downloads and runs Prometheus in "agent" mode, generating a tailored configuration file.
 */
public class PrometheusAgentTask extends AbstractCustomTask {

    private static final String AGENT_ROOT_DIR_NAME = "prometheus-agent";

    public enum PrometheusAgentTaskOptions implements OptionsKey {
	DOWNLOAD_URL,
	SCRAPE_INTERVAL,
	JOB_NAME,
	SCHEME,
	METRICS_PATH,
	TARGET,
	REMOTE_WRITE_URL,
	AWS_REGION,
	LISTEN_PORT;
    }

    @Override
    public String getName() {
	return "Prometheus agent task";
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	log(status, "Prometheus agent task STARTED");

	Optional<EnumMap<PrometheusAgentTaskOptions, String>> taskOptions = readTaskOptions(context,
		PrometheusAgentTaskOptions.class);
	if (taskOptions.isEmpty() || taskOptions.get().isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("No options specified for Prometheus agent task");
	    status.setPhase(JobPhase.CANCELED);
	    return;
	}

	EnumMap<PrometheusAgentTaskOptions, String> options = taskOptions.get();
	String downloadUrl = requireOption(options, PrometheusAgentTaskOptions.DOWNLOAD_URL);
	String scrapeInterval = requireOption(options, PrometheusAgentTaskOptions.SCRAPE_INTERVAL);
	String jobName = requireOption(options, PrometheusAgentTaskOptions.JOB_NAME);
	String scheme = requireOption(options, PrometheusAgentTaskOptions.SCHEME);
	String metricsPath = requireOption(options, PrometheusAgentTaskOptions.METRICS_PATH);
	String target = requireOption(options, PrometheusAgentTaskOptions.TARGET);
	String remoteWriteUrl = requireOption(options, PrometheusAgentTaskOptions.REMOTE_WRITE_URL);
	String awsRegion = requireOption(options, PrometheusAgentTaskOptions.AWS_REGION);
	int listenPort = Integer.parseInt(requireOption(options, PrometheusAgentTaskOptions.LISTEN_PORT));

	String safeJobName = FileUtils.sanitizeForNtfs(jobName);

	Path javaTmpDir = IOStreamUtils.getUserTempDirectory().toPath();
	File agentRootDir = javaTmpDir.resolve(AGENT_ROOT_DIR_NAME).toFile();

	// 1) cache download/extraction (shared across jobs, as long as URL is the same)
	File prometheusCacheDir = getCacheDir(agentRootDir, downloadUrl);
	File prometheusBinary = getOrDownloadAndExtractPrometheus(status, context, downloadUrl, prometheusCacheDir);
	if (prometheusBinary == null) {
	    // canceled before we could start the agent
	    return;
	}

	// 2) generate agent config for this specific job
	File prometheusBinaryDir = prometheusBinary.getParentFile();
	File configFile = new File(prometheusBinaryDir, "prometheus-" + safeJobName + ".yml");
	String yaml = buildPrometheusAgentConfigYaml(scrapeInterval, jobName, scheme, metricsPath, target,
		remoteWriteUrl, awsRegion);
	Files.writeString(configFile.toPath(), yaml, StandardCharsets.UTF_8);

	log(status, "Launching Prometheus agent at port " + listenPort, false);

	// 3) start the external prometheus process
	ProcessBuilder pb = new ProcessBuilder(//
		"./prometheus", //
		"--config.file=" + configFile.getName(), //
		"--enable-feature=agent", //
		"--web.listen-address=:" + listenPort);
	pb.directory(prometheusBinaryDir);
	pb.redirectErrorStream(true);

	// Start a process output logger so the child won't block due to full buffers.
	Process process = pb.start();
	startOutputLogger(process.getInputStream(), context);

	// Cancellation watcher: if the job is disabled/removed, stop the agent.
	startCancellationWatcher(process, status, context);

	int exitCode = waitForProcessToEnd(process, status, context);
	if (status.getPhase() == JobPhase.CANCELED || ConfigurationWrapper.isJobCanceled(context)) {
	    return;
	}
	if (exitCode != 0) {
	    throw new IOException("Prometheus agent exited with code " + exitCode);
	}

	log(status, "Prometheus agent task ENDED");
    }

    private static String requireOption(EnumMap<PrometheusAgentTaskOptions, String> options,
	    PrometheusAgentTaskOptions key) {
	String value = options.get(key);
	if (value == null || value.trim().isEmpty()) {
	    throw new IllegalArgumentException("Missing required option: " + key.name());
	}
	return value.trim();
    }

    private static File getCacheDir(File agentRootDir, String downloadUrl) {
	String fileName = toLastPathSegment(downloadUrl);
	String baseName = fileName;
	// remove common archive suffixes
	if (baseName.endsWith(".tar.gz")) {
	    baseName = baseName.substring(0, baseName.length() - ".tar.gz".length());
	} else if (baseName.endsWith(".tgz")) {
	    baseName = baseName.substring(0, baseName.length() - ".tgz".length());
	}

	File cacheDir = new File(agentRootDir, "cache" + File.separator + baseName);
	if (!cacheDir.exists()) {
	    cacheDir.mkdirs();
	}
	return cacheDir;
    }

    private static String toLastPathSegment(String url) {
	try {
	    String path = new java.net.URI(url).getPath();
	    int idx = path.lastIndexOf('/');
	    return idx >= 0 ? path.substring(idx + 1) : path;
	} catch (Exception e) {
	    // fallback: no path parsing, but keep it stable
	    return url.replaceAll("[^a-zA-Z0-9._-]", "_");
	}
    }

    private File getOrDownloadAndExtractPrometheus(SchedulerJobStatus status, JobExecutionContext context,
	    String downloadUrl, File prometheusCacheDir) throws Exception {

	File prometheusBinary = findPrometheusBinary(prometheusCacheDir);
	if (prometheusBinary != null && prometheusBinary.canExecute()) {
	    return prometheusBinary;
	}

	// Serialize downloads/extraction for the same cache directory.
	File lockFile = new File(prometheusCacheDir, ".download.lock");
	lockFile.getParentFile().mkdirs();

	try (RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
		FileChannel channel = raf.getChannel(); FileLock lock = channel.lock()) {

	    // Double-check after acquiring lock.
	    prometheusBinary = findPrometheusBinary(prometheusCacheDir);
	    if (prometheusBinary != null && prometheusBinary.canExecute()) {
		return prometheusBinary;
	    }

	    if (ConfigurationWrapper.isJobCanceled(context)) {
		status.setPhase(JobPhase.CANCELED);
		return null;
	    }

	    String tarFileName = toLastPathSegment(downloadUrl);
	    File tarFile = new File(prometheusCacheDir, tarFileName);

	    downloadTarIfNeeded(downloadUrl, tarFile);

	    // Extract into a dedicated directory inside the cache dir.
	    File extractDir = new File(prometheusCacheDir, "extract");
	    if (extractDir.exists()) {
		// Keep it simple: ensure an old partial extraction doesn't break us.
		// (We don't delete aggressively if it's already usable.)
		if (findPrometheusBinary(extractDir) == null) {
		    IOStreamUtils.deleteDirectory(extractDir.toPath().toFile());
		}
	    }
	    extractDir.mkdirs();

	    GSLoggerFactory.getLogger(getClass()).info("Extracting Prometheus from {}", tarFile.getAbsolutePath());
	    TarExtractor extractor = new TarExtractor();
	    extractor.extract(tarFile, extractDir, true);

	    prometheusBinary = findPrometheusBinary(extractDir);
	    if (prometheusBinary == null) {
		throw new IOException("Unable to find prometheus binary after extraction");
	    }
	    prometheusBinary.setExecutable(true);

	    return prometheusBinary;
	}
    }

    private static void downloadTarIfNeeded(String downloadUrl, File tarFile) throws Exception {
	if (tarFile.exists() && tarFile.length() > 0) {
	    return;
	}

	Downloader downloader = new Downloader();
	// downloader already retries on non-200 responses if configured by caller; we keep it simple.
	// We download the response body into the target file.
	File parent = tarFile.getParentFile();
	if (!parent.exists() && !parent.mkdirs()) {
	    throw new IOException("Unable to create directory: " + parent.getAbsolutePath());
	}

	GSLoggerFactory.getLogger(PrometheusAgentTask.class).info("Downloading Prometheus from {}", downloadUrl);

	try (InputStream in = downloader.downloadOptionalStream(downloadUrl).orElseThrow(
		() -> new IOException("Unable to download Prometheus archive: " + downloadUrl))) {
	    Path tmp = tarFile.toPath().resolveSibling(tarFile.getName() + ".part");
	    Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
	    Files.move(tmp, tarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
    }

    private File findPrometheusBinary(File rootDir) throws IOException {
	if (rootDir == null || !rootDir.exists()) {
	    return null;
	}

	// Search for an executable named "prometheus".
	Path root = rootDir.toPath();
	try (var stream = Files.walk(root)) {
	    return stream//
		    .filter(p -> Files.isRegularFile(p))//
		    .filter(p -> p.getFileName().toString().equals("prometheus"))//
		    .map(Path::toFile)//
		    .findFirst()//
		    .orElse(null);
	}
    }

    private static String buildPrometheusAgentConfigYaml(String scrapeInterval, String jobName, String scheme,
	    String metricsPath, String target, String remoteWriteUrl, String awsRegion) {
	// YAML generation intentionally matches the example provided by the user.
	return "global:\n" //
		+ "  scrape_interval: " + scrapeInterval + "\n" //
		+ "# Define the scrape configurations\n" //
		+ "scrape_configs:\n" //
		+ "  - job_name: '" + jobName + "'\n" //
		+ "    scheme: '" + scheme + "'\n" //
		+ "    metrics_path: '" + metricsPath + "'\n" //
		+ "    static_configs:\n" //
		+ "      - targets: ['" + target + "']\n" //
		+ "\n" //
		+ "remote_write:\n" //
		+ "  - url: \"" + remoteWriteUrl + "\"\n" //
		+ "    sigv4:\n" //
		+ "      region: \"" + awsRegion + "\"\n";
    }

    private void startOutputLogger(InputStream inputStream, JobExecutionContext context) {
	Thread t = new Thread(() -> {
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
		String line;
		while ((line = br.readLine()) != null) {
		    if (ConfigurationWrapper.isJobCanceled(context)) {
			break;
		    }
		    GSLoggerFactory.getLogger(getClass()).debug("[prometheus] {}", line);
		}
	    } catch (IOException e) {
		// Process is being stopped or stream interrupted; we just log and exit the logger thread.
		GSLoggerFactory.getLogger(getClass()).debug("Prometheus output logger stopped: {}", e.getMessage());
	    }
	});
	t.setDaemon(true);
	t.start();
    }

    private void startCancellationWatcher(Process process, SchedulerJobStatus status, JobExecutionContext context) {
	Thread t = new Thread(() -> {
	    try {
		while (process.isAlive()) {
		    if (ConfigurationWrapper.isJobCanceled(context)) {
			status.setPhase(JobPhase.CANCELED);
			process.destroy();
			break;
		    }
		    Thread.sleep(2000);
		}
	    } catch (InterruptedException e) {
		Thread.currentThread().interrupt();
	    }
	});
	t.setDaemon(true);
	t.start();
    }

    private int waitForProcessToEnd(Process process, SchedulerJobStatus status, JobExecutionContext context)
	    throws InterruptedException {
	while (process.isAlive()) {
	    if (ConfigurationWrapper.isJobCanceled(context)) {
		status.setPhase(JobPhase.CANCELED);
		process.destroy();
		break;
	    }
	    TimeUnit.SECONDS.sleep(1);
	}
	return process.waitFor();
    }
}

