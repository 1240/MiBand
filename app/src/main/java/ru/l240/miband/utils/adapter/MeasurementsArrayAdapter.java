package ru.l240.miband.utils.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import ru.fors.remsmed.R;
import ru.fors.remsmed.core.dto.measurements.MeasurementsList;
import ru.fors.remsmed.core.dto.measurements.UserMeasurement;

/**
 * @author Alexander Popov on 28.04.15.
 */
public class MeasurementsArrayAdapter extends ArrayAdapter {

    private final Context context;
    private final List<MeasurementsList> values;


    public MeasurementsArrayAdapter(Context context, List<MeasurementsList> values) {
        super(context, R.layout.measurementslist_listitem, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.measurementslist_listitem, parent, false);
        TextView measurementsDay = (TextView) rowView.findViewById(R.id.measurementsDay);
        TextView measurementsTime = (TextView) rowView.findViewById(R.id.measurementsTime);
        TextView measurementsDes = (TextView) rowView.findViewById(R.id.measurementsDescription);
        MeasurementsList measurementsList = values.get(position);
        measurementsDay.setText(new SimpleDateFormat("dd/MM/yyyy").format(values.get(position).getDate()));
        measurementsTime.setText(new SimpleDateFormat("HH:mm").format(values.get(position).getTime()));
        if (measurementsList.getDescriptions() == null) {
            measurementsDes.setVisibility(View.GONE);
        } else {
            measurementsDes.setText(measurementsList.getDescriptions());
        }
        TableLayout measurementsListItemLL = (TableLayout) rowView.findViewById(R.id.measurementsListItemLL);
        int size = values.get(position).getUserMeasurementses().size();
        for (int i = 0; i < size; i++) {
            UserMeasurement item = values.get(position).getUserMeasurementses().get(i);
            measurementsListItemLL.addView(getChildView(item, i % 2 == 1));
        }

        return rowView;
    }

    private View getChildView(UserMeasurement item, boolean isWhite) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.measurementsitem_listitem, null, false);

        TextView measure = (TextView) rowView.findViewById(R.id.measurementsMeasure);
        TextView value = (TextView) rowView.findViewById(R.id.measurementsValue);
        TextView unit = (TextView) rowView.findViewById(R.id.measurementsUnit);
        measure.setText(item.getMeasurementName());
        value.setText(item.getMeasurementValue());
        unit.setText(item.getMeasurementUnit());
        if (!isWhite) {
            rowView.setBackgroundResource(R.color.listview_first);
        } else {
            rowView.setBackgroundResource(R.color.listview_second);
        }
        return rowView;
    }

}
