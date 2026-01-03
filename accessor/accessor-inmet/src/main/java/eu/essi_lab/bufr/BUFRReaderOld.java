// package eu.essi_lab.bufr;

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
//
// import java.io.File;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
//
// import org.slf4j.Logger;
//
// import eu.essi_lab.bufr.datamodel.BUFRElement;
// import eu.essi_lab.bufr.datamodel.BUFRRecord;
// import eu.essi_lab.lib.utils.GSLoggerFactory;
// import ucar.ma2.Array;
// import ucar.ma2.DataType;
// import ucar.ma2.StructureData;
// import ucar.ma2.StructureDataIterator;
// import ucar.ma2.StructureMembers.Member;
// import ucar.nc2.Attribute;
// import ucar.nc2.Variable;
// import ucar.nc2.dataset.NetcdfDataset;
// import ucar.nc2.dataset.SequenceDS;
//
// public class BUFRReaderOld {
//
// Logger logger = GSLoggerFactory.getLogger(getClass());
//
// private File file;
//
// public BUFRReaderOld(File file) throws IOException {
//
// this.file = file;
//
// }
//
// public List<BUFRRecord> extractRecords() throws IOException {
//
// List<BUFRRecord> ret = new ArrayList<>();
//
// String path = file.getAbsolutePath();
//
// logger.info("Extracting BUFR elements from: " + path);
//
// NetcdfDataset dataset = NetcdfDataset.openDataset(path);
//
// List<Attribute> globalAttributes = dataset.getGlobalAttributes();
//
// List<BUFRElement> globalElements = new ArrayList<BUFRElement>();
// for (Attribute attribute : globalAttributes) {
// String name = attribute.getShortName();
// Object value = attribute.getValue(0);
// if (value != null) {
// BUFRElement globalElement = new BUFRElement();
// globalElement.setName(name);
// globalElement.setValue(value.toString());
// // if (name.toLowerCase().contains("latitude")) {
// // logger.info("Extracting BUFR element from global attributes: " + name + " " + value.toString());
// // }
// logger.info("From global attributes: " + name + " " + value.toString());
// globalElements.add(globalElement);
// }
// }
//
// List<Variable> variables = dataset.getVariables();
//
// Variable obs = variables.get(0);
//
// if (obs instanceof SequenceDS) {
// SequenceDS sds = (SequenceDS) obs;
// StructureDataIterator iterator = sds.getStructureIterator();
//
// List<Variable> memberVariables = sds.getVariables();
// HashMap<String, Object> missingValues = new HashMap<String, Object>();
// for (Variable memberVariable : memberVariables) {
// String name = memberVariable.getShortName();
// Attribute missingAttribute = memberVariable.findAttribute("missing_value");
// if (missingAttribute != null) {
// Object missingAttributeObject = missingAttribute.getValue(0);
// if (missingAttributeObject != null) {
// missingValues.put(name, missingAttributeObject);
// }
// }
// }
//
// while (iterator.hasNext()) {
//
// StructureData sdata = iterator.next();
//
// BUFRRecord record = decodeStructureData(sdata, missingValues);
// record.getElements().addAll(globalElements);
// ret.add(record);
//
// }
// }
//
// dataset.close();
//
// return ret;
//
// }
//
// private BUFRRecord decodeStructureData(StructureData sdata, HashMap<String, Object> missingValues) {
//
// BUFRRecord record = new BUFRRecord();
//
// List<Member> members = sdata.getMembers();
// for (int i = 0; i < members.size(); i++) {
// Member member = members.get(i);
//
// String info = "";
//
// info += "(" + member.getDataType() + ") ";
// info += member.getName() + ": ";
// String units = member.getUnitsString();
// if (units != null) {
// info += "[units: " + units + "] ";
// }
//
// DataType memberDataType = member.getDataType();
// Array array = sdata.getArray(member);
// if (array == null || array.getSize() == 0) {
// info += "empty";
// } else {
//
// long size = array.getSize();
// if (size == 0) {
// continue;
// }
// if (size > 1) {
// info += " (Array size: " + size + ") ";
// }
//
// String value = "";
//
// switch (memberDataType) {
// case SEQUENCE:
// info += "Child sequence";
// break;
// case STRUCTURE:
// // System.out.println("Start child structure");
// StructureData structure = sdata.getScalarStructure(member);
//
// BUFRRecord childRecord = decodeStructureData(structure, missingValues);
// List<BUFRElement> variableElements = childRecord.identifyVariables();
// for (BUFRElement variableElement : variableElements) {
// // attaching auxiliary elements
// for (BUFRElement childElement : childRecord.getElements()) {
// if (childElement.isTimePeriorOrDisplacement()) {
// variableElement.setTimeSupport(childElement);
// } else if (childElement.isAuxiliaryVariable()) {
// variableElement.getAuxiliaryElements().add(childElement);
// }
// }
// record.getElements().add(variableElement);
//
// }
// // System.out.println("End child structure");
// break;
// case CHAR:
// for (int j = 0; j < array.getSize(); j++) {
// value += array.getChar(j);
// }
// info += value;
// break;
// default:
// value = array.getObject(0).toString();
// Object missingValueObject = missingValues.get(member.getName());
// if (missingValueObject != null && missingValueObject.toString().equals(value.toString())) {
// info += "missing value";
// } else {
// info += value;
// if (!value.equals("NaN") && !value.toLowerCase().startsWith("missing")) {
//
// // GSLoggerFactory.getLogger(getClass()).debug(info);
// BUFRElement element = new BUFRElement();
// // if (member.getName().toLowerCase().contains("latitude")) {
// // logger.info("Extracting BUFR element: " + member.getName() + " " + value);
// // }
// element.setName(member.getName());
// element.setUnits(units);
// element.setValue(value);
// record.getElements().add(element);
//
// }
// }
// break;
// }
// }
// }
//
// return record;
//
// }
//
// }
