package eu.essi_lab.cfga.source.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;

import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.ConfigurationSource.LockAcquisitionResult;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class ConfigurationSourceTest {

    /**
     * @param source
     * @throws Exception
     */
    public void listTest(ConfigurationSource source) throws Exception {

	boolean emptyOrMissing = source.isEmptyOrMissing();
	Assert.assertTrue(emptyOrMissing);

	List<Setting> list = source.list();
	Assert.assertTrue(list.isEmpty());

	Setting setting = new Setting();

	source.flush(Arrays.asList(setting));

	emptyOrMissing = source.isEmptyOrMissing();
	Assert.assertFalse(emptyOrMissing);

	list = source.list();
	Assert.assertEquals(1, list.size());

	list.add(new Setting());

	source.flush(list);

	emptyOrMissing = source.isEmptyOrMissing();
	Assert.assertFalse(emptyOrMissing);

	list = source.list();
	Assert.assertEquals(2, list.size());

	source.flush(Arrays.asList());

	emptyOrMissing = source.isEmptyOrMissing();
	Assert.assertTrue(emptyOrMissing);

	list = source.list();
	Assert.assertTrue(list.isEmpty());
    }

    /**
     * @param source
     * @throws Exception
     */
    public void lockTest(ConfigurationSource source) throws Exception {

	String owner = UUID.randomUUID().toString();

	Optional<String> locked = source.isLocked();
	Assert.assertFalse(locked.isPresent());

	boolean released = source.releaseLock();
	Assert.assertFalse(released);

	boolean orphanLockFound = source.orphanLockFound(1000);
	Assert.assertFalse(orphanLockFound);

	orphanLockFound = source.orphanLockFound(10000);
	Assert.assertFalse(orphanLockFound);

	//
	//
	//

	LockAcquisitionResult acquired = source.acquireLock(owner);
	Assert.assertEquals(LockAcquisitionResult.SUCCEEDED, acquired);

	acquired = source.acquireLock("anotherOwner");
	Assert.assertEquals(LockAcquisitionResult.REJECTED, acquired);

	acquired = source.acquireLock(owner);
	Assert.assertEquals(LockAcquisitionResult.OWNED, acquired);

	locked = source.isLocked();
	Assert.assertTrue(locked.isPresent());
	Assert.assertEquals(owner, locked.get());

	//
	//
	//

	released = source.releaseLock();
	Assert.assertTrue(released);

	released = source.releaseLock();
	Assert.assertFalse(released);

	locked = source.isLocked();
	Assert.assertFalse(locked.isPresent());

	//
	//
	//

	acquired = source.acquireLock(owner);
	Assert.assertEquals(LockAcquisitionResult.SUCCEEDED, acquired);

	acquired = source.acquireLock(owner);
	Assert.assertEquals(LockAcquisitionResult.OWNED, acquired);

	acquired = source.acquireLock("anotherOwner");
	Assert.assertEquals(LockAcquisitionResult.REJECTED, acquired);

	acquired = source.acquireLock(owner);
	Assert.assertEquals(LockAcquisitionResult.OWNED, acquired);

	locked = source.isLocked();
	Assert.assertTrue(locked.isPresent());
	Assert.assertEquals(owner, locked.get());

	//
	//
	//

	Thread.sleep(5000);

	orphanLockFound = source.orphanLockFound(1000);
	Assert.assertTrue(orphanLockFound);

	orphanLockFound = source.orphanLockFound(2000);
	Assert.assertTrue(orphanLockFound);

	orphanLockFound = source.orphanLockFound(2500);
	Assert.assertTrue(orphanLockFound);

	orphanLockFound = source.orphanLockFound(3500);
	Assert.assertTrue(orphanLockFound);

	orphanLockFound = source.orphanLockFound(4000);
	Assert.assertTrue(orphanLockFound);

	orphanLockFound = source.orphanLockFound(4500);
	Assert.assertTrue(orphanLockFound);

	orphanLockFound = source.orphanLockFound(9000);
	Assert.assertFalse(orphanLockFound);

	orphanLockFound = source.orphanLockFound(10000);
	Assert.assertFalse(orphanLockFound);

	orphanLockFound = source.orphanLockFound(15000);
	Assert.assertFalse(orphanLockFound);

	//
	//
	//

	released = source.releaseLock();
	Assert.assertTrue(released);

	released = source.releaseLock();
	Assert.assertFalse(released);

	locked = source.isLocked();
	Assert.assertFalse(locked.isPresent());
    }
}
