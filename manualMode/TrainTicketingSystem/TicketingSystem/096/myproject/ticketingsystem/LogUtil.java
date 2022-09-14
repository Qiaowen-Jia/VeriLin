package ticketingsystem;

import java.util.concurrent.locks.*;

public class LogUtil {
    public static final boolean DEBUG = false;
    public static final boolean PERF  = false;
    private static final boolean TOSTDERR = true;

    public static void Log(String log) {
        if (DEBUG) {
            String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

            if (TOSTDERR) {
                System.err.println(ThreadId.get() + ": " + className + "." + methodName + "():" + lineNumber + ": " + log);
            } else {
                System.out.println(ThreadId.get() + ": " + className + "." + methodName + "():" + lineNumber + ": " + log);
            }
        }
    }
}