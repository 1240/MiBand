package ru.l240.miband.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.WindowManager;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LinePoint;

import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.fors.remsmed.R;
import ru.fors.remsmed.core.MedContract;
import ru.fors.remsmed.core.dto.Chart.ChartData;
import ru.fors.remsmed.core.dto.Chart.PointPosition;
import ru.fors.remsmed.core.dto.measurements.Measurement;
import ru.fors.remsmed.core.dto.measurements.MeasurementField;
import ru.fors.remsmed.core.dto.measurements.MeasurementsList;
import ru.fors.remsmed.core.dto.measurements.UserMeasurement;

/**
 * Created by kirill.saveliev on 03.06.2015.
 */
public class ChartUtils {

    static private Deque<Integer> colorPool = null;
    static private Map<String, Integer> colormap = null;

    static public Float findMaxValue(Map<String, ChartData> chartDataMap) {
        double maxY = 0;
        Float currentValue;

        for (Map.Entry entry : chartDataMap.entrySet()) {
            List<PointPosition> ppLst = ((ChartData) entry.getValue()).getPoint();
            for (PointPosition pp : ppLst) {
                currentValue = pp.getY();
                if (currentValue > 1000) {
                    currentValue /= 1000;
                }
                if (currentValue > maxY) {
                    maxY = currentValue;
                }
            }
        }

        return Float.valueOf(String.valueOf(maxY));
    }

    public static Float findMinValue(Map<String, ChartData> chartDataMap) {
        double minY = 65535;
        Float currentValue;

        for (Map.Entry entry : chartDataMap.entrySet()) {
            List<PointPosition> ppLst = ((ChartData) entry.getValue()).getPoint();
            for (PointPosition pp : ppLst) {
                currentValue = pp.getY();
                if (currentValue < minY) {
                    minY = currentValue;
                }
            }
        }

        return Float.valueOf(String.valueOf(minY));
    }

    static public int getColor(Resources resources, final String name, boolean state) {
        if (colorPool == null) {
            colorPool = new ArrayDeque<>();
        }
        if (colorPool.isEmpty()) {
            colorPool.add(resources.getColor(R.color.blue));
            colorPool.add(resources.getColor(R.color.blue));
            colorPool.add(resources.getColor(R.color.blue));
            colorPool.add(resources.getColor(R.color.blue));
            colorPool.add(resources.getColor(R.color.blue));
            colorPool.add(resources.getColor(R.color.blue));
        }
        if (colormap == null) {
            colormap = new HashMap<>();
        }

        if (!colormap.containsKey(name)) {
            colormap.put(name, colorPool.pop());
        }

        if (!state) {
            return Color.parseColor("#" + Integer.toHexString(colormap.get(name) & resources.getColor(R.color.faded_white)));
        } else {
            return Color.parseColor("#" + Integer.toHexString(colormap.get(name)));
        }

    }


    private LineGraph chartArea;
    private Measurement type;
    private Activity activity;
    private int SCREEN_WIDTH;
    private List<MeasurementsList> data;

