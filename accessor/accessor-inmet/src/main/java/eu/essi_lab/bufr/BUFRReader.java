package eu.essi_lab.bufr;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import eu.essi_lab.bufr.datamodel.BUFRElement;
import eu.essi_lab.bufr.datamodel.BUFRRecord;
import eu.essi_lab.model.resource.InterpolationType;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArraySequence;
import ucar.ma2.ArrayStructure;
import ucar.ma2.DataType;
import ucar.ma2.StructureData;
import ucar.ma2.StructureDataIterator;
import ucar.ma2.StructureMembers;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.SequenceDS;
import ucar.nc2.dataset.StructureDS;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.iosp.bufr.BufrIosp2;
import ucar.nc2.iosp.bufr.Message;
import ucar.nc2.iosp.bufr.MessageScanner;
import ucar.nc2.iosp.bufr.writer.Bufr2Xml;
import ucar.unidata.io.RandomAccessFile;
import ucar.unidata.util.StringUtil2;

/**
 * modified from John Caron class {@link Bufr2Xml}
 * 
 * @author boldrini
 */
public class BUFRReader {

    public class BUFRMessageReader {

	public BUFRMessageReader() {

	}

	public List<BUFRRecord> read(Message message, NetcdfDataset ncfile) {

	    try {
		List<BUFRRecord> ret = new ArrayList<>();

		BUFRRecord headerRecord = new BUFRRecord();

		headerRecord.getElements().add(new BUFRElement("BUFR:edition", null, null, Integer.toString(message.is.getBufrEdition())));
		String header = message.getHeader().trim();
		if (header.length() > 0) {
		    headerRecord.getElements().add(new BUFRElement("BUFR:header", null, null, header));
		}
		headerRecord.getElements().add(new BUFRElement("BUFR:tableVersion", null, null, message.getLookup().getTableName()));
		headerRecord.getElements().add(new BUFRElement("BUFR:center", null, null, message.getLookup().getCenterName()));
		headerRecord.getElements().add(new BUFRElement("BUFR:category", null, null, message.getLookup().getCategoryName()));

		ret.add(headerRecord);

		SequenceDS obs = (SequenceDS) ncfile.findVariable(BufrIosp2.obsRecordName);
		StructureDataIterator sdataIter = obs.getStructureIterator(-1);
		List<BUFRRecord> records = getRecords(obs, sdataIter, false);
		ret.addAll(records);

		return ret;

	    } catch (IOException | XMLStreamException e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	    }
	}

	// iterate through the observations

	private List<BUFRRecord> getRecords(StructureDS s, StructureDataIterator sdataIter, boolean recursive)
		throws IOException, XMLStreamException {

	    List<BUFRRecord> ret = new ArrayList<>();
	    try {
		while (sdataIter.hasNext()) {

		    BUFRRecord record = new BUFRRecord();
		    ret.add(record);
		    // out.format("%sSequence %s count=%d%n", indent, s.getShortName(), count++);
		    StructureData sdata = sdataIter.next();

		    for (StructureMembers.Member m : sdata.getMembers()) {
			Variable v = s.findVariable(m.getName());

			if (m.getDataType().isString() || m.getDataType().isNumeric()) {
			    BUFRElement element = getElement((VariableDS) v, sdata.getArray(m));
			    if (element.getValue() != null) {
				// simple element
				refineElement(element);
				record.getElements().add(element);
			    }

			} else if (m.getDataType() == DataType.STRUCTURE) {
			    StructureDS sds = (StructureDS) v;
			    ArrayStructure data = (ArrayStructure) sdata.getArray(m);
			    List<BUFRElement> elements = getElements(sds, data.getStructureDataIterator(), true);
			    List<BUFRElement> relatedElements = refineRelatedelements(elements);
			    record.getElements().addAll(relatedElements);

			} else if (m.getDataType() == DataType.SEQUENCE) {
			    SequenceDS sds = (SequenceDS) v;
			    ArraySequence data = (ArraySequence) sdata.getArray(m);
			    List<BUFRElement> elements = getElements(sds, data.getStructureDataIterator(), true);
			    List<BUFRElement> relatedElements = refineRelatedelements(elements);
			    record.getElements().addAll(relatedElements);
			}

		    }

		    List<BUFRElement> elements = record.getElements();
		    List<BUFRElement> refinedElements = new ArrayList<BUFRElement>();
		    BUFRElement timePeriodOrDisplacement = null;
		    BUFRElement timeSignificance = null;
		    for (BUFRElement element : elements) {
			if (element.isHeaderVariable()) {
			    refinedElements.add(element);
			} else if (element.isTimePeriorOrDisplacement()) {
			    timePeriodOrDisplacement = element;
			} else if (element.isTimeSignificance()) {
			    timeSignificance = element;
			} else if (element.isVariable()) {
			    if (element.getTimeSupport() == null) {
				element.setTimeSupport(timePeriodOrDisplacement);
			    }
			    if (element.getInterpolationType() == null && timeSignificance != null) {
				element.getAuxiliaryElements().add(timeSignificance);
			    }
			    refineElement(element);
			    refinedElements.add(element);
			}

		    }
		    record.getElements().clear();
		    record.getElements().addAll(refinedElements);
		}
	    } finally {
		sdataIter.finish();
	    }
	    return ret;
	}

