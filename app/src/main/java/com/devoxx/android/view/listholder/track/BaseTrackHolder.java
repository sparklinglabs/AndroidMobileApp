package com.devoxx.android.view.listholder.track;

import com.devoxx.connection.model.SlotApiModel;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class BaseTrackHolder extends RecyclerView.ViewHolder {

	public BaseTrackHolder(View itemView) {
		super(itemView);
	}

	public abstract void setupView(SlotApiModel slotApiModel, boolean runningItem, boolean isRunning);
}
