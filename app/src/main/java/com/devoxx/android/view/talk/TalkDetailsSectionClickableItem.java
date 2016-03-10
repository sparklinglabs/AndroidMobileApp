package com.devoxx.android.view.talk;

import com.devoxx.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@EViewGroup(R.layout.talk_details_section_clickable_item)
public class TalkDetailsSectionClickableItem extends LinearLayout {

	@ViewById(R.id.talkDetailsSectionIcon)
	ImageView icon;

	@ViewById(R.id.talkDetailsSectionTitle)
	TextView title;

	@ViewById(R.id.talkDetailsSectionSpeakersContainer)
	LinearLayout speakers;

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		final int size = speakers.getChildCount();
		int maxChildWidth = Integer.MIN_VALUE;
		for (int i = 0; i < size; i++) {
			final View child = speakers.getChildAt(i);
			final int childWidth = child.getMeasuredWidth();
			if (childWidth > maxChildWidth) {
				maxChildWidth = childWidth;
			}
		}

		if (maxChildWidth < speakers.getMeasuredWidth()) {
			for (int i = 0; i < size; i++) {
				final View child = speakers.getChildAt(i);
				child.layout(child.getLeft(), child.getTop(),
						child.getLeft() + maxChildWidth, child.getBottom());
			}
		}
	}

	@AfterViews void afterViews() {
		setOrientation(HORIZONTAL);
	}

	public void setupView(@DrawableRes int iconRes, @StringRes int titleResId) {
		icon.setImageResource(iconRes);
		title.setText(titleResId);
	}

	public void addSpeakerView(View view) {
		speakers.addView(view);
	}

	public LinearLayout getSpeakersContainer() {
		return speakers;
	}

	public TalkDetailsSectionClickableItem(Context context) {
		super(context);
	}

	public TalkDetailsSectionClickableItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TalkDetailsSectionClickableItem(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TalkDetailsSectionClickableItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
}
