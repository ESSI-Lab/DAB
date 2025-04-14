/**
 * 
 */
package eu.essi_lab.database.ftp.server;

import java.io.IOException;

import org.apache.ftpserver.command.impl.OPTS;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.LocalizedFtpReply;

/**
 * @author Fabrizio
 */
public class OPTS_MLSD extends OPTS {

    @Override
    public void execute(//
	    FtpIoSession session, //
	    FtpServerContext context, //
	    FtpRequest request) throws IOException, FtpException {

	session.resetState();

	String argument = request.getArgument();

	if (argument != null && argument.startsWith("MLSD")) {

	    int reply = FtpReply.REPLY_200_COMMAND_OKAY;

	    try {

		String opts = argument.replace("MLSD ", "");
		String[] split = opts.split(" - ");

		if (split.length == 2) {

		    DatabaseFtpFile.setStartIndex(Integer.valueOf(split[0]));
		    DatabaseFtpFile.setMaxListSize(Integer.valueOf(split[1]));

		} else {

		    Integer size = Integer.valueOf(opts);
		    DatabaseFtpFile.setMaxListSize(size);
		}
	    } catch (Exception ex) {

		reply = FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS;
	    }

	    session.write(LocalizedFtpReply.translate(session, request, context, reply, "OPTS.MLSD", null));

	    return;
	}

	super.execute(session, context, request);
    }
}
