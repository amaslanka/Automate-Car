/**
 * Copyright 2014 Magnus Woxblom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.maslanka.automatecar.prefconnected.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.prefconnected.AppsToLaunch;
import pl.maslanka.automatecar.helpers.QuattroObject;

public class ItemAdapter extends
        DragItemAdapter<QuattroObject<Long, String, String, Drawable>, ItemAdapter.ViewHolder> {

    private Activity mActivity;
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    public ItemAdapter(Activity activity, ArrayList<QuattroObject<Long, String, String, Drawable>> list,
                       int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mActivity = activity;
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String text = mItemList.get(position).getName();
        Drawable icon = mItemList.get(position).getDrawable();
        holder.mText.setText(text);
        holder.itemView.setTag(position);
        holder.mImage.setImageDrawable(icon);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).getIndex();
    }

    public class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView mText;
        public ImageView mImage;


        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText = (TextView) itemView.findViewById(R.id.text);
            mImage = (ImageView) itemView.findViewById(R.id.image);
        }

        @Override
        public void onItemClicked(View view) {
            Toast.makeText(view.getContext(), mActivity.getString(R.string.drag_apps_to_change_order), Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {
            AlertDialog deleteConfirmDialog = new AlertDialog.Builder(view.getContext())
                    .setTitle(mActivity.getString(R.string.delete_app_question))
                    .setMessage(mActivity.getString(R.string.sure_about_deleting_app_question))
                    .setIcon(R.drawable.ic_delete_forever_black)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = getAdapterPosition();
                            ((AppsToLaunch) mActivity).deleteApp(position);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            if (deleteConfirmDialog.getWindow() != null)
                deleteConfirmDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

            deleteConfirmDialog.show();
            return true;
        }
    }
}
