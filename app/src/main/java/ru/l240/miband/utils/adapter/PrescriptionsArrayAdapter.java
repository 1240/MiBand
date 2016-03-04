package ru.l240.miband.utils.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import ru.fors.remsmed.R;
import ru.fors.remsmed.core.dto.prescriptions.PrescrTemplate;
import ru.fors.remsmed.core.dto.prescriptions.PrescriptionItem;
import ru.fors.remsmed.db.DBHelper;

/**
 * @author Alexander Popov on 20.04.15.
 */
public class PrescriptionsArrayAdapter extends MedArrayAdapter {

    private final Context context;
    private final List<PrescriptionItem> values;
    private Date from;
    private Date to;

    public PrescriptionsArrayAdapter(Context context, List<PrescriptionItem> values, Date from, Date to) {
        super(context, R.layout.prescriprions_history_listitem, values);
        this.context = context;
        this.values = values;
        this.from = from;
        this.to = to;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        PrescriptionItem item = values.get(position);
        View rowView = inflater.inflate(R.layout.prescriprions_history_listitem, parent, false);
        ProgressBar progressBarPrescriptionsHistoryListItem = (ProgressBar) rowView.findViewById(R.id.pbPrescriptionsHistoryListItem);
        TextView tvPrescriptionsHistoryListItemDayTime = (TextView) rowView.findViewById(R.id.tvPrescriptionsHistoryListItemDayTime);
        TextView tvPrescriptionsHistoryListItemDes = (TextView) rowView.findViewById(R.id.tvPrescriptionsHistoryListItemDes);
        TextView tvPrescriptionsHistoryListItemMessage = (TextView) rowView.findViewById(R.id.tvPrescriptionsHistoryListItemMessage);
        DBHelper dbHelper = new DBHelper(context);
        List<PrescrTemplate> templates = dbHelper.getAllExecTemplateByPrescId(item.getId());
        String times = "";
        for (PrescrTemplate template : templates) {
            times += template.getHour() + ":00" + ((templates.indexOf(template) == (templates.size() - 1)) ? "" : ", ");
        }
        tvPrescriptionsHistoryListItemDayTime.setText(times);
        tvPrescriptionsHistoryListItemDes.setText(item.getDescription());
        tvPrescriptionsHistoryListItemMessage.setText(item.getDescription());


        Rect bounds = progressBarPrescriptionsHistoryListItem.getProgressDrawable().getBounds();
        Integer allPrescrExecutionsCount = dbHelper.getAllPrescrExecutionsCountByDate(item.getId(), from, to);
        Integer allPrescrCount = dbHelper.getPrescriptionsCount(item.getId(), to, from);
        int value = (int) (allPrescrExecutionsCount / Double.valueOf(allPrescrCount) * 100);
        if (value < 25) {
            progressBarPrescriptionsHistoryListItem.setProgressDrawable(rowView.getResources().getDrawable(R.drawable.horizontal_progress_drawable_25));
        } else if (value <= 50) {
            progressBarPrescriptionsHistoryListItem.setProgressDrawable(rowView.getResources().getDrawable(R.drawable.horizontal_progress_drawable_50));
        } else if (value <= 75) {
            progressBarPrescriptionsHistoryListItem.setProgressDrawable(rowView.getResources().getDrawable(R.drawable.horizontal_progress_drawable_75));
        } else {
            progressBarPrescriptionsHistoryListItem.setProgressDrawable(rowView.getResources().getDrawable(R.drawable.horizontal_progress_drawable_100));
        }
        progressBarPrescriptionsHistoryListItem.getProgressDrawable().setBounds(bounds);
        progressBarPrescriptionsHistoryListItem.setProgress(value);

        rowView = paint(rowView, position);

        return rowView;
    }

}
