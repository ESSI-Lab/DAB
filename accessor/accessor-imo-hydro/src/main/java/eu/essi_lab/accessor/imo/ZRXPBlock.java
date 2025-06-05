package eu.essi_lab.accessor.imo;

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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class ZRXPBlock {

    private static final String ZRXPB_LOCK_ERROR = "ZRXPB_LOCK_ERROR";
    
    private File file;
    private Integer startHeadersLine = null;
    private Integer startDataLine = null;
    private Integer endDataLine = null;

    public void setEndDataLine(Integer endDataLine) {
	this.endDataLine = endDataLine;
    }

    public Integer getEndDataLine() {
	return endDataLine;
    }

    private int timestampIndex;
    private int valueIndex;

    private EnumMap<ZRXPKeyword, String> headers = new EnumMap<>(ZRXPKeyword.class);

    public ZRXPBlock(File file, int startLine) {
	this.file = file;
	this.startHeadersLine = startLine;
    }

    public void addHeader(ZRXPKeyword zk, String value) {
	if (zk.equals(ZRXPKeyword.LAYOUT)) {
	    decodeDataLayout(value);
	}
	headers.put(zk, value);
    }

    private void decodeDataLayout(String layout) {
	layout = layout.replace("(", "").replace(")", "");
	String[] split = layout.split(",");
	for (int i = 0; i < split.length; i++) {
	    String s = split[i];
	    String dataKeyword = s.trim();
	    ZRXPDataKeyword keyword = ZRXPDataKeyword.fromKey(dataKeyword);
	    if (keyword != null) {
		switch (keyword) {
		case TIMESTAMP:
		    timestampIndex = i;
		    break;
		case VALUE:
		    valueIndex = i;
		    break;
		default:
		    break;
		}
	    }
	}
    }

    List<SimpleEntry<Date, Double>> data = new ArrayList<>();

    public List<SimpleEntry<Date, Double>> getDataValues() {
	return data;
    }

    static SimpleDateFormat sdf = null;

    static {
	sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void addDataLine(String line) {
	String[] split = line.split(" ");
	String timeString = split[timestampIndex];
	String valueString = split[valueIndex];
	try {
	    Date time = sdf.parse(timeString);
	    Double value = Double.parseDouble(valueString);
	    data.add(new SimpleEntry<Date, Double>(time, value));
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Block from file: " + file.getAbsolutePath() + " line #" + startHeadersLine + "\n");
	Set<Entry<ZRXPKeyword, String>> headerSet = headers.entrySet();
	for (Entry<ZRXPKeyword, String> header : headerSet) {
	    builder.append(header.getKey().name() + " (" + header.getKey().getDescription() + "): " + header.getValue() + "\n");
	}
	return builder.toString();
    }

    public String getUnit() {
	return headers.get(ZRXPKeyword.CUNIT);
    }

    public String getStationIdentifier() {
	String path = headers.get(ZRXPKeyword.TSPATH);
	if (path.contains("vhm")) {
	    path = path.substring(path.indexOf("vhm") + 3);
	    path = path.substring(0, path.indexOf('/'));
	    return "V" + path;
	}

	return headers.get(ZRXPKeyword.SANR);
    }

    public String getMissingDataValue() {
	return headers.get(ZRXPKeyword.RINVAL);
    }

    public String getParameterName() {
	return headers.get(ZRXPKeyword.CNAME);
    }

    public String getStationName() {
	return headers.get(ZRXPKeyword.SNAME);
    }

    public String getTimeZone() {
	return headers.get(ZRXPKeyword.TZ);
    }

    public String getRiverName() {
	return headers.get(ZRXPKeyword.SWATER);
    }

    public String getLatitude() {
	return headers.get(ZRXPKeyword.LATITUDE);
    }

    public String getLongitude() {
	return headers.get(ZRXPKeyword.LONGITUDE);
    }

    public void setStartDataLine(int startDataLine) {
	this.startDataLine = startDataLine;

    }

    public Integer getStartHeadersLine() {
	return startHeadersLine;
    }

    public Integer getStartDataLine() {
	return startDataLine;
    }

    public String asString() {
	FileReader fileReader = null;
	try {
	    fileReader = new FileReader(file);
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	}
	BufferedReader reader = new BufferedReader(fileReader);

	String line = "";

	ZRXPBlock tmpBlock = null;

	int i = 0;

	String ret = "";
	try {
	    while ((line = reader.readLine()) != null) {
		i++;
		if (i >= (startHeadersLine - 1) && i <= endDataLine) {
		    ret += line + "\n";
		}
		if (i > endDataLine) {
		    break;
		}
	    }
	} catch (Exception e) {
	}
	return ret + "\n";
    }

    public static HashMap<String, List<ZRXPBlock>> aggregateBlocks(List<ZRXPDocument> zrxps) throws GSException {
	HashMap<String, List<ZRXPBlock>> map = new HashMap<String, List<ZRXPBlock>>();

	for (ZRXPDocument zrxp : zrxps) {

	    List<ZRXPBlock> blocks;
	    try {
		blocks = zrxp.getBlocks();
	    } catch (IOException e) {

		throw GSException.createException(//
			ZRXPBlock.class, //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			ZRXPB_LOCK_ERROR, //
			e);
	    }

	    for (ZRXPBlock block : blocks) {

		String parameterName = block.getParameterName();
		String riverName = block.getRiverName();
		String stationId = block.getStationIdentifier();
		String stationName = block.getStationName();
		String unit = block.getUnit();

		List<SimpleEntry<Date, Double>> dataValues = block.getDataValues();
		if (dataValues.isEmpty()) {
		    continue;
		}

		String key = parameterName + "@" + stationName;

		List<ZRXPBlock> mapBlocks = map.get(key);
		if (mapBlocks == null) {
		    mapBlocks = new ArrayList<ZRXPBlock>();
		    map.put(key, mapBlocks);
		}
		mapBlocks.add(block);

	    }

	}
	return map;
    }

    public void addHeaderInFile(ZRXPKeyword key, String value) throws Exception {
	File temp = File.createTempFile(getClass().getSimpleName(), ".zrxp");
	value = value.trim();
	FileInputStream fis = new FileInputStream(file);
	InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);
	BufferedReader buffer = new BufferedReader(reader);
	String line;
	int i = 0;
	FileOutputStream fos = new FileOutputStream(temp);
	OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
	BufferedWriter writer = new BufferedWriter(osw);
	while ((line = buffer.readLine()) != null) {
	    if (i == startHeadersLine) {
		String headerString = key.toString() + value + "|*|";
		line = line + headerString;
	    }
	    writer.write(line + "\n");
	    i++;
	}
	buffer.close();
	reader.close();
	fis.close();
	writer.close();
	osw.close();
	fos.close();

	FileInputStream fis1 = new FileInputStream(temp);
	FileOutputStream fos2 = new FileOutputStream(file);
	IOUtils.copy(fis1, fos2);
	fis1.close();
	fos2.close();
	temp.delete();

	addHeader(key, value);

    }

    // public void insert(String filename, long offset, byte[] content) throws Exception {
    // RandomAccessFile r = new RandomAccessFile(new File(filename), "rw");
    // RandomAccessFile rtemp = new RandomAccessFile(new File(filename + "~"), "rw");
    // long fileSize = r.length();
    // FileChannel sourceChannel = r.getChannel();
    // FileChannel targetChannel = rtemp.getChannel();
    // sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
    // sourceChannel.truncate(offset);
    // r.seek(offset);
    // r.write(content);
    // long newOffset = r.getFilePointer();
    // targetChannel.position(0L);
    // sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
    // sourceChannel.close();
    // targetChannel.close();
    // r.close();
    // rtemp.close();
    // }

}
