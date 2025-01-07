package eu.essi_lab.accessor.eurobis.ld;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class TurtleFixer {
    // Function to fix the date format, workaround!
    private static String fixDate(String dateStr) {
	String[] parts = dateStr.split("-");
	String year = parts[0];
	String month = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
	String day = parts[2].length() == 1 ? "0" + parts[2] : parts[2];
	return year + "-" + month + "-" + day;
    }

    public static void fixFile(File inFile, File outFile) {
	GSLoggerFactory.getLogger(TurtleFixer.class).info("Fixing {} to {}", inFile.getAbsolutePath(), outFile.getAbsolutePath());
	// Read the Turtle file into a model
	Model model = ModelFactory.createDefaultModel();
	RDFParser.source(inFile.getAbsolutePath()).lang(RDFLanguages.TTL).parse(model);

	// Iterate over all statements and fix dates
	StmtIterator iter = model.listStatements();
	List<Statement> toRemove = new ArrayList<Statement>();
	List<Statement> toAdd = new ArrayList<Statement>();
	while (iter.hasNext()) {
	    Statement stmt = iter.nextStatement();

	    // Check if the object is a date literal
	    if (stmt.getObject().isLiteral()
		    && stmt.getObject().asLiteral().getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#date")) {
		String dateStr = stmt.getLiteral().getString();
		String fixedDateStr = fixDate(dateStr);

		// Replace the literal if it was fixed
		if (!fixedDateStr.equals(dateStr)) {
		    Literal fixedDateLiteral = model.createTypedLiteral(fixedDateStr, XSDDatatype.XSDdate);
		    toRemove.add(stmt);
		    Statement fixedStatement = new StatementImpl(stmt.getSubject(), stmt.getPredicate(), fixedDateLiteral);
		    toAdd.add(fixedStatement);
		}
	    }
	}
	for (Statement statement : toRemove) {
	    model.remove(statement);
	}
	for (Statement statement : toAdd) {
	    model.add(statement);
	}

	// Write the corrected model to a file
	try (java.io.OutputStream out = new FileOutputStream(outFile)) {
	    RDFDataMgr.write(out, model, RDFFormat.TURTLE);
	    GSLoggerFactory.getLogger(TurtleFixer.class).info("Fixed {} to {}", inFile.getAbsolutePath(), outFile.getAbsolutePath());
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(TurtleFixer.class).error(e);
	}

    }

}
