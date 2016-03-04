package ru.l240.miband.utils.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import ru.fors.remsmed.R;
import ru.fors.remsmed.core.MedContract;

/**
 * @author Alexander Popov created on 14.08.2015.
 */
public class OrderRecyclerAdapter extends RecyclerView.Adapter<OrderRecyclerAdapter.ViewHolder> {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat dbdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Cursor mCursor;
    private final Context mContext;

    public OrderRecyclerAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.order_listitem, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            String text = mCursor.getString(mCursor.getColumnIndex(MedContract.Order.KEY_ORDERS_NOMBRE));
            String date = null;
            try {
                date = dateFormat.format(dbdateFormat.parse(mCursor.getString(mCursor.getColumnIndex(MedContract.Order.KEY_ORDERS_ULTIMAVENTA))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String count = String.valueOf(mCursor.getDouble(mCursor.getColumnIndex(MedContract.Order.KEY_ORDERS_UNIDADES)));

            holder.text.setText(text);
            holder.count.setText(count);
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
        public TextView count;
        public TextView date;
        public LinearLayout ll;
        public View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ll = (LinearLayout) itemView.findViewById(R.id.llOrderListItem);
            count = (TextView) itemView.findViewById(R.id.tvOrderListItemCount);
            date = (TextView) itemView.findViewById(R.id.tvOrderListitemDate);
            text = (TextView) itemView.findViewById(R.id.tvOrderListItemText);
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
