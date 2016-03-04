package ru.l240.miband.utils.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import ru.fors.remsmed.R;
import ru.fors.remsmed.core.dto.home.Alert;

/**
 * @author Alexander Popov on 08.05.15.
 */
public class HomeArrayAdapter extends MedArrayAdapter {

    private final Context context;
    private final List<Alert> values;


    public HomeArrayAdapter(Context context, List<Alert> values) {
        super(context, R.layout.home_listitem, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.home_listitem, parent, false);

        ImageView ivAlert = (ImageView) rowView.findViewById(R.id.ivHomeAlert);
        TextView tvAlertDes = (TextView) rowView.findViewById(R.id.tvAlertDes);
        TextView tvAlertTime = (TextView) rowView.findViewById(R.id.tvAlertTime);
        TextView tvAlertTitle = (TextView) rowView.findViewById(R.id.tvAlertTitle);

        Alert alert = values.get(position);
        switch (alert.getImageId()) {
            case 1:
                ivAlert.setImageResource(R.mipmap.alert_icon1);
                break;
            case 2:
                ivAlert.setImageResource(R.mipmap.alert_icon2);
                break;
            case 3:
                ivAlert.setImageResource(R.mipmap.alert_icon3);
                break;
            case 4:
                ivAlert.setImageResource(R.mipmap.alert_icon4);
                break;
            case 5:
                ivAlert.setImageResource(R.mipmap.alert_icon_msg);
                break;
            default:
                break;
        }

        tvAlertDes.setText(alert.getDescriptions());
        tvAlertTime.setText(new SimpleDateFormat("HH:mm").format(alert.getTime()));
        tvAlertTitle.setText(alert.getTitle());

        rowView = paint(rowView, position);
        return rowView;
    }
}
