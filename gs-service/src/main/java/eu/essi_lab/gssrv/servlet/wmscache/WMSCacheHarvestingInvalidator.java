package eu.essi_lab.gssrv.servlet.wmscache;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.ConfiguredSMTPClient;
import eu.essi_lab.cfga.gs.setting.WMSCacheInvalidationSetting;
import eu.essi_lab.cfga.gs.setting.WMSCacheInvalidationSetting.TriggerCondition;
import eu.essi_lab.cfga.gs.setting.WMSCacheSetting;
import eu.essi_lab.cfga.gs.setting.WMSCacheSetting.WMSCacheMode;
import eu.essi_lab.gssrv.servlet.WMSCacheFilter;
import eu.essi_lab.harvester.HarvestingEndListener;
import eu.essi_lab.harvester.HarvestingNotifier;
import eu.essi_lab.harvester.HarvestingNotifier.HarvestingResult;
import eu.essi_lab.harvester.HarvestingReportsHandler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.HostNamePropertyUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.bond.View;

/**
 * Removes all the cached WMS layers of the views configured in the {@link WMSCacheInvalidationSetting}, when a
 * harvesting of a source belonging to one of these views changes the source records, according to the configured
 * {@link TriggerCondition}.<br> The sources of a view are the ones explicitly referenced by the view (see
 * {@link ConfigurationWrapper#getViewSourceIdentifiers(View)}); views without source constraints are ignored, since
 * they would match every harvested source.<br>
 * If the harvesting report e-mails are enabled (see {@link HarvestingReportsHandler#isEnabled()}), an e-mail reports
 * the result of each invalidation
 *
 * @author boldrini
 */
public class WMSCacheHarvestingInvalidator implements HarvestingEndListener {

    private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor(r -> {

	Thread thread = new Thread(r);
	thread.setPriority(Thread.MIN_PRIORITY);
	thread.setName(thread.getName() + "_WMS_CACHE_INVALIDATOR");

	return thread;
    });

    @Override
    public void harvestingEnded(HarvestingNotifier summary) {

	Optional<WMSCacheSetting> cacheSetting = ConfigurationWrapper.getWMSCacheSettings();

	if (cacheSetting == null || cacheSetting.isEmpty() || cacheSetting.get().getMode() == WMSCacheMode.DISABLED) {

	    return;
	}

	Optional<WMSCacheInvalidationSetting> setting = cacheSetting.get().getInvalidationSetting();

	if (setting.isEmpty()) {

	    return;
	}

	String sourceId = summary.getSource().getUniqueIdentifier();

	if (!isTriggered(summary, setting.get().getTriggerCondition())) {

	    GSLoggerFactory.getLogger(getClass()).debug("WMS cache invalidation not triggered by harvesting of source {} [{}, {} -> {}]",
		    sourceId, summary.getResult(), summary.getRecordsBefore(), summary.getRecordsAfter());

	    return;
	}

	List<String> views = findViewsToClean(sourceId, setting.get().getViews());

	if (views.isEmpty()) {

	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Harvesting of source {} [{} -> {} records] triggers WMS cache invalidation of views {}",
		sourceId, summary.getRecordsBefore(), summary.getRecordsAfter(), views);

	TriggerCondition condition = setting.get().getTriggerCondition();

	THREAD_POOL.execute(() -> invalidate(views, summary, condition));
    }

    /**
     * @param summary
     * @param condition
     * @return
     */
    static boolean isTriggered(HarvestingNotifier summary, TriggerCondition condition) {

	if (summary.getResult() != HarvestingResult.COMPLETED) {

	    return false;
	}

	return switch (condition) {
	case EVERY_COMPLETED_HARVESTING -> true;
	case RECORDS_COUNT_CHANGED -> summary.getRecordsAfter() >= 0 && summary.getRecordsAfter() != summary.getRecordsBefore();
	};
    }

    /**
     * @param sourceId
     * @param viewIds
     * @return the identifiers of the views which reference the given source
     */
    private List<String> findViewsToClean(String sourceId, List<String> viewIds) {

	List<String> out = new ArrayList<>();

	if (viewIds.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).warn("WMS cache invalidation enabled, but no view configured");

	    return out;
	}

	DatabaseReader reader;

	try {

	    reader = DatabaseProviderFactory.getReader(ConfigurationWrapper.getStorageInfo());

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to create database reader: {}", ex.getMessage(), ex);

	    return out;
	}

