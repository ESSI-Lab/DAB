package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

/**
 * @author Fabrizio
 */
public class TarExtractor {

    /**
    * 
    */
    private static final int BUFFER_SIZE = 8192;

    /**
     * 
     */
    private int maxEntries;
    /**
     * 
     */
    private Function<String, String> fileNameMapper;

    /**
     * 
     */
    private Timer timeOutTimer;

    /**
     * 
     */
    private TimeOutTask timeoutTask;

    /**
     * 
     */
    public TarExtractor() {

	this(-1, entryName -> entryName);
    }

    /**
     * @param maxEntries
     */
    public TarExtractor(int maxEntries) {

	this(maxEntries, entryName -> entryName);
    }

    /**
     * @param fileNameMapper
     */
    public TarExtractor(Function<String, String> fileNameMapper) {

	this(-1, fileNameMapper);
    }

    /**
     * @param maxEntries
     * @param fileNameMapper
     */
    public TarExtractor(int maxEntries, Function<String, String> fileNameMapper) {

	setMaxEntries(maxEntries);
	setFileNameMapper(fileNameMapper);

	timeoutTask = new TimeOutTask();
    }

    /**
     * @param timeUnit
     * @param timeout
     */
    public void setTimeOut(TimeUnit unit, int timeout) {

	timeOutTimer = new Timer();

	timeoutTask.setTimeUnit(unit);
	timeoutTask.setTimeout(timeout);
    }

    /**
     * @return the maxEntries
     */
    public Optional<Integer> getMaxEntries() {

	return maxEntries > 0 ? Optional.of(maxEntries) : Optional.empty();
    }

    /**
     * @param maxEntries the maxEntries to set
     */
    public void setMaxEntries(int maxEntries) {

	this.maxEntries = maxEntries;
    }

    /**
     * @return the fileNameMapper
     */
    public Function<String, String> getFileNameMapper() {

	return fileNameMapper;
    }

    /**
     * @param fileNameMapper the fileNameMapper to set
     */
    public void setFileNameMapper(Function<String, String> fileNameMapper) {

	this.fileNameMapper = fileNameMapper;
    }

    /**
     * @param gzInputStream
     * @param destDirectory
     * @param closeStream
     * @return
     * @throws TimeoutException
     */
    public List<File> extract(InputStream gzInputStream, File destDirectory, boolean closeStream) throws TimeoutException {

	GSLoggerFactory.getLogger(TarExtractor.class).debug("Extracting tar entries in {} STARTED", destDirectory.getAbsolutePath());

	if (timeOutTimer != null) {

	    timeOutTimer.schedule(timeoutTask, timeoutTask.getDelay());
	}

	List<File> out = new ArrayList<>();

	TarArchiveInputStream tarStream = null;

	GZIPInputStream gzipStream = null;

	int entriesCount = 0;

	if (maxEntries > 0) {

	    GSLoggerFactory.getLogger(TarExtractor.class).debug("Max entries to extract: {}", maxEntries);
	}

	try {

	    gzipStream = new GZIPInputStream(gzInputStream); // .gz

	    tarStream = new TarArchiveInputStream(gzipStream); // .tar.gz

	    TarArchiveEntry tarEntry = null;

	    while ((tarEntry = tarStream.getNextTarEntry()) != null) {

		if (tarEntry.isDirectory()) {

		    GSLoggerFactory.getLogger(TarExtractor.class).debug("Tar entry {} is a directory", tarEntry.getName());

		    continue;

		} else {

		    GSLoggerFactory.getLogger(TarExtractor.class).debug("Extraction of tar entry {} STARTED", tarEntry.getName());

		    String mappedEntryName = this.fileNameMapper.apply(tarEntry.getName());

		    GSLoggerFactory.getLogger(TarExtractor.class).debug("Mapped entry name: {}", mappedEntryName);

		    File outputFile = new File(destDirectory + File.separator + mappedEntryName);
		    outputFile.getParentFile().mkdirs();

		    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

		    copy(tarStream, fileOutputStream);

		    fileOutputStream.flush();
		    fileOutputStream.close();

		    entriesCount++;

		    out.add(outputFile);

		    GSLoggerFactory.getLogger(TarExtractor.class).debug("Extraction of tar entry {} ENDED", tarEntry.getName());

		    if (maxEntries > 0 && entriesCount == maxEntries) {

			GSLoggerFactory.getLogger(TarExtractor.class).debug("Max entries {} reached", maxEntries);

			break;
		    }
		}
	    }
	} catch (IOException ex) {

	    GSLoggerFactory.getLogger(TarExtractor.class).error(ex.getMessage(), ex);

	} catch (TimeoutException e) {

	    GSLoggerFactory.getLogger(TarExtractor.class).error("Timeout occurred during tar.gz extraction");

	    throw e;

	} finally {

	    timeOutTimer = null;

	    if (closeStream) {

		try {
		    GSLoggerFactory.getLogger(TarExtractor.class).debug("Closing stream STARTED");

		    gzInputStream.close();
		    tarStream.close();

		    GSLoggerFactory.getLogger(TarExtractor.class).debug("Closing stream ENDED");

		} catch (IOException e) {

		    GSLoggerFactory.getLogger(TarExtractor.class).error(e.getMessage(), e);
		}
	    }
	}

	GSLoggerFactory.getLogger(TarExtractor.class).debug("Extracted entries: {}", entriesCount);

	GSLoggerFactory.getLogger(TarExtractor.class).debug("Extracting tar entries in {} ENDED", destDirectory.getAbsolutePath());

	return out;
    }

