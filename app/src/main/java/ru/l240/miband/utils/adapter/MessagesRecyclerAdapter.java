package ru.l240.miband.utils.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import ru.fors.remsmed.R;
import ru.fors.remsmed.core.MedContract;

/**
 * @author Alexander Popov created on 14.08.2015.
 */
public class MessagesRecyclerAdapter extends RecyclerView.Adapter<MessagesRecyclerAdapter.ViewHolder> {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Cursor mCursor;
    private final Context mContext;

    public MessagesRecyclerAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.messages_listitem, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            boolean pReceiver = mCursor.getInt(mCursor.getColumnIndex(MedContract.Messages.KEY_MESSAGE_PATIENT_RECEIVER)) == 1;
            String text = mCursor.getString(mCursor.getColumnIndex(MedContract.Messages.KEY_MESSAGE_TEXT));
            String sysInfo = mCursor.getString(mCursor.getColumnIndex(MedContract.Messages.KEY_MESSAGE_SYSINFO));
            if (!pReceiver) {
                holder.text.setTextColor(mContext.getResources().getColor(R.color.main_font_color));
                if (sysInfo == null)
                    holder.ll.setBackgroundResource(R.drawable.textview_rounded_borders_chat_me);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.RIGHT;
                holder.ll.setLayoutParams(params);
            } else {
                holder.text.setTextColor(mContext.getResources().getColor(R.color.system_font));
                holder.ll.setBackgroundResource(R.drawable.textview_rounded_borders_chat_contact);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.LEFT;
                holder.ll.setLayoutParams(params);
            }
            if (sysInfo != null) {
                holder.text.setVisibility(View.GONE);
                holder.rivVideoCall.setVisibility(View.VISIBLE);
            } else {
                holder.text.setVisibility(View.VISIBLE);
                holder.rivVideoCall.setVisibility(View.GONE);
            }
            holder.text.setText(text);
            try {
                holder.date.setText(new SimpleDateFormat("dd/MM/yyyy HH:ss").format(dateFormat.parse(mCursor.getString(mCursor.getColumnIndex(MedContract.Messages.KEY_MESSAGE_DATE)))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
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
        public LinearLayout ll;
        public ImageView rivVideoCall;

        public ViewHolder(View itemView) {
            super(itemView);
            rivVideoCall = (ImageView) itemView.findViewById(R.id.rivMessageListitem);
            ll = (LinearLayout) itemView.findViewById(R.id.llMessageListItem);
            text = (TextView) itemView.findViewById(R.id.tvMessageListItemText);
            date = (TextView) itemView.findViewById(R.id.tvMessageListitemDate);
        }
    }
}
