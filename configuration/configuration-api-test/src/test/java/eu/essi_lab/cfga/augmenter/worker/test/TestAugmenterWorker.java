package eu.essi_lab.cfga.augmenter.worker.test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;

import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.augmenter.worker.AugmenterWorker;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;

/**
 * @author Fabrizio
 */
public class TestAugmenterWorker extends AugmenterWorker {

    private boolean testSucceeded;

    public TestAugmenterWorker() {

	this.testSucceeded = true;
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	GSLoggerFactory.getLogger(this.getClass()).info("Starting Augmenter Job");

	int maxRecords = getSetting().getMaxRecords();
	testSucceeded &= 5 == maxRecords;

	boolean orderingSet = getSetting().isLessRecentSortSet();
	testSucceeded &= orderingSet;

	int timeBack = getSetting().getMaxAge();
	testSucceeded &= 7 == timeBack;

	//
	// ---
	//

	List<String> selSourcesIds = getSetting().//
		getSelectedSourcesIds().//
		stream().//
		sorted().//
		collect(Collectors.toList());

	List<String> expSourcesIds = ConfigurationWrapper.getHarvestedAndMixedSources().//
		stream().//
		map(s -> s.getUniqueIdentifier()).//
		sorted().//
		collect(Collectors.toList());

	testSucceeded &= selSourcesIds.equals(expSourcesIds);

	//
	// ---
	//
	Bond sourcesBond = getSetting().getSourcesBond();

	testSucceeded &= sourcesBond instanceof LogicalBond;
	LogicalBond orBond = (LogicalBond) sourcesBond;

	testSucceeded &= orBond.getLogicalOperator() == LogicalOperator.OR;

	List<String> bondSourcesIds = orBond.getOperands().//
		stream().//
		map(o -> ((ResourcePropertyBond) o).getPropertyValue()).//
		sorted().//
		collect(Collectors.toList());

	testSucceeded &= bondSourcesIds.equals(expSourcesIds);

	//
	// --- here the augmenters are created from the related setting, ready to be executed
	//
	@SuppressWarnings("rawtypes")
	List<Augmenter> augmenters = getSetting().//
		getSelectedAugmenterSettings().//
		stream().//
		sorted(Comparator.comparing(AugmenterSetting::getPriority)).//
		map(s -> {
		    try {
			return (Augmenter) s.createConfigurable();
		    } catch (Exception e) {
			e.printStackTrace();
		    }

		    return null;
		}). //
		collect(Collectors.toList());

	testSucceeded &= !augmenters.isEmpty();

	for (int i = 0; i < augmenters.size(); i++) {

	    testSucceeded &= i == augmenters.get(i).getSetting().getPriority();
	}
    }

    /**
     * @return
     */
    public boolean testPassed() {

	return testSucceeded;
    }

    @Override
    public String getType() {

	return "TestAugmenterWorker";
    }

}
