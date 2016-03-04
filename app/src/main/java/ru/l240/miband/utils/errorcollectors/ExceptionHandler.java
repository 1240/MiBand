package ru.l240.miband.utils.errorcollectors;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit.Call;
import ru.fors.remsmed.core.MedContract;
import ru.fors.remsmed.core.dto.MedDTO;
import ru.fors.remsmed.core.dto.journal.JournalItem;
import ru.fors.remsmed.core.retrofitloaders.api.ApiFactory;
import ru.fors.remsmed.core.retrofitloaders.api.MedRetrofitService;
import ru.fors.remsmed.core.retrofitloaders.api.RetrofitCallback;
import ru.fors.remsmed.db.RequestTaskReauthorize;
import ru.fors.remsmed.utils.MedUtils;

/**
 * @author Alexander Popov created on 14.10.2015.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static final String TAG = ExceptionHandler.class.getSimpleName();

    private final DateFormat formatter = new SimpleDateFormat("dd.MM.yy HH:mm");
    private final DateFormat fileFormatter = new SimpleDateFormat("dd-MM-yy");
    private String versionName = "0";
    private int versionCode = 0;
    private final String stacktraceDir;
    private final Thread.UncaughtExceptionHandler previousHandler;
    private Context mContext;

    private ExceptionHandler(Context context, boolean chained) {
        mContext = context;
        PackageManager mPackManager = context.getPackageManager();
        PackageInfo mPackInfo;
        try {
            mPackInfo = mPackManager.getPackageInfo(context.getPackageName(), 0);
            versionName = mPackInfo.versionName;
            versionCode = mPackInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // ignore
        }
        if(chained)
            previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        else
            previousHandler = null;
        stacktraceDir = String.format("/Android/data/%s/files/", context.getPackageName());
    }

    static ExceptionHandler inContext(Context context) {
        return new ExceptionHandler(context, true);
    }

    static ExceptionHandler reportOnlyHandler(Context context) {
        return new ExceptionHandler(context, false);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        final String state = Environment.getExternalStorageState();
        final Date dumpDate = new Date(System.currentTimeMillis());
        if (Environment.MEDIA_MOUNTED.equals(state)) {

            StringBuilder reportBuilder = new StringBuilder();
            reportBuilder
                    .append("\n\n\n")
                    .append(formatter.format(dumpDate)).append("\n")
                    .append(String.format("Version: %s (%d)\n", versionName, versionCode))
                    .append(thread.toString()).append("\n");
            processThrowable(exception, reportBuilder);
            
            sendJournal(reportBuilder.toString(), mContext);
            
            File sd = Environment.getExternalStorageDirectory();
            File stacktrace = new File(
                    sd.getPath() + stacktraceDir,
                    String.format(
                            "stacktrace-%s.txt",
                            fileFormatter.format(dumpDate)));
            File dumpdir = stacktrace.getParentFile();
            boolean dirReady = dumpdir.isDirectory() || dumpdir.mkdirs();
            if (dirReady) {
                FileWriter writer = null;
                try {
                    writer = new FileWriter(stacktrace, true);
                    writer.write(reportBuilder.toString());
                } catch (IOException e) {
                    // ignore
                } finally {
                    try {
                        if (writer != null)
                            writer.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        if(previousHandler != null)
            previousHandler.uncaughtException(thread, exception);
    }

    private void processThrowable(Throwable exception, StringBuilder builder) {
        if(exception == null)
            return;
        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        builder
                .append("Exception: ").append(exception.getClass().getName()).append("\n")
                .append("Message: ").append(exception.getMessage()).append("\nStacktrace:\n");
        for(StackTraceElement element : stackTraceElements) {
            builder.append("\t").append(element.toString()).append("\n");
        }
        processThrowable(exception.getCause(), builder);
    }
    
    
    private void sendJournal(String text, Context context) {
        final JournalItem item = new JournalItem();
        item.setDate(new Date());
        item.setMessage(text);
        if (MedUtils.isNetworkConnected(context)) {
            MedRetrofitService service = ApiFactory.getMedService();
            SharedPreferences preferences = context.getSharedPreferences(MedUtils.COOKIE_PREF, 0);
            String cookie = preferences.getString(MedUtils.COOKIE_PREF, "");
            Call<List<MedDTO>> journalRecords = service.postAddJournalRecords(Collections.singletonList(item), cookie);
            journalRecords.enqueue(new RetrofitCallback<List<MedDTO>>() {
                @Override
                public void onResponse(retrofit.Response<List<MedDTO>> response) {
                    if (response.isSuccess()) {
                        List<MedDTO> list = response.body();
                        for (MedDTO medO : list) {
                            item.setId(medO.getId());
                            context.getContentResolver().insert(MedContract.Journal.CONTENT_URI,
                                    item.toContentValues());
                        }
                    } else {
                        RequestTaskReauthorize requestTaskReauthorize = new RequestTaskReauthorize(context);
                        requestTaskReauthorize.execute();
                    }
                    super.onResponse(response);
                }

                @Override
                public void onFailure(Throwable t) {
                    super.onFailure(t);
                }
            });
        } else {
//                                    dbHelper.createSyncJournal(item);
            item.setId(new MedDTO<JournalItem>().getSyncId(MedContract.Journal.CONTENT_URI,
                    context));
            context.getContentResolver().insert(MedContract.Journal.CONTENT_URI,
                    item.toContentValues());
            SharedPreferences preferences = context.getSharedPreferences(MedUtils.SCHEDULER_PREF, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(MedUtils.SCHEDULER_JOURNAL_PREF, "need");
            editor.commit();
        }
    }
}
