package eu.essi_lab.services.impl;

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
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.FileUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.zip.TarExtractor;
import eu.essi_lab.services.message.MessageChannel;

/**
 * Downloads and runs Prometheus in "agent" mode, generating a tailored configuration file.
 * <p>
 * Configure via {@link eu.essi_lab.services.ManagedServiceSetting} key-value options (Java
 * {@link Properties}); see {@link OptionKeys} for required property names.
 */
public class PrometheusAgentService extends AbstractManagedService {

    /**
     * Property keys for {@link eu.essi_lab.services.ManagedServiceSetting} key-value options.
     */
    public static final class OptionKeys {

	public static final String DOWNLOAD_URL = "DOWNLOAD_URL";
	public static final String SCRAPE_INTERVAL = "SCRAPE_INTERVAL";
	public static final String JOB_NAME = "JOB_NAME";
	public static final String SCHEME = "SCHEME";
	public static final String METRICS_PATH = "METRICS_PATH";
	public static final String TARGET = "TARGET";
	public static final String REMOTE_WRITE_URL = "REMOTE_WRITE_URL";
	public static final String AWS_REGION = "AWS_REGION";
	public static final String LISTEN_PORT = "LISTEN_PORT";

	private OptionKeys() {
	}
    }

    private static final String AGENT_ROOT_DIR_NAME = "prometheus-agent";

    private volatile boolean running;
    private final AtomicReference<Process> processRef = new AtomicReference<>();

    @Override
    public void start() {

	running = true;

	publish(MessageChannel.MessageLevel.INFO, "Prometheus agent service STARTED: " + getId());

	Optional<Properties> taskOptions = getSetting().getKeyValueOptions();
	if (taskOptions.isEmpty() || taskOptions.get().isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("No key-value options specified for Prometheus agent service");
	    publish(MessageChannel.MessageLevel.ERROR, "No options specified for Prometheus agent service");
	    return;
	}

	Properties options = taskOptions.get();
	String downloadUrl;
	String scrapeInterval;
	String jobName;
	String scheme;
	String metricsPath;
	String target;
	String remoteWriteUrl;
	String awsRegion;
	int listenPort;
	try {
	    downloadUrl = requireProp(options, OptionKeys.DOWNLOAD_URL);
	    scrapeInterval = requireProp(options, OptionKeys.SCRAPE_INTERVAL);
	    jobName = requireProp(options, OptionKeys.JOB_NAME);
	    scheme = requireProp(options, OptionKeys.SCHEME);
	    metricsPath = requireProp(options, OptionKeys.METRICS_PATH);
	    target = requireProp(options, OptionKeys.TARGET);
	    remoteWriteUrl = requireProp(options, OptionKeys.REMOTE_WRITE_URL);
	    awsRegion = requireProp(options, OptionKeys.AWS_REGION);
	    listenPort = Integer.parseInt(requireProp(options, OptionKeys.LISTEN_PORT));
	} catch (NumberFormatException e) {
	    publish(MessageChannel.MessageLevel.ERROR,
		    "Invalid " + OptionKeys.LISTEN_PORT + ": " + e.getMessage());
	    return;
	} catch (IllegalArgumentException e) {
	    publish(MessageChannel.MessageLevel.ERROR, e.getMessage());
	    return;
	}

	String safeJobName = FileUtils.sanitizeForNtfs(jobName);

	Path javaTmpDir = IOStreamUtils.getUserTempDirectory().toPath();
	File agentRootDir = javaTmpDir.resolve(AGENT_ROOT_DIR_NAME).toFile();

	File prometheusCacheDir = getCacheDir(agentRootDir, downloadUrl);
	File prometheusBinary;
	try {
	    prometheusBinary = getOrDownloadAndExtractPrometheus(downloadUrl, prometheusCacheDir);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Prometheus agent setup failed", e);
	    publish(MessageChannel.MessageLevel.ERROR, "Prometheus agent setup failed: " + e.getMessage());
	    return;
	}
	if (prometheusBinary == null) {
	    return;
	}

	File prometheusBinaryDir = prometheusBinary.getParentFile();
	File configFile = new File(prometheusBinaryDir, "prometheus-" + safeJobName + ".yml");
	String yaml = buildPrometheusAgentConfigYaml(scrapeInterval, jobName, scheme, metricsPath, target, remoteWriteUrl,
		awsRegion);
	try {
	    Files.writeString(configFile.toPath(), yaml, StandardCharsets.UTF_8);
	} catch (IOException e) {
	    publish(MessageChannel.MessageLevel.ERROR, "Unable to write config: " + e.getMessage());
	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Launching Prometheus agent at port {}", listenPort);

	ProcessBuilder pb = new ProcessBuilder(//
		"./prometheus", //
		"--config.file=" + configFile.getName(), //
		"--enable-feature=agent", //
		"--web.listen-address=:" + listenPort);
	pb.directory(prometheusBinaryDir);
	pb.redirectErrorStream(true);

	Process process;
	try {
	    process = pb.start();
	} catch (IOException e) {
	    publish(MessageChannel.MessageLevel.ERROR, "Unable to start Prometheus: " + e.getMessage());
	    return;
	}

	processRef.set(process);
	startOutputLogger(process.getInputStream());
	startCancellationWatcher(process);

	int exitCode;
	try {
	    exitCode = waitForProcessToEnd(process);
	} catch (InterruptedException e) {
	    Thread.currentThread().interrupt();
	    publish(MessageChannel.MessageLevel.INFO, "Prometheus agent interrupted");
	    return;
	} finally {
	    processRef.compareAndSet(process, null);
	}

	if (!running) {
	    return;
	}
	if (exitCode != 0) {
	    publish(MessageChannel.MessageLevel.ERROR, "Prometheus agent exited with code " + exitCode);
	    return;
	}

	publish(MessageChannel.MessageLevel.INFO, "Prometheus agent service ENDED: " + getId());
    }

    @Override
    public void stop() {

	running = false;

	Process p = processRef.getAndSet(null);
	if (p != null) {
	    p.destroy();
	}

	publish(MessageChannel.MessageLevel.INFO, "Stopped Prometheus agent service: " + getId());
    }

    private static String requireProp(Properties options, String key) {

	String value = options.getProperty(key);
	if (value == null || value.trim().isEmpty()) {
	    throw new IllegalArgumentException("Missing required option: " + key);
	}
	return value.trim();
    }

    private static File getCacheDir(File agentRootDir, String downloadUrl) {

	String fileName = toLastPathSegment(downloadUrl);
	String baseName = fileName;
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
	    return url.replaceAll("[^a-zA-Z0-9._-]", "_");
	}
    }

