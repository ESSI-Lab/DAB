package eu.essi_lab.cfga.rest;

import eu.essi_lab.cfga.setting.*;

import javax.ws.rs.core.*;
import java.util.*;

/**
 *
 * @param <S>
 * @author Fabrizio
 *
 */
public class SettingFinder<S extends Setting> {

    private S setting;
    private Response errorResponse;

    /**
     * @param setting
     */
    SettingFinder(S setting) {

	this.setting = setting;
    }

    /**
     * @param response
     */
    SettingFinder(Response response) {

	errorResponse = response;
    }

    /**
     * @return
     */
    public Optional<S> getSetting() {

	return Optional.ofNullable(setting);
    }

    /**
     * @return
     */
    public Optional<Response> getErrorResponse() {

	return Optional.ofNullable(errorResponse);
    }
}
