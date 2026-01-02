/**
 * 
 */
package eu.essi_lab.database.ftp.server;

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

import org.apache.ftpserver.command.Command;
import org.apache.ftpserver.command.CommandFactory;
import org.apache.ftpserver.command.impl.DefaultCommandFactory;

/**
 * @author Fabrizio
 */
public class DatabaseFtpCommandFactory extends DefaultCommandFactory {

    /**
     * @param commandFactory
     * @return
     */
    public static DatabaseFtpCommandFactory get(CommandFactory commandFactory) {

	return new DatabaseFtpCommandFactory(commandFactory);
    }

    private CommandFactory commandFactory;

    /**
     * @param commandFactory
     */
    private DatabaseFtpCommandFactory(CommandFactory commandFactory) {

	this.commandFactory = commandFactory;
    }

    @Override
    public Command getCommand(final String cmdName) {

	if (cmdName.equals("OPTS")) {

	    return new OPTS_MLSD();
	}

	return commandFactory.getCommand(cmdName);
    }
}
