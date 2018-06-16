/* Copyright Deloitte Consulting LLP 2018 */

package BlankWorkerSupport;

import improbable.worker.Connection;
import improbable.worker.LogLevel;

public class Logger {

    private Connection connection;
    private String workerId;

    public void info(String message) {
        this.sendLogMessage(LogLevel.INFO, message);
    }

    public void warn(String message) {
        this.sendLogMessage(LogLevel.WARN, message);
    }

    public void error(String message) {
        this.sendLogMessage(LogLevel.ERROR, message);
    }

    public void error(Exception ex) {
        this.sendLogMessage(LogLevel.ERROR, exceptionToString(ex));
    }

    private String exceptionToString(Exception ex) {
        StringBuilder output = new StringBuilder(ex.getMessage());
        for( StackTraceElement ste : ex.getStackTrace() ){
            output.append('\n')
                    .append( ste.getFileName() )
                    .append(':')
                    .append( ste.getLineNumber() )
                    .append('\t')
                    .append( ste.getClassName() )
                    .append( '.' )
                    .append( ste.getMethodName() );
        }
        return output.toString();
    }

    public void sendLogMessage(LogLevel level, String message) {
        if (this.connection == null) {
            System.err.println(level + "  " + message);
        } else {
            this.connection.sendLogMessage(level, this.workerId, message);
        }
    }

    public void connect(Connection connection, String workerId) {
        this.connection = connection;
        this.workerId = workerId;
    }
}
