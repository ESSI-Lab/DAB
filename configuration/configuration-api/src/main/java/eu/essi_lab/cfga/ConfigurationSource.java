package eu.essi_lab.cfga;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public interface ConfigurationSource {

    /**
     * @author Fabrizio
     */
    public enum LockAcquisitionResult {

	/**
	 * The lock is acquired
	 */
	SUCCEEDED,
	/**
	 * The lock is already owned
	 */
	OWNED,
	/**
	 * The lock is owned by another owner
	 */
	REJECTED
    }
    
    /**
     * 
     * @return
     */
    InputStream getStream() throws Exception;

    /**
     * @return
     * @throws Exception
     */
    List<Setting> list() throws Exception;

    /**
     * @param settings
     * @throws Exception
     */
    void flush(List<Setting> settings) throws Exception;

    /**
     * @return
     * @throws Exception
     */
    public boolean isEmptyOrMissing() throws Exception;

    /**
     * Tries to acquire the write lock for the given <code>owner</code> on the source.<br>
     * If the source is already locked by another owner, this method returns {@link LockAcquisitionResult#REJECTED}.<br>
     * If the source is already locked
     * by the given <code>owner</code>, the lock acquisition is skipped and returns
     * {@link LockAcquisitionResult#OWNED}.<br>
     * If the source is not locked, the lock is acquired and returns {@link LockAcquisitionResult#SUCCEEDED}.<br>
     * In the latter two cases, the lock is updated with the current time stamp {@link System#currentTimeMillis()}
     * 
     * @return
     * @throws Exception
     */
    public LockAcquisitionResult acquireLock(String owner) throws Exception;

    /**
     * @return
     * @throws Exception
     */
    public boolean releaseLock() throws Exception;

    /**
     * A lock is orphan if the time passed from its last time stamp is greater than <code>maxIdleTime</code>.<br>
     * <code>maxIdleTime</code> must be enough in order to be sure to assume that the lock owner is offline.
     * If <code>maxIdleTime</code> is too short, a new coming node could try to remove a lock owned by a currently
     * online owner
     * 
     * @param maxIdleTime
     * @return
     */
    public boolean orphanLockFound(long maxIdleTime) throws Exception;

    /**
     * @return
     * @throws Exception
     */
    public Optional<String> isLocked() throws Exception;

    /**
     * @throws IOException
     */
    ConfigurationSource backup() throws Exception;

    /**
     * @return
     */
    public String getLocation();

}
