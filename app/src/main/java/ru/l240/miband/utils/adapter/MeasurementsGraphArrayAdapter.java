package ru.l240.miband.utils.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.echo.holographlibrary.LineGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ru.fors.remsmed.R;
import ru.fors.remsmed.core.MedContract;
import ru.fors.remsmed.core.dto.measurements.Measurement;
import ru.fors.remsmed.core.dto.measurements.MeasurementsList;
import ru.fors.remsmed.core.dto.measurements.UserMeasurement;
import ru.fors.remsmed.utils.ChartUtils;
import ru.fors.remsmed.utils.MedUtils;

/**
 * @author Alexander Popov on 28.04.15.
 */
public class MeasurementsGraphArrayAdapter extends ArrayAdapter {

    private final Activity activity;
    private final List<Measurement> types;
    private Date dateFrom;
    private Date dateTo;

    public MeasurementsGraphArrayAdapter(Activity activity, List<Measurement> types, Date from, Date to) {
        super(activity, R.layout.measurementlist_graph_listitem, types);
        this.dateFrom = from;
        this.dateTo = to;
        this.activity = activity;
        int j = -1;
        for (int i = 0; i < types.size(); i++) {
            if (types.get(i).getSysName().equals("height")) {
                j = i;
                break;
            }
        }
        if (j != -1)
            types.remove(j);
        this.types = types;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.measurementlist_graph_listitem, parent, false);
        Measurement type = types.get(position);
        TextView tvMeasurementGraph = (TextView) rowView.findViewById(R.id.tvMeasurementGraph);
        TextView tvMeasurementGraphNoData = (TextView) rowView.findViewById(R.id.tvMeasurementGraphNoData);
        LineGraph lineGraph = (LineGraph) rowView.findViewById(R.id.graph);
        List<MeasurementsList> measurementsLists = initData(dateFrom, dateTo, type);
        tvMeasurementGraph.setText(String.format("%s, %s", type.getName(), type.getUnit()));
        if (!measurementsLists.isEmpty()) {
            ChartUtils chartUtils = new ChartUtils(lineGraph,
                    type,
                    activity,
                    measurementsLists);
            tvMeasurementGraphNoData.setVisibility(View.GONE);
        } else {
            tvMeasurementGraphNoData.setVisibility(View.VISIBLE);
            lineGraph.setVisibility(View.GONE);
        }
        return rowView;
    }

    private List<MeasurementsList> initData(Date from, Date to, Measurement type) {
        List<MeasurementsList> lists = new ArrayList<>();
        List<UserMeasurement> items = new ArrayList<>();
        List<UserMeasurement> allMeasurements = new UserMeasurement().fromCursor(getContext().getContentResolver()
                .query(MedContract.UserMeasurement.CONTENT_URI,
                        MedContract.UserMeasurement.DEFAULT_PROJECTION,
                        MedContract.UserMeasurement.KEY_USER_MEASUREMENT_DATE
                                + " BETWEEN '"
                                + new MedUtils().dfDB().format(from)
                                + " 00:00:00' AND '"
                                + new MedUtils().dfDB().format(to)
                                + " 99:99:99'"
                                + " AND (" + MedContract.UserMeasurement.KEY_USER_MEASUREMENT_TYPE + "= ? )",
                        new String[]{type.getId().toString()},
                        MedContract.UserMeasurement.DEFAULT_SORT));
        if (allMeasurements.isEmpty()) {
            return lists;
        }
        MeasurementsList measurementsList = new MeasurementsList();

        for (UserMeasurement userMeasurement : allMeasurements) {
            if (measurementsList.getDate() == null) {
                measurementsList.setDate(userMeasurement.getMeasurementDate());
                measurementsList.setTime(userMeasurement.getMeasurementDate());
                items.add(userMeasurement);
                continue;
            } else {
                if (measurementsList.getDate().equals(userMeasurement.getMeasurementDate())) {
                    items.add(userMeasurement);
                } else {
                    measurementsList.setUserMeasurementses(items);
                    items = new ArrayList<>();
                    lists.add(measurementsList);
                    measurementsList = new MeasurementsList();
                    measurementsList.setDate(userMeasurement.getMeasurementDate());
                    measurementsList.setTime(userMeasurement.getMeasurementDate());
                    items.add(userMeasurement);
                }
            }
        }
        measurementsList.setUserMeasurementses(items);
        lists.add(measurementsList);
        Collections.reverse(lists);
        return lists;
    }

}
