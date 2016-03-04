package ru.l240.miband.utils.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import ru.fors.remsmed.MainActivity;
import ru.fors.remsmed.R;
import ru.fors.remsmed.core.MedContract;
import ru.fors.remsmed.fragments.ChoiseChatFragment;
import ru.fors.remsmed.fragments.MessagesFragment;
import ru.fors.remsmed.utils.ProfileUtils;

/**
 * @author Alexander Popov created on 13.08.2015.
 */
public class ContactRecyclerAdapter extends RecyclerView.Adapter<ContactRecyclerAdapter.ViewHolder> {

    private Cursor mCursor;
    private final Context mContext;

    public ContactRecyclerAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.contact_listitem, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            final String fio = mCursor.getString(mCursor.getColumnIndex(MedContract.Contacts.KEY_CONTACT_USER_FIO));
            Bitmap icon = null;
            final long id = mCursor.getLong(mCursor.getColumnIndex(MedContract.Contacts._ID));
            byte[] image = mCursor.getBlob(mCursor.getColumnIndex(MedContract.Contacts.KEY_CONTACT_USER_IMG));
            if (image != null) {
                InputStream is = new ByteArrayInputStream(image);
                icon = BitmapFactory.decodeStream(is);

            }
            Integer countNewMessages = mCursor.getInt(mCursor.getColumnIndex(MedContract.Contacts.KEY_CONTACT_COUNT_MSG__NEW));

            holder.tvContactListItemFIO.setText(fio);
            holder.tvContactListBadge.setText(countNewMessages.toString());
            if (countNewMessages > 0) {
                holder.tvContactListBadge.setVisibility(View.VISIBLE);
            } else {
                holder.tvContactListBadge.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong("contact_id", id);
                ChoiseChatFragment choiseChatFragment = new ChoiseChatFragment();
                choiseChatFragment.setArguments(args);
                android.support.v4.app.FragmentTransaction frTr = ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction();
                try {
                    frTr.add(R.id.container, choiseChatFragment, ChoiseChatFragment.TAG)
                            .addToBackStack(ChoiseChatFragment.TAG)
                            .commit();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            });

            if (icon != null) {
                holder.ivContactListItem.setImageBitmap(ProfileUtils.getRoundedImage((mContext), icon, 0));
            }

            holder.ivContactLIMessage.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong("contact_id", id);
                args.putBoolean("is_video", false);
                MessagesFragment messagesFragment = new MessagesFragment();
                messagesFragment.setArguments(args);
                View abTitle = ((MainActivity) mContext).getAbTitle();
                ((TextView) abTitle.findViewById(R.id.textViewTitle)).setText(fio);
                android.support.v4.app.FragmentTransaction frTr = ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction();
                try {
                    frTr.add(R.id.container, messagesFragment, MessagesFragment.TAG)
                            .addToBackStack(MessagesFragment.TAG)
                            .commit();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            });

            holder.ivContactLIVideo.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong("contact_id", id);
                args.putBoolean("is_video", true);
                MessagesFragment messagesFragment = new MessagesFragment();
                messagesFragment.setArguments(args);
                View abTitle = ((MainActivity) mContext).getAbTitle();
                ((TextView) abTitle.findViewById(R.id.textViewTitle)).setText(fio);
                android.support.v4.app.FragmentTransaction frTr = ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction();
                try {
                    frTr.add(R.id.container, messagesFragment, MessagesFragment.TAG)
                            .addToBackStack(MessagesFragment.TAG)
                            .commit();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            });
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvContactListBadge;
        public TextView tvContactListItemFIO;
        public ImageView ivContactListItem;
        public ImageView ivContactLIMessage;
        public ImageView ivContactLIVideo;
        public FrameLayout flContactListItem;
        public View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvContactListBadge = (TextView) itemView.findViewById(R.id.tvContactListBadge);
            tvContactListItemFIO = (TextView) itemView.findViewById(R.id.tvContactListItemFIO);
            ivContactListItem = (ImageView) itemView.findViewById(R.id.ivContactListItem);
            ivContactLIMessage = (ImageView) itemView.findViewById(R.id.ivContactLIMessage);
            ivContactLIVideo = (ImageView) itemView.findViewById(R.id.ivContactLIVideo);
            flContactListItem = (FrameLayout) itemView.findViewById(R.id.flContactListItem);
        }
    }
}
