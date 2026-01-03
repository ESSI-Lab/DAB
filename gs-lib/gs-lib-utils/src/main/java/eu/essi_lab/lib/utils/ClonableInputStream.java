package eu.essi_lab.lib.utils;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.google.common.io.ByteStreams;

/**
 * This class can be useful when a given input stream is read and closed ( for example by a 3rd party library or
 * application ) and there is the need to read it again.<br>
 * To avoid the problem of reading a close stream, always use the {@link #clone()} input stream instead of the original
 * one
 * 
 * @author Fabrizio
 */
public class ClonableInputStream implements Cloneable {

    private ByteArrayOutputStream copy;

    /**
     * Creates a new instance with the given <code>inputStream</code>
     * 
     * @param inputStream the stream to clone
     * @throws IOException
     */
    public ClonableInputStream(InputStream inputStream) throws IOException {

	copy = new ByteArrayOutputStream();

	ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
	WritableByteChannel outputChannel = Channels.newChannel(copy);

	ByteStreams.copy(inputChannel, outputChannel);

	inputStream.close();
    }

    /**
     * Return a clone of the original input stream
     * 
     * @return
     */
    public InputStream clone() {

	return new ByteArrayInputStream(copy.toByteArray());
    }

    /**
     * Return the length in bytes of the cloned stream
     * 
     * @return
     */
    public int getLength() {

	return copy.toByteArray().length;
    }
}