    /**
     * @param gzInputStream
     * @param destDirectory
     * @return
     * @throws TimeoutException 
     */
    public List<File> extract(InputStream gzInputStream, File destDirectory) throws TimeoutException {

	return extract(gzInputStream, destDirectory, true);
    }

    /**
     * @param tarFile
     * @param destFile
     * @param closeStream
     * @return
     * @throws FileNotFoundException
     * @throws TimeoutException 
     */
    public List<File> extract(File tarFile, File destFile, boolean closeStream) throws FileNotFoundException, TimeoutException {

	return extract(new FileInputStream(tarFile), destFile, closeStream);
    }

    /**
     * @param tarFile
     * @param destFile
     * @return
     * @throws FileNotFoundException
     * @throws TimeoutException
     */
    public List<File> extract(File tarFile, File destFile) throws FileNotFoundException, TimeoutException {

	return extract(new FileInputStream(tarFile), destFile);
    }

    /**
     * @author Fabrizio
     */
    private static class TimeOutTask extends TimerTask {

	public boolean isTimedOut;

	private TimeUnit timeUnit;
	private int timeout;

	@Override
	public void run() {

	    isTimedOut = true;
	}

	/**
	 * @return the isTimedOut
	 */
	public boolean isTimedOut() {

	    return isTimedOut;
	}

	/**
	 * @param timeUnit the timeUnit to set
	 */
	public void setTimeUnit(TimeUnit timeUnit) {

	    this.timeUnit = timeUnit;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout) {

	    this.timeout = timeout;
	}

	/**
	 * @return
	 */
	public long getDelay() {

	    return timeUnit.toMillis(timeout);
	}
    }

    /**
     * @param source
     * @param sink
     * @return
     * @throws IOException
     */
    private long copy(InputStream source, OutputStream sink) throws IOException, TimeoutException {
	long nread = 0L;
	byte[] buf = new byte[BUFFER_SIZE];
	int n;
	while ((n = source.read(buf)) > 0) {

	    if (timeoutTask.isTimedOut()) {

		throw new TimeoutException();
	    }

	    sink.write(buf, 0, n);
	    nread += n;
	}
	return nread;
    }
}
