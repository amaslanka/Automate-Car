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

package pl.maslanka.automatecar.connectedpref.adapters;

import android.app.Activity;
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
import pl.maslanka.automatecar.helperobjectsandinterfaces.Triplet;

public class ItemAdapter extends DragItemAdapter<Triplet<Long, String, Drawable>, ItemAdapter.ViewHolder> {

    private Activity mActivity;
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;

    public ItemAdapter(Activity activity, ArrayList<Triplet<Long, String, Drawable>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
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
        String text = mItemList.get(position).getSecond();
        Drawable icon = mItemList.get(position).getThird();
        holder.mText.setText(text);
        holder.itemView.setTag(text);
        holder.mImage.setImageDrawable(icon);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).getFirst();
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
            return true;
        }
    }
}
