package eu.essi_lab.shared.messages;

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
public interface IWriteSharedContent<T> {

    /**
     * Writes the provided content to shared repository. Note that if the provided content is null, the method returns
     * {@link WriteResult#SUCCESS} (i.e. nothing to write, done). It is important that implementations of this method don't throw any
     * exception, i.e. catch {@link Throwable}, since this method will be invoked several times during users' request. Errors are
     * reported in {@link SharedContentWriteResponse#getGSException()} and it is up to the invoking component to properly handle them.
     * This is done to prevent run time exceptions to stop, e.g., query execution because status can't be written.
     *
     * @param content
     * @return the {@link SharedContentWriteResponse} with execution result. The result is {@link WriteResult#ERROR} if there are any
     * exceptions in executing the write operation.
     */
    public SharedContentWriteResponse store(T content);
}
