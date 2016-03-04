package ru.l240.miband.utils.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import ru.fors.remsmed.R;
import ru.fors.remsmed.core.dto.journal.JournalItem;

/**
 * @author Alexander Popov on 08.05.15.
 */
public class JournalArrayAdapter extends MedArrayAdapter {

    private final Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final List<JournalItem> values;


    public JournalArrayAdapter(Context context, List<JournalItem> values) {
        super(context, R.layout.journal_listitem, values);
        this.context = context;
        this.values = values;
    }


    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.journal_listitem, parent, false);
        JournalItem journalItem = values.get(position);
        final String sDate = String.valueOf(dateFormat.format(journalItem.getDate()));
        final String sComment = journalItem.getMessage();
        TextView day = (TextView) rowView.findViewById(R.id.tvJounalLIDate);
        TextView message = (TextView) rowView.findViewById(R.id.tvJounalLIMessage);

        day.setText(sDate);
        message.setText(sComment);

        rowView = paint(rowView, position);
        return rowView;


    }

}
