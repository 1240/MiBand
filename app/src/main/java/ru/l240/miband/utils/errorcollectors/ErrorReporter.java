package ru.l240.miband.utils.errorcollectors;

import android.content.Context;

/**
 * @author Alexander Popov created on 14.10.2015.
 */
public class ErrorReporter {

    private ErrorReporter() {}

    /**
     * Apply error reporting to a specified application context
     * @param context context for which errors are reported (used to get package name)
     */
    public static void bindReporter(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.inContext(context));
    }

    public static void reportError(Context context, Throwable error) {
        ExceptionHandler.reportOnlyHandler(context).uncaughtException(Thread.currentThread(), error);
    }

}