	for (String viewId : viewIds) {

	    try {

		Optional<View> view = reader.getView(viewId);

		if (view.isEmpty()) {

		    GSLoggerFactory.getLogger(getClass()).warn("WMS cache invalidation: view {} not found", viewId);

		    continue;
		}

		List<String> sourceIds = ConfigurationWrapper.getViewSourceIdentifiers(view.get());

		if (sourceIds.isEmpty()) {

		    GSLoggerFactory.getLogger(getClass()).warn("WMS cache invalidation: view {} has no source constraints, skipped", viewId);

		    continue;
		}

		if (sourceIds.contains(sourceId)) {

		    out.add(viewId);
		}

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error("WMS cache invalidation: unable to check view {}: {}", viewId, ex.getMessage(),
			ex);
	    }
	}

	return out;
    }

    /**
     * @param views
     * @param summary
     * @param condition
     */
    private void invalidate(List<String> views, HarvestingNotifier summary, TriggerCondition condition) {

	// view -> removed layers, or the error message
	Map<String, Object> outcomes = new LinkedHashMap<>();

	String failure = null;

	try {

	    //
	    // the cache is initialized by the WMS cache filter, which could be not active on this node (e.g. a batch node)
	    //
	    if (!WMSCacheFilter.enabled) {

		WMSCacheFilter.initWMSCache();
	    }

	    if (!WMSCacheFilter.enabled) {

		GSLoggerFactory.getLogger(getClass()).warn("WMS cache not enabled, unable to invalidate views {}", views);

		failure = "The WMS cache is not enabled on this host";

	    } else {

		for (String view : views) {

		    try {

			outcomes.put(view, WMSCache.getInstance().invalidateView(view));

		    } catch (Exception ex) {

			GSLoggerFactory.getLogger(getClass()).error("WMS cache invalidation of view {} failed: {}", view, ex.getMessage(), ex);

			outcomes.put(view, ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
		    }
		}
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error("WMS cache invalidation of views {} failed: {}", views, ex.getMessage(), ex);

	    failure = ex.getMessage();
	}

	sendEmail(views, summary, condition, outcomes, failure);
    }

    /**
     * Sends the invalidation report, if the harvesting report e-mails are enabled
     *
     * @param views
     * @param summary
     * @param condition
     * @param outcomes view -> removed layers, or the error message
     * @param failure the error which prevented the invalidation, if any
     */
    @SuppressWarnings("unchecked")
    private void sendEmail(List<String> views, HarvestingNotifier summary, TriggerCondition condition, Map<String, Object> outcomes,
	    String failure) {

	if (!HarvestingReportsHandler.isEnabled()) {

	    return;
	}

	try {

	    boolean error = failure != null || outcomes.values().stream().anyMatch(o -> o instanceof String);

	    String subject = ConfiguredSMTPClient.MAIL_REPORT_SUBJECT + ConfiguredSMTPClient.MAIL_HARVESTING_SUBJECT + "[WMS CACHE INVALIDATED]"
		    + (error ? ConfiguredSMTPClient.MAIL_ERROR_SUBJECT : "");

	    StringBuilder message = new StringBuilder();

	    message.append("The WMS cache has been invalidated after the harvesting of a source belonging to the cached views.\n\n");

	    message.append("Label: ").append(summary.getSource().getLabel()).append("\n");
	    message.append("Endpoint: ").append(summary.getSource().getEndpoint()).append("\n");
	    message.append("Source id: ").append(summary.getSource().getUniqueIdentifier()).append("\n");
	    message.append("Harvesting id: ").append(summary.getHarvestingId()).append("\n");
	    message.append("Records before the harvesting #: ").append(summary.getRecordsBefore()).append("\n");
	    message.append("Records after the harvesting #: ").append(summary.getRecordsAfter()).append("\n");
	    message.append("Trigger condition: ").append(condition.getLabel()).append("\n");
	    message.append("Host: ").append(HostNamePropertyUtils.getHostNameProperty()).append("\n");
	    message.append("Date: ").append(ISO8601DateTimeUtils.getISO8601DateTime()).append("\n\n");

	    if (failure != null) {

		message.append("Invalidation of views ").append(String.join(", ", views)).append(" FAILED: ").append(failure).append("\n");

	    } else {

		outcomes.forEach((view, outcome) -> {

		    if (outcome instanceof List) {

			List<String> layers = (List<String>) outcome;

			message.append("View ").append(view).append(": ").append(layers.size()).append(" cached layer(s) removed\n");

			layers.forEach(layer -> message.append("  - ").append(layer).append("\n"));

		    } else {

			message.append("View ").append(view).append(": invalidation FAILED: ").append(outcome).append("\n");
		    }
		});
	    }

	    message.append("--- \n");

	    ConfiguredSMTPClient.sendEmail(subject, message.toString());

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to send the WMS cache invalidation e-mail: {}", ex.getMessage(), ex);
	}
    }
}
