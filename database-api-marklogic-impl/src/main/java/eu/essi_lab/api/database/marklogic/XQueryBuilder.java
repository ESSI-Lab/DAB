package eu.essi_lab.api.database.marklogic;

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

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;

/**
 * @author Fabrizio
 */
public class XQueryBuilder {

    private StringBuilder builder;

    /**
     * 
     */
    public XQueryBuilder() {

	builder = new StringBuilder();
    }

    /**
     * @return
     */
    public XQueryBuilder appendXSAttribute() {

	return appendAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
    }

    /**
     * @return
     */
    public XQueryBuilder appendXSIAttribute() {

	return appendAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    }

    /**
     * @return
     */
    public XQueryBuilder appendGSAttribute() {

	return appendAttribute("xmlns:gs", CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI);
    }

    /**
     * @param name
     * @param value
     * @return
     */
    public XQueryBuilder appendAttribute(String name, String value) {

	return appendAttribute(name, value, true);
    }

    /**
     * @param name
     * @param value
     * @return
     */
    public XQueryBuilder appendAttribute(String name, String value, boolean quoteValue) {

	if (quoteValue) {

	    builder.append("attribute{'" + name + "'}{'" + value + "'}");

	} else {

	    builder.append("attribute{'" + name + "'}{" + value + "}");
	}

	return this;
    }

    /**
     * @param nameSpace
     * @param name
     * @return
     */
    public XQueryBuilder appendElement(String nameSpace, String name) {

	builder.append("element{ fn:QName('" + nameSpace + "','" + name + "') }");
	return this;
    }

    /**
     * @param nameSpace
     * @param name
     * @return
     */
    public XQueryBuilder appendGSElement(String name) {

	builder.append("element{ fn:QName('" + CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI + "','" + name + "') }");
	return this;

    }

    /**
     * @param target
     * @param start
     * @param length
     * @return
     */
    public XQueryBuilder appendSubsequence(String target, int start, int length) {

	builder.append("subsequence( " + target + ", " + start + ", " + length + " )");
	return this;

    }

    /**
     * @param count
     */
    public XQueryBuilder appendCarriageReturn(int count) {

	for (int i = 0; i < count; i++) {
	    builder.append("\n");
	}
	return this;

    }

    /**
     * 
     */
    public XQueryBuilder appendCarriageReturn() {

	return appendCarriageReturn(1);
    }

    /**
     * @param text
     */
    public XQueryBuilder append(String text) {

	builder.append(text);
	return this;

    }

    /**
     * @param text
     */
    public XQueryBuilder appendComma() {

	return append(",");
    }

    /**
     * @param text
     */
    public XQueryBuilder appendOpenBrace() {

	return append("{");
    }

    /**
     * @param text
     */
    public XQueryBuilder appendClosedBrace() {

	return append("}");
    }

    /**
     * 
     */
    public XQueryBuilder appendReturnStatement() {

	return append("return ");
    }

    /**
     * @param variable
     * @return
     */
    public XQueryBuilder appendForInStatement(String variable) {

	return append("for $" + variable + " in ");
    }

    /**
     * 
     */
    public XQueryBuilder cutLast() {

	builder.deleteCharAt(builder.length() - 1);
	return this;
    }

    /**
     * @return
     */
    public StringBuilder getStringBuilder() {

	return builder;
    }

    /**
     * @return
     */
    public String build() {

	return builder.toString();
    }

    /**
     * @param sourceSeq
     * @param startingLoc
     * @param length
     * @return
     */
    public static String subsequence(String sourceSeq, int startingLoc, int length) {

	return "subsequence( " + sourceSeq + ", " + startingLoc + ", " + length + " )";
    }
}
