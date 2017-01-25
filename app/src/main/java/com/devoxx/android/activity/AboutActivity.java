package com.devoxx.android.activity;


import com.annimon.stream.Optional;
import com.devoxx.R;
import com.devoxx.data.conference.ConferenceManager;
import com.devoxx.data.model.RealmConference;
import com.devoxx.navigation.Navigator;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.widget.TextView;

@EActivity(R.layout.activity_about)
public class AboutActivity extends BaseActivity {

	@Bean
	ConferenceManager conferenceManager;

	@Bean
	Navigator navigator;

	@ViewById(R.id.main_toolbar)
	Toolbar toolbar;

	@ViewById(R.id.main_appbar)
	AppBarLayout appBarLayout;

	@ViewById(R.id.main_collapsing)
	CollapsingToolbarLayout collapsingToolbarLayout;

	@ViewById(R.id.aboutWebButton)
	FloatingActionButton firstButton;

	@ViewById(R.id.aboutTwitterButton)
	FloatingActionButton secondButton;

	@ViewById(R.id.aboutDescription)
	TextView description;

	@ViewById(R.id.aboutLink)
	TextView linkView;

	@AfterViews void afterViews() {
		linkView.setText(conferenceManager.getActiveConference().get().getWwwURL());
		Linkify.addLinks(linkView, Linkify.WEB_URLS);

		collapsingToolbarLayout.setTitle(getString(R.string.about));
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		final Optional<RealmConference> optConf = conferenceManager.getActiveConference();
		if (optConf.isPresent()) {
			description.setText(optConf.get().getDescription());
		}
	}

	@OptionsItem(android.R.id.home) void onBackClick() {
		finish();
	}

	@Click(R.id.aboutWebButton) void onWebButtonClick() {
		final Optional<RealmConference> optConf = conferenceManager.getActiveConference();
		if (optConf.isPresent()) {
			final String www = optConf.get().getWwwURL();
			navigator.openWwwLink(this, www);
		}
	}

	@Click(R.id.aboutTwitterButton) void onTwitterButtonClick() {
		final Optional<RealmConference> optConf = conferenceManager.getActiveConference();
		if (optConf.isPresent()) {
			final RealmConference conference = optConf.get();
			final String message = String.format(getString(R.string.twitter_devoxx_message),
					conference.getCountry(),
					conference.getWwwURL());
			navigator.tweetMessage(this, message);
		}
	}
}
