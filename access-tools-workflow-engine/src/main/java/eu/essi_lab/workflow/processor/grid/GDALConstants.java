package eu.essi_lab.workflow.processor.grid;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.InputStream;
import java.io.InputStreamReader;

import org.gdal.gdal.gdal;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class GDALConstants {
    public static Implementation IMPLEMENTATION = Implementation.RUNTIME;

    public enum Implementation {
	JNI, RUNTIME
    }

    private static boolean JNI_INITED = false;

    public static void initJNI() {
	if (!JNI_INITED) {
	    gdal.AllRegister();
	    JNI_INITED = true;
	}
    }

    public static Boolean isGDALAvailable() {
	try {
	    switch (IMPLEMENTATION) {
	    case JNI:
		initJNI();
		String versionInfo = gdal.VersionInfo();
		if (versionInfo != null && !versionInfo.equals("")) {
		    if (checkVersion(versionInfo)) {
			return true;
		    } else {
			GSLoggerFactory.getLogger(GDALConstants.class).error("Wrong GDAL version (required at least 2.4): " + versionInfo);
		    }
		}
		break;
	    case RUNTIME:
	    default:
		Runtime rt = Runtime.getRuntime();
		String command = "gdalwarp --version";
		Process ps = rt.exec(command);
		int exitVal = ps.waitFor();
		if (exitVal != 0) {
		    return false;
		}
		command = "gdal_translate --version";
		ps = rt.exec(command);
		exitVal = ps.waitFor();
		if (exitVal != 0) {
		    return false;
		}
		InputStream stream = ps.getInputStream();
		InputStreamReader reader = new InputStreamReader(stream);
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		while ((line = br.readLine()) != null) {
		    if (checkVersion(line)) {
			return true;
		    }
		}
		return false;

	    }
	} catch (Throwable e) {
	}
	return false;
    }

    private static boolean checkVersion(String versionInfo) {
	if (versionInfo.contains("GDAL 2.4") || //
		versionInfo.contains("GDAL 2.5") || //
		versionInfo.contains("GDAL 2.6") || //
		versionInfo.contains("GDAL 2.7") || //
		versionInfo.contains("GDAL 2.8") || //
		versionInfo.contains("GDAL 2.9") || //
		versionInfo.contains("GDAL 3") || //
		versionInfo.contains("GDAL 4") || //
		versionInfo.contains("GDAL 5") || //
		versionInfo.contains("GDAL 6") //
	) { //
	    return true;
	}
	return false;
    }

    public static void main(String[] args) {
	GDALConstants.IMPLEMENTATION = Implementation.JNI;
	System.out.println(GDALConstants.isGDALAvailable());
	GDALConstants.IMPLEMENTATION = Implementation.RUNTIME;
	System.out.println(GDALConstants.isGDALAvailable());
    }
}
