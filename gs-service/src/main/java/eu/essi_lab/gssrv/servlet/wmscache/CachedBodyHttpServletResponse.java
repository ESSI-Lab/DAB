package eu.essi_lab.gssrv.servlet.wmscache;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private ServletOutputStream cachedServletOutputStream;
    private PrintWriter cachedWriter;
    private WriteListener writeListener;

    public CachedBodyHttpServletResponse(HttpServletResponse response) {
	super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
	if (cachedWriter != null) {
	    throw new IllegalStateException("getWriter() has already been called on this response.");
	}
	if (cachedServletOutputStream == null) {
	    cachedServletOutputStream = new ServletOutputStream() {
		@Override
		public boolean isReady() {
		    // for a simple buffer-backed implementation, always ready
		    return true;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
		    CachedBodyHttpServletResponse.this.writeListener = writeListener;
		    // Immediately notify the listener that writing is possible.
		    // For more advanced non-blocking behaviour you should integrate with your IO loop.
		    if (writeListener != null) {
			try {
			    writeListener.onWritePossible();
			} catch (IOException e) {
			    writeListener.onError(e);
			}
		    }
		}

		@Override
		public void write(int b) {
		    buffer.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) {
		    buffer.write(b, off, len);
		}
	    };
	}
	return cachedServletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
	if (cachedServletOutputStream != null) {
	    throw new IllegalStateException("getOutputStream() has already been called on this response.");
	}
	if (cachedWriter == null) {
	    String enc = getCharacterEncoding();
	    if (enc == null) {
		enc = StandardCharsets.UTF_8.name();
	    }
	    cachedWriter = new PrintWriter(new OutputStreamWriter(buffer, Charset.forName(enc)), true);
	}
	return cachedWriter;
    }

    @Override
    public void flushBuffer() throws IOException {
	if (cachedWriter != null) {
	    cachedWriter.flush();
	}
	if (cachedServletOutputStream != null) {
	    // no-op because our stream writes into buffer
	}
	super.flushBuffer();
    }

    public byte[] getBody() throws IOException {
	// ensure writer content is flushed into buffer
	if (cachedWriter != null) {
	    cachedWriter.flush();
	}
	return buffer.toByteArray();
    }

    public String getBodyAsString() throws IOException {
	byte[] bytes = getBody();
	String enc = getCharacterEncoding();
	if (enc == null)
	    enc = StandardCharsets.UTF_8.name();
	return new String(bytes, enc);
    }
}
