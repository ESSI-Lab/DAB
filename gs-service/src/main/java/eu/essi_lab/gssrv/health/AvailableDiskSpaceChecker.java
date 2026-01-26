package eu.essi_lab.gssrv.health;

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

import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import eu.essi_lab.cfga.gs.ConfiguredSMTPClient;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.HostNamePropertyUtils;

public class AvailableDiskSpaceChecker {

    public static void check() {
	try {
	    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
	    FileStore fileStore = Files.getFileStore(tempDir);

	    long availableSpace = fileStore.getUsableSpace();
	    long gb = getGB(availableSpace);

	    GSLoggerFactory.getLogger(AvailableDiskSpaceChecker.class).info("Available space on temporary folder: " + gb + "GB");

	    if (gb < 5) {

		GSLoggerFactory.getLogger(AvailableDiskSpaceChecker.class).error("Warning: the disk free space is below 5GB");

		if (gb < 1) {
		    ExecutionMode executionMode = ExecutionMode.get();
		    String hostname = HostNamePropertyUtils.getHostNameProperty();
		    String alarmMessage = "Error: the disk free space is below 1GB " + hostname + " (" + executionMode + ")";
		    GSLoggerFactory.getLogger(AvailableDiskSpaceChecker.class).error(alarmMessage);

		    ConfiguredSMTPClient.sendEmail(ConfiguredSMTPClient.MAIL_ALARM, "Disk space alert \n\n" + alarmMessage);
		}
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(AvailableDiskSpaceChecker.class).error(e);
	}

    }

    private static long getGB(long bytes) {
	bytes /= 1024;
	bytes /= 1024;
	bytes /= 1024;
	return bytes;
    }

}
