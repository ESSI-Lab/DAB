package eu.essi_lab.accessor.hiscentral.lombardia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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

/**
 * Token store using a file in the system temp directory. Suitable for single-node only.
 */
public class FileLombardiaTokenStore implements LombardiaTokenStore {

    private static final String TOKEN_FILE = "arpa-lombardia.token";

    @Override
    public synchronized String readToken() throws IOException {
	String tmpdir = System.getProperty("java.io.tmpdir");
	File file = new File(tmpdir, TOKEN_FILE);
	if (!file.exists()) {
	    return null;
	}
	try (FileInputStream fis = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(reader)) {
	    return br.readLine();
	}
    }

    @Override
    public synchronized void writeToken(String token) throws IOException {
	String tmpdir = System.getProperty("java.io.tmpdir");
	File file = new File(tmpdir, TOKEN_FILE);
	if (file.exists()) {
	    file.delete();
	}
	file.createNewFile();
	try (FileOutputStream fos = new FileOutputStream(file)) {
	    fos.write(token.getBytes(StandardCharsets.UTF_8));
	}
    }

    @Override
    public synchronized void deleteToken(String token) throws IOException {
	String tmpdir = System.getProperty("java.io.tmpdir");
	File file = new File(tmpdir, TOKEN_FILE);
	if (file.exists()) {
	    file.delete();
	}
    }
}