	private List<BUFRElement> getElements(StructureDS s, StructureDataIterator sdataIter, boolean recursive)
		throws IOException, XMLStreamException {

	    List<BUFRElement> ret = new ArrayList<>();
	    try {
		while (sdataIter.hasNext()) {
		    // out.format("%sSequence %s count=%d%n", indent, s.getShortName(), count++);
		    StructureData sdata = sdataIter.next();

		    for (StructureMembers.Member m : sdata.getMembers()) {
			Variable v = s.findVariable(m.getName());

			if (m.getDataType().isString() || m.getDataType().isNumeric()) {
			    BUFRElement element = getElement((VariableDS) v, sdata.getArray(m));
			    if (element.getValue() != null) {
				refineElement(element);
				ret.add(element);
			    }

			} else if (m.getDataType() == DataType.STRUCTURE) {
			    StructureDS sds = (StructureDS) v;
			    ArrayStructure data = (ArrayStructure) sdata.getArray(m);
			    List<BUFRElement> elements = getElements(sds, data.getStructureDataIterator(), true);
			    List<BUFRElement> relatedElements = refineRelatedelements(elements);
			    ret.addAll(relatedElements);

			} else if (m.getDataType() == DataType.SEQUENCE) {
			    SequenceDS sds = (SequenceDS) v;
			    ArraySequence data = (ArraySequence) sdata.getArray(m);
			    List<BUFRElement> elements = getElements(sds, data.getStructureDataIterator(), true);
			    List<BUFRElement> relatedElements = refineRelatedelements(elements);
			    ret.addAll(relatedElements);
			}

		    }

		}
	    } finally {
		sdataIter.finish();
	    }
	    return ret;
	}

	private List<BUFRElement> refineRelatedelements(List<BUFRElement> elements) {

	    List<BUFRElement> ret = new ArrayList<>();
	    List<BUFRElement> variables = new ArrayList<>();
	    for (BUFRElement element : elements) {
		if (element.isVariable()) {
		    variables.add(element);
		}
	    }
	    for (BUFRElement variable : variables) {
		for (BUFRElement element : elements) {
		    if (element.equals(variable)) {
			continue;
		    }
		    if (element.isTimePeriorOrDisplacement()) {
			variable.setTimeSupport(element);
		    }
		    if (element.isAuxiliaryVariable()) {
			variable.getAuxiliaryElements().add(element);
		    }
		}
		refineElement(variable);
		ret.add(variable);
	    }
	    return ret;
	}

