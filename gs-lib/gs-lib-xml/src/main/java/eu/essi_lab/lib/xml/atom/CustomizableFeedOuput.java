/**
 * 
 */
package eu.essi_lab.lib.xml.atom;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jdom2.Document;
import org.jdom2.Element;

import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedGenerator;
import com.rometools.rome.io.WireFeedOutput;
import com.rometools.rome.io.impl.Atom10Generator;

/**
 * This class allows to use a custom implementation of {@link WireFeedGenerator} in case more control on the output feed
 * generation is required
 * 
 * @author Fabrizio
 */
public class CustomizableFeedOuput extends WireFeedOutput {

    private WireFeedGenerator generator;

    /**
     * @param generator
     */
    public CustomizableFeedOuput(WireFeedGenerator generator) {

	this.generator = generator;
    }

    /**
     * 
     */
    public Document outputJDom(final WireFeed feed) throws IllegalArgumentException, FeedException {

	return generator.generate(feed);
    }

    /**
     * @param args
     * @throws IllegalArgumentException
     * @throws FeedException
     */
    public static void main(String[] args) throws IllegalArgumentException, FeedException {

	Feed feed = new Feed();
	feed.setFeedType("atom_1.0");

	feed.setTitle("Sample Feed (created with ROME)");
	feed.setUpdated(new Date());

	List<Entry> entries = new ArrayList<>();

	Entry entry = new Entry();

	entry.setId(UUID.randomUUID().toString());

	entry.setTitle("title");

	entries.add(entry);

	feed.setEntries(entries);

	WireFeedGenerator extended = new Atom10Generator() {

	    @Override
	    protected void populateEntry(final Entry entry, final Element eEntry) throws FeedException {

		super.populateEntry(entry, eEntry);

		//
		// additional code
		//

		eEntry.addContent(generateSimpleElement("someName", "someValue"));
	    }
	};

	CustomizableFeedOuput output = new CustomizableFeedOuput(extended);

	System.out.println(output.outputString(feed));
    }
}
