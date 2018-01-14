package me.morpheus.dtpunishment.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.inject.Singleton;

public class ChatOffenceData {

	private Map<UUID, OffenceDetail> lastOffence;

	public ChatOffenceData() {
		lastOffence = new HashMap<UUID, OffenceDetail>();
	}

	public OffenceDetail getLastOffence(UUID uuid) {
		return lastOffence.get(uuid);
	}

	public void trackLastOffence(UUID uuid, String offenceText, String offenceType, int points) {
		this.lastOffence.put(uuid, new OffenceDetail(offenceText, offenceType, points));
	}

	public static class OffenceDetail {
		private String message;

		private String offenceType;

		private int pointsIncurred;

		public OffenceDetail(String message, String offenceType, int pointsIncurred) {
			this.message = message;
			this.offenceType = offenceType;
			this.pointsIncurred = pointsIncurred;
		}

		public String getMessage() {
			return this.message;
		}

		public String getOffenceType() {
			return this.offenceType;
		}

		public int getPointsIncurred() {
			return pointsIncurred;
		}
	}
}
