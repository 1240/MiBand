package ru.l240.miband.utils;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;

import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Popov created on 12.10.2015.
 */
public class FitUtils {

    public DataSet createDataForRequest(DataType dataType, int dataSourceType, Object values,
                                         long startTime, long endTime, TimeUnit timeUnit) {
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName("ru.fors.remsmed")
                .setDataType(dataType)
                .setType(dataSourceType)
                .build();

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint().setTimeInterval(startTime, endTime, timeUnit);

        if (values instanceof Integer) {
            dataPoint = dataPoint.setIntValues((Integer)values);
        } else {
            dataPoint = dataPoint.setFloatValues((Float)values);
        }

        dataSet.add(dataPoint);

        return dataSet;
    }

    public DataSet createDataForRequest(DataType dataType, int dataSourceType, Object values,
                                         long startTime, TimeUnit timeUnit) {
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName("ru.fors.remsmed")
                .setDataType(dataType)
                .setType(dataSourceType)
                .build();

        DataSet dataSet = DataSet.create(dataSource);
        DataPoint dataPoint = dataSet.createDataPoint().setTimestamp(startTime, timeUnit);

        if (values instanceof Integer) {
            dataPoint = dataPoint.setIntValues((Integer)values);
        } else {
            dataPoint = dataPoint.setFloatValues((Float)values);
        }

        dataSet.add(dataPoint);

        return dataSet;
    }

}