    public ChartUtils(LineGraph chartArea, Measurement type, Activity activity, List<MeasurementsList> data) {
        this.chartArea = chartArea;
        this.type = type;
        this.activity = activity;
        this.data = data;

        WindowManager wm = (WindowManager) activity.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_WIDTH = size.x - 10;

        try {
            drawPlot();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        /*Line l = new Line();
        LinePoint p = new LinePoint();
        p.setX(0);
        p.setY(5);
        l.addPoint(p);
        p = new LinePoint();
        p.setX(8);
        p.setY(8);
        l.addPoint(p);
        p = new LinePoint();
        p.setX(10);
        p.setY(4);
        l.addPoint(p);
        l.setColor(Color.parseColor("#FFBB33"));

        chartArea.addLine(l);
        chartArea.setRangeY(0, 10);
        chartArea.setLineToFill(0);*/
    }

    private Map<Date, Float> xCoordMap = new LinkedHashMap<>();

    private float drawHorizontalNet(float minYCap, final Float maxYCap, float maxX) {

        Float y;
        int color = Color.parseColor("#77cacaca");
        chartArea.setShowYAxis(true);

        float yDelta = (maxYCap - minYCap) / 10;
        if (0 == yDelta) {
            return 1;
        }
        for (float i = minYCap, yStep = 0; i <= maxYCap; i += yDelta, yStep += 20) {
            Line l = new Line();
            y = Float.valueOf(i);

            LinePoint point = new LinePoint();
            point.setX(0);
            point.setY(yStep);
            point.setRealValue(y);
            l.addPoint(point);

            point = new LinePoint();
            point.setX(maxX);
            point.setY(yStep);
            point.setRealValue(y);
            l.addPoint(point);

            l.setColor(color);
            l.setShowingPoints(false);
            l.setStrokeWidth(1);
            chartArea.addLine(l);
        }

        return yDelta;
    }

    private Map<String, ChartData> chartDataMap = new HashMap<>();

    private void addChartData(String name, String unit, float yPos, Date date, boolean state) {
        if (chartDataMap.containsKey(name)) {
            ChartData chartData = chartDataMap.get(name);
            List<PointPosition> ppLst = chartData.getPoint();
            PointPosition pp = new PointPosition(xCoordMap.get(date),
                    yPos, yPos, date);
            ppLst.add(pp);
            chartData.setPoint(ppLst);
            chartDataMap.put(name, chartData);
        } else {
            List<PointPosition> ppLst = new ArrayList<>();
            PointPosition pp = new PointPosition(xCoordMap.get(date),
                    yPos, yPos, date);
            ppLst.add(pp);
            ChartData chartData = new ChartData(name, ppLst, state);
            chartData.setUnit(unit);
            chartData.setPoint(ppLst);
            chartDataMap.put(name, chartData);
        }
    }

    public LineGraph drawPlot() throws ParseException {
        chartArea.removeAllLines();
        xCoordMap.clear();
        chartDataMap.clear();
        Collections.reverse(data);

        float secDelta = finMaxTimestampDiff(data);

        Float xDelta = (float) SCREEN_WIDTH / data.size();
        Float xPos = -xDelta + xDelta / 4;
        Date dateOld = new Date();
        for (MeasurementsList ml : data) {
            List<UserMeasurement> userMeasurementList = ml.getUserMeasurementses();
            for (UserMeasurement um : userMeasurementList) {
                Date date = um.getMeasurementDate();

                if (!xCoordMap.containsKey(date)) {
                    float percentage = 0;

                    if (xCoordMap.size() > 0) {
                        float ms = (float) (date.getTime() - dateOld.getTime()) / 1000;
                        if (0f != secDelta) {
                            percentage = ms / secDelta * 100;
                        }
                    }

                    float factor = (xDelta / 100) * percentage;
                    xPos += (percentage < 50 ? xDelta - factor : xDelta + factor);
                    xCoordMap.put(date, xPos);
                    dateOld = date;
                }
            }
        }

        if (xCoordMap.isEmpty()) {
            return null;
        }

        for (MeasurementsList ml : data) {
            List<UserMeasurement> userMeasurementList = ml.getUserMeasurementses();
            for (UserMeasurement um : userMeasurementList) {
                String name = um.getMeasurementName();
                try {
                    String value = um.getMeasurementValue();
                    if (value.contains("/")) {
                        String[] bloodPressure = value.split("/");

                        addChartData("Diastole", um.getMeasurementUnit(), Float.valueOf(bloodPressure[0]),
                                um.getMeasurementDate(), true);
                        addChartData("Sustole", um.getMeasurementUnit(), Float.valueOf(bloodPressure[1]),
                                um.getMeasurementDate(), true);
                    } else {
                        addChartData(name, um.getMeasurementUnit(), Float.valueOf(value),
                                um.getMeasurementDate(), true);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        Float maxYVal;
        Float minYVal;

        maxYVal = findMaxValue(chartDataMap);
        minYVal = findMinValue(chartDataMap);

        if (maxYVal.equals(minYVal)) {
            Measurement measurement = type;
            measurement.setFields(new MeasurementField().fromCursor(activity.getApplicationContext().getContentResolver()
                    .query(MedContract.MeasurementField.CONTENT_URI,
                            MedContract.MeasurementField.DEFAULT_PROJECTION,
                            MedContract.MeasurementField.KEY_MEASUREMENT_FIELD_MEASUREMENT_ID + " = ?",
                            new String[]{measurement.getId().toString()},
                            null)));
            String typeName = measurement.getName();
            maxYVal = measurement.getMaxValue(typeName);
            minYVal = measurement.getMinValue(typeName);
        }

        chartArea.setRangeY(0, 300);

        float yScale = drawHorizontalNet(minYVal, maxYVal + (maxYVal - minYVal) / 10, (xPos > SCREEN_WIDTH ? xPos : xPos) + 70);

        int index = 0;
        for (final Map.Entry chartData : chartDataMap.entrySet()) {
            final String key = (String) chartData.getKey();
            ChartData value = (ChartData) chartData.getValue();
            String unit = value.getUnit();
            boolean state = value.getState();
            final List<PointPosition> pointLst = computeChartValues(value.getPoint(),
                    maxYVal, minYVal, yScale);

            if (!pointLst.isEmpty()) {
                int color = paintGraph(key, pointLst, state);

                Drawable mDrawable = activity.getResources().getDrawable(R.drawable.chart_measurement_label);
                assert mDrawable != null;
                mDrawable.setColorFilter(new
                        PorterDuffColorFilter(color, PorterDuff.Mode.SRC));
            }
        }

        return chartArea;
    }

    private float finMaxTimestampDiff(List<MeasurementsList> data) {
        List<Date> tempCoordList = new LinkedList<>();
        for (MeasurementsList ml : data) {
            List<UserMeasurement> userMeasurementList = ml.getUserMeasurementses();
            for (UserMeasurement um : userMeasurementList) {
                Date date = um.getMeasurementDate();

                if (!tempCoordList.contains(date)) {
                    tempCoordList.add(date);
                }
            }
        }
        float secDelta = 0;
        for (int i = 1; i < tempCoordList.size(); ++i) {
            float cSec = (int) (tempCoordList.get(i).getTime() - tempCoordList.get(i - 1).getTime()) / 1000;

            if (cSec > secDelta) {
                secDelta = cSec;
            }
        }
        return secDelta;
    }

    private int paintGraph(String key, List<PointPosition> pointLst, boolean state) {
        Line l = new Line();
        l.setTag(key);
        //l.setUsingDips(true);
        Calendar calendar = Calendar.getInstance();
        int color = activity.getResources().getColor(R.color.black);
        for (PointPosition pp : pointLst) {
            calendar.setTime(pp.getDate());
            Date date = calendar.getTime();
            l.addPoint(new LinePoint(pp.getX(), pp.getY(), date, pp.getRealValue()));
            color = getColor(activity.getResources(), key, state);
            l.setStrokeWidth(1);
            l.setActive(state);
            l.setColor(color);
        }
        chartArea.addLine(l);

        return color;
    }

    private static List<PointPosition> computeChartValues(List<PointPosition> pLst,
                                                          Float maxYVal, Float minYVal, float yScale) {

        List<PointPosition> pointLst = new ArrayList<>();
        float scale = 20 / yScale;
        for (PointPosition pp : pLst) {
            PointPosition newPP = new PointPosition();
            newPP.setX(pp.getX());
            newPP.setY((pp.getY() - minYVal) * scale);
            newPP.setDate(pp.getDate());
            newPP.setRealValue(pp.getRealValue());
            pointLst.add(newPP);

        }
        return pointLst;
    }

}
