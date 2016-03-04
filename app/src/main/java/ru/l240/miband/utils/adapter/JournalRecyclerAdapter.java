package ru.l240.miband.utils.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import ru.fors.remsmed.R;
import ru.fors.remsmed.core.MedContract;

/**
 * @author Alexander Popov created on 14.08.2015.
 */
public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat dbdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Cursor mCursor;
    private final Context mContext;

    public JournalRecyclerAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.journal_listitem, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            String text = mCursor.getString(mCursor.getColumnIndex(MedContract.Journal.KEY_JOURNAL_TEXT));
            String date = null;
            try {
                date = dateFormat.format(dbdateFormat.parse(mCursor.getString(mCursor.getColumnIndex(MedContract.Journal.KEY_JOURNAL_DATE))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.text.setText(text);
            holder.date.setText(date);
            holder.itemView = paint(holder.itemView, position);
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView text;
        public TextView date;
        public View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            date = (TextView) itemView.findViewById(R.id.tvJounalLIDate);
            text = (TextView) itemView.findViewById(R.id.tvJounalLIMessage);
        }
    }

    private View paint(View rowView, int position) {
        if (position % 2 == 0)
            rowView.setBackgroundColor(mContext.getResources().getColor(R.color.listview_first));
        else
            rowView.setBackgroundColor(mContext.getResources().getColor(R.color.listview_second));
        return rowView;
    }


}