	private void refineElement(BUFRElement variable) {
	    String name = variable.getName();
	    if (name.contains("-hour_")) { // e.g. 3-hours_pressure_change
		try {
		    Integer hours = Integer.parseInt(name.substring(0, name.indexOf("-hour_")));
		    BUFRElement timeSupport = new BUFRElement("Time_period_or_displacement", "0-4-24", "h", "-" + hours + ".00");
		    if (variable.getTimeSupport() == null) {
			variable.setTimeSupport(timeSupport);
		    }
		    if (variable.getInterpolationType() == null) {
			variable.setInterpolationType(InterpolationType.INCREMENTAL);
		    }
		} catch (Exception e) {
		}
	    }
	    if (name.endsWith("_past_24_hours")) {
		if (variable.getTimeSupport() == null) {
		    BUFRElement timeSupport = new BUFRElement("Time_period_or_displacement", "0-4-24", "h", "-24.00");
		    variable.setTimeSupport(timeSupport);
		}
	    }
	    if (name.startsWith("Total_")) {
		variable.setInterpolationType(InterpolationType.TOTAL);
	    }
	    if (name.startsWith("Minimum_")) {
		variable.setInterpolationType(InterpolationType.MIN);
	    }
	    if (name.startsWith("Maximum_")) {
		variable.setInterpolationType(InterpolationType.MAX);
	    }
	    for (BUFRElement auxiliary : variable.getAuxiliaryElements()) {
		if (auxiliary.isTimeSignificance()) {
		    String value = auxiliary.getValue().toLowerCase();
		    // 1813.00,"008021","Time significance","0","Reserved",,,,"Operational"
		    // 1814.00,"008021","Time significance","1","Time series",,,,"Operational"
		    // 1815.00,"008021","Time significance","2","Time averaged (see Note 1)",,,,"Operational"
		    // 1816.00,"008021","Time significance","3","Accumulated",,,,"Operational"
		    // 1817.00,"008021","Time significance","4","Forecast",,,,"Operational"
		    // 1818.00,"008021","Time significance","5","Forecast time series",,,,"Operational"
		    // 1819.00,"008021","Time significance","6","Forecast time averaged",,,,"Operational"
		    // 1820.00,"008021","Time significance","7","Forecast accumulated",,,,"Operational"
		    // 1821.00,"008021","Time significance","8","Ensemble mean (see Note 2)",,,,"Operational"
		    // 1822.00,"008021","Time significance","9","Ensemble mean time series",,,,"Operational"
		    // 1823.00,"008021","Time significance","10","Ensemble mean time averaged",,,,"Operational"
		    // 1824.00,"008021","Time significance","11","Ensemble mean accumulated",,,,"Operational"
		    // 1825.00,"008021","Time significance","12","Ensemble mean forecast",,,,"Operational"
		    // 1826.00,"008021","Time significance","13","Ensemble mean forecast time series",,,,"Operational"
		    // 1827.00,"008021","Time significance","14","Ensemble mean forecast time averaged",,,,"Operational"
		    // 1828.00,"008021","Time significance","15","Ensemble mean forecast accumulated",,,,"Operational"
		    // 1829.00,"008021","Time significance","16","Analysis",,,,"Operational"
		    // 1830.00,"008021","Time significance","17","Start of phenomenon",,,,"Operational"
		    // 1831.00,"008021","Time significance","18","Radiosonde launch time",,,,"Operational"
		    // 1832.00,"008021","Time significance","19","Start of orbit",,,,"Operational"
		    // 1833.00,"008021","Time significance","20","End of orbit",,,,"Operational"
		    // 1834.00,"008021","Time significance","21","Time of ascending node",,,,"Operational"
		    // 1835.00,"008021","Time significance","22","Time of occurrence of wind shift",,,,"Operational"
		    // 1836.00,"008021","Time significance","23","Monitoring period",,,,"Operational"
		    // 1837.00,"008021","Time significance","24","Agreed time limit for report
		    // reception",,,,"Operational"
		    // 1838.00,"008021","Time significance","25","Nominal reporting time",,,,"Operational"
		    // 1839.00,"008021","Time significance","26","Time of last known position",,,,"Operational"
		    // 1840.00,"008021","Time significance","27","First guess",,,,"Operational"
		    // 1841.00,"008021","Time significance","28","Start of scan",,,,"Operational"
		    // 1842.00,"008021","Time significance","29","End of scan or time of ending",,,,"Operational"
		    // 1843.00,"008021","Time significance","30","Time of occurrence",,,,"Operational"
		    // 1844.00,"008021","Time significance","31","Missing value",,,,"Operational"

		    if (value.contains("average")) {
			variable.setInterpolationType(InterpolationType.AVERAGE);
		    } else if (value.contains("accumulated")) {
			variable.setInterpolationType(InterpolationType.TOTAL);
		    } else if (value.contains("minimum")) { // it should not happen.. however...
			variable.setInterpolationType(InterpolationType.MIN);
		    } else if (value.contains("maximum")) { // it should not happen.. however...
			variable.setInterpolationType(InterpolationType.MAX);
		    }
		}
	    }
	    if (variable.getInterpolationType() == null) {
		variable.setInterpolationType(InterpolationType.CONTINUOUS);
	    }
	}

