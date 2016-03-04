package ru.l240.miband.utils.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.List;

import ru.fors.remsmed.R;

/**
 * @author Alexander Popov on 29.04.15.
 */
public class MedArrayAdapter extends ArrayAdapter {

    private Context context;

    public MedArrayAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        this.context = context;
    }

    protected View paint(View rowView, int position) {
        if (position % 2 == 0)
            rowView.setBackgroundColor(context.getResources().getColor(R.color.listview_first));
        else
            rowView.setBackgroundColor(context.getResources().getColor(R.color.listview_second));
        return rowView;
    }
}
