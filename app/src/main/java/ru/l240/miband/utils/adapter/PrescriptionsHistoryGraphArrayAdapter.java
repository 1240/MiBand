package ru.l240.miband.utils.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ru.fors.remsmed.R;
import ru.fors.remsmed.core.dto.prescriptions.PrescriptionItem;
import ru.fors.remsmed.db.DBHelper;
import ru.fors.remsmed.utils.DateUtils;

/**
 * @author Alexander Popov on 20.04.15.
 */
public class PrescriptionsHistoryGraphArrayAdapter extends MedArrayAdapter {

    private final Context context;
    private final List<PrescriptionItem> values;
    private Date from;
    private Date to;

    public PrescriptionsHistoryGraphArrayAdapter(Context context, List<PrescriptionItem> values, Date from, Date to) {
        super(context, R.layout.prescriprions_history_graph_listitem, values);
        this.context = context;
        this.values = values;
        this.from = from;
        this.to = to;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.prescriprions_history_graph_listitem, parent, false);
        DBHelper dbHelper = new DBHelper(context);
        TextView tvDrag = (TextView) rowView.findViewById(R.id.tvPrescriptionsHistoryGraphListItemMessage);
        LinearLayout ll = (LinearLayout) rowView.findViewById(R.id.llPrescriptionsHistoryGraphListItem);
        PrescriptionItem value = values.get(position);
        tvDrag.setText(value.getDescription());

        for (Date date = from; date.before(DateUtils.addDays(to, 1)); date = DateUtils.addDays(date, 1)) {

            View listItem = LayoutInflater.from(context).inflate(R.layout.prescriprions_history_graph_listitem_item, parent, false);

            ProgressBar pb = (ProgressBar) listItem.findViewById(R.id.pbPrescriptionsHistoryGraphListitemItem);
            TextView tvListItem = (TextView) listItem.findViewById(R.id.tvPrescriptionsHistoryGraphListitemItemMessage);
            Rect bounds = pb.getProgressDrawable().getBounds();

            Integer allPrescrExecutionsCount = dbHelper.getAllPrescrExecutionsCountByDate(value.getId(), date, date);
            Integer allPrescrCount = dbHelper.getPrescriptionsCount(value.getId(), date, date);
            int progress = (int) (allPrescrExecutionsCount / Double.valueOf(allPrescrCount) * 100);
            if (progress < 25) {
                pb.setProgressDrawable(rowView.getResources().getDrawable(R.drawable.horizontal_progress_drawable_25));
            } else if (progress <= 50) {
                pb.setProgressDrawable(rowView.getResources().getDrawable(R.drawable.horizontal_progress_drawable_50));
            } else if (progress <= 75) {
                pb.setProgressDrawable(rowView.getResources().getDrawable(R.drawable.horizontal_progress_drawable_75));
            } else {
                pb.setProgressDrawable(rowView.getResources().getDrawable(R.drawable.horizontal_progress_drawable_100));
            }
            pb.getProgressDrawable().setBounds(bounds);
            pb.setProgress(progress);
            tvListItem.setText(new SimpleDateFormat("dd/MM").format(date));
            ll.addView(listItem);
        }
        return paint(rowView, position);
    }

}
