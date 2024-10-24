package eu.essi_lab.profiler.oaipmh.token;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.XMLGregorianCalendarUtils;

public class TokenTimer {

    private class TokenTask extends TimerTask {

	private String tokenId;

	public TokenTask(String tokenId) {

	    this.tokenId = tokenId;
	}

	@Override
	public void run() {

	    removeToken(tokenId);
	}
    }

    private static final TokenTimer INSTANCE = new TokenTimer();
    private HashMap<String, Timer> timerMap;

    private TokenTimer() {

	timerMap = new HashMap<>();
    }

    public static TokenTimer getInstance() {

	return INSTANCE;
    }

    public synchronized XMLGregorianCalendar addToken(String tokenId) {

	GSLoggerFactory.getLogger(getClass()).debug("Adding new token: {} ", tokenId);

	Timer timer = new Timer();
	// after 5 minutes removes the token
	timer.schedule(new TokenTask(tokenId), 1000 * 60 * 5); // 5 minutes

	timerMap.put(tokenId, timer);

	long expiration = System.currentTimeMillis() + (1000 * 60 * 5);

	try {

	    XMLGregorianCalendar calendar = XMLGregorianCalendarUtils.createGregorianCalendar(new Date(expiration));

	    GSLoggerFactory.getLogger(getClass()).debug("Token {} expiration: {}", tokenId, calendar);

	    return calendar;

	} catch (DatatypeConfigurationException e) {
	    GSLoggerFactory.getLogger(getClass()).debug(e.getMessage(), e);
	}

	return null;
    }

    public synchronized XMLGregorianCalendar restartTiming(String tokenId) {
	
	GSLoggerFactory.getLogger(getClass()).info("Temporary disabled token functionalities, see GIP-304");

//	GSLoggerFactory.getLogger(getClass()).debug("Restarting token: {}", tokenId);
//
//	Timer timer = timerMap.get(tokenId);
//	timer.cancel();

	return addToken(tokenId);
    }

    public synchronized void removeToken(String tokenId) {
	
	GSLoggerFactory.getLogger(getClass()).info("Temporary disabled token functionalities, see GIP-304");
	return;

//	GSLoggerFactory.getLogger(getClass()).debug("Removing expired token: {}", tokenId);
//	GSLoggerFactory.getLogger(getClass()).debug("Current timer map tokens");
//	timerMap.forEach((k, v) -> {
//	    GSLoggerFactory.getLogger(getClass()).debug("Token: {}", k);
//	});
//
//	Timer timer = timerMap.get(tokenId);
//	timer.cancel();
//
//	timerMap.remove(tokenId);
//
//	GSLoggerFactory.getLogger(getClass()).debug("Removed expired token :{}", tokenId);
    }

    public synchronized boolean isExpired(String tokenId) {
	
	GSLoggerFactory.getLogger(getClass()).info("Temporary disabled token functionalities, see GIP-304");
	return false;

//	GSLoggerFactory.getLogger(getClass()).debug("Testing token {} expiration", tokenId);
//
//	boolean expired = timerMap.get(tokenId) == null;
//
//	GSLoggerFactory.getLogger(getClass()).debug("Token {} is expired {}: ", tokenId, expired);
//
//	return expired;
    }
}
