package sage.util;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Created by Dell on 02-06-2016.
 */
public class LogUtil {
    private static Logger L = Logger.getLogger("SageLog");

    static {
        L.setUseParentHandlers(false);
        L.addHandler(new ConsoleHandler() {
            @Override
            public void publish(LogRecord record) {
                System.out.println(
                        record.getLevel().getName().charAt(0) + " " +
                                "[" + new Date() + "]" +
                                ": " + record.getMessage()
                );
            }
        });
    }

    public void i(String msg) {
        L.log(Level.INFO, msg);
    }

    public void w(String msg) {
        L.warning(msg);
    }

    public void e(String msg) {
        L.log(Level.SEVERE, msg);
    }

    public Logger l() {
        return L;
    }
}
