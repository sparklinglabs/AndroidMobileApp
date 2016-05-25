package com.devoxx.data.manager;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.devoxx.connection.model.SlotApiModel;
import com.devoxx.connection.model.TalkSpeakerApiModel;
import com.devoxx.data.dao.SlotDao;
import com.devoxx.data.downloader.SlotsDownloader;
import com.devoxx.data.model.RealmSpeakerShort;
import com.devoxx.utils.Logger;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class SlotsDataManager extends AbstractDataManager<SlotApiModel> {

	@Bean
	SlotsDownloader slotsDownloader;

	@Bean
	SlotDao slotDao;

	private List<SlotApiModel> allSlots = new ArrayList<>();
	private List<SlotApiModel> talks = new ArrayList<>();

	@AfterInject void afterInject() {
		allSlots.clear();
		allSlots.addAll(slotDao.getAllSlots());

		talks.clear();
		talks.addAll(Stream.of(allSlots)
				.filter(value -> value.isTalk() && !value.isBreak())
				.filter(value1 -> !value1.notAllocated)
				.collect(Collectors.<SlotApiModel>toList()));
	}

	public Optional<SlotApiModel> getSlotByTalkId(final String talkId) {
		return Stream.of(allSlots).filter(value -> value.isTalk() && !value.isBreak()
				&& value.talk.id.equals(talkId)).findFirst();
	}

	public List<SlotApiModel> getLastTalks() {
		return talks;
	}

	public boolean fetchTalksSync(final String confCode) throws IOException {
		allSlots.clear();
		allSlots = slotsDownloader.downloadTalks(confCode);
		slotDao.saveSlots(allSlots);

		final List<SlotApiModel> talks = Stream.of(allSlots)
				.filter(value -> value.talk != null && !value.isBreak())
				.collect(Collectors.<SlotApiModel>toList());
		this.talks.clear();
		this.talks.addAll(talks);

		return !Stream.of(allSlots).filter(SlotApiModel::isTalk)
				.collect(Collectors.toList()).isEmpty();
	}

	public List<SlotApiModel> getSlotsForDay(final long timeMs) {
		final DateTime requestedDate = new DateTime(timeMs);
		final DateTime tmpDate = new DateTime();
		final DateTimeComparator dateComparator = DateTimeComparator.getDateOnlyInstance();

		return Stream.of(allSlots)
				.filter(value -> dateComparator.compare(requestedDate, tmpDate.withMillis(value.fromTimeMillis)) == 0)
				.filter(value1 -> !value1.notAllocated)
				.collect(Collectors.<SlotApiModel>toList());
	}

	@Override
	public void clearData() {
		allSlots.clear();
		talks.clear();
		slotDao.clearData();
	}

	@Background void resyncDataInBackground(String confCode) {
		try {
			slotsDownloader.downloadTalks(confCode);
		} catch (IOException e) {
			Logger.exc(e);
		}
	}

	private boolean isDataUpdateNeeded(String confCode) {
		return slotsDownloader.isDataUpdateNeeded(confCode);
	}

	public void updateSlotsIfNeededInBackground(String confCode) {
		if (isDataUpdateNeeded(confCode)) {
			resyncDataInBackground(confCode);
		}
	}

	public void updateSlotsBySpeakerImages(List<RealmSpeakerShort> speakers) {
		final HashMap<String, String> uuidToSpeakerImage = new HashMap<>(speakers.size());
		for (RealmSpeakerShort speaker : speakers) {
			uuidToSpeakerImage.put(speaker.getUuid(), speaker.getAvatarURL());
		}

		for (SlotApiModel slot : allSlots) {
			if (slot.talk != null && !slot.isBreak()) {
				final List<TalkSpeakerApiModel> talkSpeakers = slot.talk.speakers;
				if (talkSpeakers != null && !talkSpeakers.isEmpty()) {
					final TalkSpeakerApiModel firstSpeaker = talkSpeakers.get(0);
					final String talkSpeakerUuid = TalkSpeakerApiModel.getUuidFromLink(firstSpeaker.link);

					final String avatarURL = uuidToSpeakerImage.get(talkSpeakerUuid);
					slot.setSpeakerImageUrl(avatarURL);
				}
			}
		}

		slotDao.saveSlots(allSlots);
	}
}
