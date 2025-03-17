/**
 * 
 */
package eu.essi_lab.database.ftp.server;

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

	return commandFactory.getCommand(cmdName);
    }
}