    private File getOrDownloadAndExtractPrometheus(String downloadUrl, File prometheusCacheDir) throws Exception {

	File prometheusBinary = findPrometheusBinary(prometheusCacheDir);

	if (prometheusBinary != null && prometheusBinary.canExecute()) {

	    return prometheusBinary;
	}

	File lockFile = new File(prometheusCacheDir, ".download.lock");
	lockFile.getParentFile().mkdirs();

	try (RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
		FileChannel channel = raf.getChannel(); FileLock lock = channel.lock()) {

	    prometheusBinary = findPrometheusBinary(prometheusCacheDir);
	    if (prometheusBinary != null && prometheusBinary.canExecute()) {
		return prometheusBinary;
	    }

	    if (!running) {
		return null;
	    }

	    String tarFileName = toLastPathSegment(downloadUrl);
	    File tarFile = new File(prometheusCacheDir, tarFileName);

	    GSLoggerFactory.getLogger(getClass()).info("Downloading Prometheus binary");

	    downloadTarIfNeeded(downloadUrl, tarFile);

	    File extractDir = new File(prometheusCacheDir, "extract");
	    if (extractDir.exists()) {
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
	File parent = tarFile.getParentFile();
	if (!parent.exists() && !parent.mkdirs()) {
	    throw new IOException("Unable to create directory: " + parent.getAbsolutePath());
	}

	GSLoggerFactory.getLogger(PrometheusAgentService.class).info("Downloading Prometheus from {}", downloadUrl);

	try (InputStream in = downloader.downloadOptionalStream(downloadUrl).orElseThrow(
		() -> new IOException("Unable to download Prometheus archive: " + downloadUrl))) {
	    Path tmp = tarFile.toPath().resolveSibling(tarFile.getName() + ".part");
	    Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
	    Files.move(tmp, tarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	GSLoggerFactory.getLogger(PrometheusAgentService.class).info("Downloaded Prometheus");
    }

    private File findPrometheusBinary(File rootDir) throws IOException {

	GSLoggerFactory.getLogger(getClass()).info("Finding Prometheus binary");
	if (rootDir == null || !rootDir.exists()) {
	    GSLoggerFactory.getLogger(getClass()).info("Prometheus binary not found");
	    return null;
	}

	Path root = rootDir.toPath();
	try (var stream = Files.walk(root)) {
	    File ret = stream//
		    .filter(p -> Files.isRegularFile(p))//
		    .filter(p -> p.getFileName().toString().equals("prometheus"))//
		    .map(Path::toFile)//
		    .findFirst()//
		    .orElse(null);
	    if (ret == null) {
		GSLoggerFactory.getLogger(getClass()).info("Prometheus binary not found");
	    } else {
		GSLoggerFactory.getLogger(getClass()).info("Prometheus binary found");
	    }
	    return ret;
	}
    }

    private static String buildPrometheusAgentConfigYaml(String scrapeInterval, String jobName, String scheme,
	    String metricsPath, String target, String remoteWriteUrl, String awsRegion) {

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

    private void startOutputLogger(InputStream inputStream) {

	Thread t = new Thread(() -> {
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
		String line;
		while ((line = br.readLine()) != null) {
		    if (!running) {
			break;
		    }
		    GSLoggerFactory.getLogger(getClass()).debug("[prometheus] {}", line);
		}
	    } catch (IOException e) {
		GSLoggerFactory.getLogger(getClass()).debug("Prometheus output logger stopped: {}", e.getMessage());
	    }
	});
	t.setDaemon(true);
	t.start();
    }

    private void startCancellationWatcher(Process process) {

	Thread t = new Thread(() -> {
	    try {
		while (process.isAlive()) {
		    if (!running) {
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

    private int waitForProcessToEnd(Process process) throws InterruptedException {

	while (process.isAlive()) {
	    if (!running) {
		process.destroy();
		break;
	    }
	    TimeUnit.SECONDS.sleep(1);
	}
	return process.waitFor();
    }
}
