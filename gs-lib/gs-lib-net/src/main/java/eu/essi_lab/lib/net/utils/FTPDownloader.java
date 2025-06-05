package eu.essi_lab.lib.net.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author roncella
 */
public class FTPDownloader implements Serializable {

    private Integer timeOut = 15000;

    private FTPClient ftpClient;

    private String host;

    private String path;

    /**
     * @param timeOut
     */
    public void setTimeout(Integer timeOut) {

	this.timeOut = timeOut;
    }

    /**
     * @param url
     * @return
     * @throws IOException
     * @throws SocketException
     */
    public Integer getFolderSize(String url) throws SocketException, IOException {

	List<String> names = new ArrayList<String>();

	FTPconnect(url);

	FTPFileFilter filter = new FTPFileFilter() {

	    @Override
	    public boolean accept(FTPFile file) {
		return !file.isDirectory();
	    }
	};
	FTPFile[] files = ftpClient.listFiles(this.path, filter);
	for (FTPFile file : files) {
	    String name = file.getName();
	    if (!(name.contains(".Xauthority") || name.contains(".bash_history")))
		names.add(name);
	}

	ftpClient.disconnect();
	return names.size();

    }

    /**
     * @param url
     * @return
     */
    public List<String> downloadFileNames(String url) {

	List<String> names = new ArrayList<String>();
	try {

	    FTPconnect(url);

	    FTPFileFilter filter = new FTPFileFilter() {

		@Override
		public boolean accept(FTPFile file) {
		    return !file.isDirectory();
		}
	    };
	    FTPFile[] files = ftpClient.listFiles(this.path, filter);
	    for (FTPFile file : files) {
		String name = file.getName();
		if (!(name.contains(".Xauthority") || name.contains(".bash_history")))
		    names.add(name);
	    }

	    ftpClient.disconnect();

	} catch (UnsupportedOperationException | IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return names;

    }

    public boolean checkConnection(String url) throws SocketException, IOException {
	URL hosturl = new URL(url);

	String userInfo = hosturl.getUserInfo();
	if (userInfo == null) {
	    userInfo = "";
	}

	String host = hosturl.getHost();
	String path = hosturl.getPath();

	String user = "";
	String pwd = "";

	if (ftpClient == null)
	    ftpClient = new FTPClient();

	ftpClient.setConnectTimeout(0);

	ftpClient.connect(host);

	if (!userInfo.equals("")) {

	    StringTokenizer tok = new StringTokenizer(userInfo, ":");
	    int count = tok.countTokens();
	    user = tok.nextToken();
	    pwd = count == 2 ? tok.nextToken() : "";
	}

	boolean success = ftpClient.login(user, pwd);
	return success;
    }

    private void FTPconnect(String url) throws SocketException, IOException {

	URL hosturl = new URL(url);

	String userInfo = hosturl.getUserInfo();
	if (userInfo == null) {
	    userInfo = "";
	}

	this.host = hosturl.getHost();
	this.path = hosturl.getPath();

	String user = "anonymous";
	String pwd = "";

	if (ftpClient == null)
	    ftpClient = new FTPClient();

	ftpClient.setConnectTimeout(0);

	ftpClient.connect(host);

	if (!userInfo.equals("")) {

	    StringTokenizer tok = new StringTokenizer(userInfo, ":");
	    int count = tok.countTokens();
	    user = tok.nextToken();
	    pwd = count == 2 ? tok.nextToken() : "";
	}

	boolean login = ftpClient.login(user, pwd);

	if (!login) {
	    String code = readReplyCode();
	    if (isErrorCode(code)) {
		try {
		    ftpClient.disconnect();
		} catch (IOException e) {
		    // not so bad...
		    GSLoggerFactory.getLogger(getClass()).info("Error: " + e);
		}
		throw new RuntimeException("Error during login : [" + code + "]");
	    }
	}

	ftpClient.enterLocalPassiveMode();
	ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

	// ftpClient.changeWorkingDirectory("/");

    }

    /***
     * @param url
     * @param remoteFilename
     * @param localFile
     * @return
     */
    public File downloadToFile(String url, String remoteFilename, File localFile) {

	return downloadToFile(url, remoteFilename, localFile, true);
    }

    /***
     * @param url
     * @param remoteFilename
     * @param localFile
     * @return
     */
    public File downloadToFile(String url, String remoteFilename, File localFile, boolean completePending) {
	InputStream is;
	try {
	    FTPconnect(url);
	    String prefix = remoteFilename;
	    String suffix = ".bin";
	    if (remoteFilename.contains(".")) {
		int i = remoteFilename.lastIndexOf('.');
		prefix = remoteFilename.substring(0, i);
		suffix = remoteFilename.substring(i);
	    }
	    if (localFile == null) {
		localFile = File.createTempFile(prefix, suffix);
	    }

	    if (path != null) {

		boolean changed = ftpClient.changeWorkingDirectory(path);
		if (changed) {
		    GSLoggerFactory.getLogger(getClass()).info("Changed to path: " + path);
		} else {
		    GSLoggerFactory.getLogger(getClass()).info("Unable to change to path: " + path);
		}
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Receiving file stream STARTED");

	    is = ftpClient.retrieveFileStream(remoteFilename);

	    GSLoggerFactory.getLogger(getClass()).info("Receiving file stream ENDED");

	    GSLoggerFactory.getLogger(getClass()).info("Copy to local file STARTED");

	    Files.copy(is, localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

	    GSLoggerFactory.getLogger(getClass()).info("Copy to local file ENDED");

	    if (completePending) {
		GSLoggerFactory.getLogger(getClass()).info("Completed pending command STARTED");

		boolean success = ftpClient.completePendingCommand();
		if (success) {
		    GSLoggerFactory.getLogger(getClass()).info("File " + remoteFilename + " has been downloaded successfully.");
		}

		GSLoggerFactory.getLogger(getClass()).info("Completed pending command ENDED");
	    }
	    
	    is.close();

	    return localFile;

	} catch (

	IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		if (ftpClient.isConnected()) {
		    ftpClient.logout();
		    ftpClient.disconnect();
		}
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}

	return null;
    }

    /**
     * @param url
     * @param remoteFile of the file to download
     */
    public File downloadStream(String url, String remoteFile) {
	return downloadToFile(url, remoteFile, null);
    }

    public Optional<Integer> getResponseCode(String url) {

	Optional<Integer> opt = Optional.empty();

	return opt;
    }

    public static void main(String[] args) throws Exception {

	// ftp://broker:Pla645!z@ftp.inmet.gov.br/
	String host = "ftp://broker:Pla645!z@ftp.inmet.gov.br/";

	FTPDownloader ftpDownloader = new FTPDownloader();

	List<String> listNames = ftpDownloader.downloadFileNames(host);

	if (listNames.isEmpty()) {
	    System.out.println(listNames.size());
	    for (int i = 0; i < 100; i++) {
		File isRes = ftpDownloader.downloadStream(host, listNames.get(i));
		if (isRes != null) {
		    try (FileReader reader = new FileReader(isRes); BufferedReader br = new BufferedReader(reader)) {
			// read line by line
			String line;
			while ((line = br.readLine()) != null) {
			    System.out.println(line);
			}

		    }
		}
	    }
	}

    }

    private String readReplyCode() {

	String replyString = ftpClient.getReplyString();
	String codeString = replyString.substring(0, 3);
	return codeString;
    }

    private boolean isErrorCode(String code) {

	return !code.startsWith("2");
    }

}