	private BUFRElement getElement(VariableDS v, Array mdata) throws XMLStreamException, IOException {
	    BUFRElement ret = new BUFRElement();

	    String name = v.getShortName();
	    ret.setName(StringUtil2.quoteHtmlContent(name));

	    String units = v.getUnitsString();
	    if ((units != null) && !units.equals(name) && !units.startsWith("Code")) {
		ret.setUnits(StringUtil2.quoteHtmlContent(v.getUnitsString()));
	    }

	    Attribute att = v.findAttribute(BufrIosp2.fxyAttName);
	    String desc = (att == null) ? "N/A" : att.getStringValue();
	    ret.setCode(StringUtil2.quoteHtmlContent(desc));

	    if (v.getDataType() == DataType.CHAR) {
		ArrayChar ac = (ArrayChar) mdata;
		ret.setValue(ac.getString()); // turn into a string

	    } else {

		int count = 0;
		String value = "";
		mdata.resetLocalIterator();
		while (mdata.hasNext()) {
		    if (count > 0)
			value += " ";
		    count++;

		    if (v.getDataType().isNumeric()) {
			double val = mdata.nextDouble();

			if (v.isMissing(val)) {

			    value += "";

			} else if ((v.getDataType() == DataType.FLOAT) || (v.getDataType() == DataType.DOUBLE)) {
			    value += getFloatString(v, val);

			} else { // numeric, not float
			    value += mdata.toString();
			}

		    } else { // not numeric
			String s = StringUtil2.filter7bits(mdata.next().toString());
			if (s.toLowerCase().equals("missing value") || s.toLowerCase().equals("missing")) {
			    value += "";
			} else {
			    value += StringUtil2.quoteHtmlContent(s);
			}
		    }
		}
		value = value.trim();
		if (value.equals("")) {
		    value = null;
		}
		ret.setValue(value);
	    }

	    return ret;
	}

	private String getFloatString(Variable v, double val) throws XMLStreamException {
	    Attribute bitWidthAtt = v.findAttribute("BUFR:bitWidth");
	    int sigDigits;
	    if (bitWidthAtt == null) {
		sigDigits = 7;
	    } else {
		int bitWidth = bitWidthAtt.getNumericValue().intValue();
		if (bitWidth < 30) {
		    double sigDigitsD = Math.log10(2 << bitWidth);
		    sigDigits = (int) (sigDigitsD + 1);
		} else {
		    sigDigits = 7;
		}
	    }

	    Formatter stringFormatter = new Formatter();
	    String format = "%." + sigDigits + "g";
	    stringFormatter.format(format, val);
	    String ret = stringFormatter.toString();
	    stringFormatter.close();
	    return ret;
	}

    }

    public List<BUFRRecord> extractRecords(String filename) throws IOException {

	List<BUFRRecord> ret = new ArrayList<>();
	try (RandomAccessFile raf = new RandomAccessFile(filename, "r")) {
	    MessageScanner scan = new MessageScanner(raf);
	    while (scan.hasNext()) {
		Message message = scan.next();
		if (message == null || !message.isTablesComplete() || !message.isBitCountOk())
		    continue;
		byte[] mbytes = scan.getMessageBytesFromLast(message);
		NetcdfFile ncfile = NetcdfFile.openInMemory("test", mbytes, "ucar.nc2.iosp.bufr.BufrIosp2");
		NetcdfDataset ncd = new NetcdfDataset(ncfile);
		BUFRMessageReader reader = new BUFRMessageReader();
		ret.addAll(reader.read(message, ncd));
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	return ret;
    }

}
