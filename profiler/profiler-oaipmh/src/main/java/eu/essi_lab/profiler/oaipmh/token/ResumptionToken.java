package eu.essi_lab.profiler.oaipmh.token;

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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.Optional;

import javax.xml.datatype.XMLGregorianCalendar;

import eu.essi_lab.jaxb.oaipmh.ResumptionTokenType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.profiler.oaipmh.OAIPMHRequestReader;

/**
 * @author Fabrizio
 */
public class ResumptionToken {

    public static String NONE_SEARCH_AFTER = "null";

    private String tokenId;
    private int advancement;
    private String from;
    private String until;
    private String set;
    private String prefix;
    private String searchAfter;

    /**
     * @param value
     * @return
     */
    public static ResumptionToken of(String value) {

	return new ResumptionToken(value);
    }

    /**
     * @param reader
     * @param tokenId
     * @param listSize
     * @param advancement
     * @param itemsPerPage
     * @param prefix
     * @param searchAfter
     * @return
     */
    public static ResumptionTokenType of(//
	    OAIPMHRequestReader reader, //
	    String tokenId, //
	    int listSize, //
	    int advancement, //
	    int itemsPerPage, //
	    String prefix, //
	    String searchAfter) {

	// this is the first token created for this request
	XMLGregorianCalendar expiration = null;
	if (tokenId == null) {
	    tokenId = createId();
	    expiration = TokenTimer.getInstance().addToken(tokenId);
	    // adds this token id to the timer
	} else {
	    // restart the timer for this token id
	    expiration = TokenTimer.getInstance().restartTiming(tokenId);
	}

	ResumptionTokenType rtt = _createEmpty(tokenId, listSize, advancement);
	rtt.setExpirationDate(expiration);

	String from = null;
	String until = null;
	String set = null;

	String value = reader.getResumptionToken();
	if (value != null) {
	    ResumptionToken rt = new ResumptionToken(value);

	    from = rt.getFrom();
	    until = rt.getUntil();
	    set = rt.getSet();
	} else {
	    from = reader.getFrom();
	    until = reader.getUntil();
	    set = reader.getSet();
	}

	// REQUEST_ID/ADVANCEMENT/FROM/UNTIL/SETID/PREFIX/SEARCH_AFTER
	rtt.setValue(//
		tokenId + "/" + //
			(advancement + itemsPerPage) + "/" + //
			from + "/" + //
			until + "/" + //
			set + "/" + //
			prefix + "/" + //
			searchAfter);

	return rtt;
    }

    /**
     * @return
     */
    public static String createId() {

	GSLoggerFactory.getLogger(ResumptionToken.class).info("Using same token, see GIP-304");

	return "restoken";
	// return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * @param tokenId
     * @return
     */
    public static boolean isExpired(String tokenId) {

	return TokenTimer.getInstance().isExpired(tokenId);
    }

    /**
     * @param tokenId
     * @param listSize
     * @param advancement
     * @return
     */
    public static ResumptionTokenType createEmpty(String tokenId, int listSize, int advancement) {

	// if (!tokenId.equals("")) {
	// // removes the token id from the timer
	// TokenTimer.getInstance().removeToken(tokenId);
	// }

	return _createEmpty(tokenId, listSize, advancement);
    }

    /**
     * @param value
     */
    private ResumptionToken(String value) {

	try {
	    value = URLDecoder.decode(value, "UTF8");
	} catch (UnsupportedEncodingException e) {
	}

	// REQUEST_ID/ADVANCEMENT/FROM/UNTIL/SETID/PREFIX/SEARCH_AFTER
	String[] slashSplit = value.split("/");
	if (slashSplit.length != 7) {
	    throw new IllegalArgumentException("Invalid resumption token");
	}

	// REQUEST_ID
	tokenId = slashSplit[0];
	if (tokenId.equals("")) {
	    throw new IllegalArgumentException("Invalid resumption token");
	}

	// ADVANCEMENT
	advancement = Integer.parseInt(slashSplit[1]);

	// FROM
	from = slashSplit[2];
	if (from != null && from.equals("null")) {
	    from = null;
	}

	// UNTIL
	until = slashSplit[3];
	if (until != null && until.equals("null")) {
	    until = null;
	}

	// SETID
	set = slashSplit[4];
	if (set != null && set.equals("null")) {
	    set = null;
	}

	// PREFIX
	prefix = slashSplit[5];
	if (prefix != null && prefix.equals("null")) {
	    prefix = null;
	}

	// SEARCH_AFTER
	searchAfter = slashSplit[6];
	if (searchAfter != null && searchAfter.equals("null")) {
	    searchAfter = null;
	}
    }

    /**
     * @param tokenId
     * @param listSize
     * @param advancement
     * @return
     */
    private static ResumptionTokenType _createEmpty(String tokenId, int listSize, int advancement) {

	ResumptionTokenType rtt = new ResumptionTokenType();

	rtt.setCompleteListSize(new BigInteger(String.valueOf(listSize)));
	rtt.setCursor(new BigInteger(String.valueOf(advancement)));

	return rtt;
    }

    /**
     * @return
     */
    public String getId() {

	return tokenId;
    }

    /**
     * @return
     */
    public String getMetadataPrefix() {

	return prefix;
    }

    /**
     * @return
     */
    public int getAdvancement() {

	return advancement;
    }

    /**
     * @return
     */
    public String getFrom() {

	return from;
    }

    /**
     * @return
     */
    public String getUntil() {

	return until;
    }

    /**
     * @return
     */
    public String getSet() {

	return set;
    }

    /**
     * @return
     */
    public Optional<String> getSearchAfter() {

	return searchAfter == null || searchAfter.equals(NONE_SEARCH_AFTER) ? Optional.empty() : Optional.of(searchAfter);
    }

    public static void main(String[] args) {

	// String value = "REQUEST_ID/1/FROM/UNTIL/SETID/PREFIX";
	// ResumptionToken resumptionToken = new ResumptionToken(value);

    }
}
