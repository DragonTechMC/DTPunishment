package me.morpheus.dtpunishment.configuration;

import java.util.ArrayList;
import java.util.List;

import com.google.common.reflect.TypeToken;
import com.google.inject.Singleton;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@Singleton
@ConfigSerializable
public class MainConfig {
	public static final TypeToken<MainConfig> TYPE = TypeToken.of(MainConfig.class);

	@Setting
	public Database database;

	@Setting(comment = "aliases for all DTPunishment commands")
	public Aliases aliases;

	@Setting(comment = "these settings describe which punishments users incur when reaching a certain level of banpoints/mutepoints")
	public Punishments punishments;

	public MainConfig() {
		database = new MainConfig.Database();
		punishments = new MainConfig.Punishments();
		aliases = new MainConfig.Aliases();
	}

	@ConfigSerializable
	public static class Database {

		@Setting(comment = "Use MySQL database store instead of config file based storage")
		public Boolean enabled = false;

		@Setting
		public String name;

		@Setting
		public String host;

		@Setting
		public int port;

		@Setting
		public String username;

		@Setting
		public String password;
	}

	@ConfigSerializable
	public static class Punishments {

		public static Punishment createPunishment(int threshold, String duration, int banpoints) {
			Punishment punishment = new Punishment();

			punishment.threshold = threshold;
			punishment.length = PunishmentLengthSerializer.getPunishmentLength(duration);
			punishment.banpoints = banpoints;
			return punishment;
		}

		public static Punishment createPunishment(int threshold, String duration) {
			return createPunishment(threshold, duration, 0);
		}

		public Punishment getApplicableBanpointsPunishment(int points) {
			Punishment punishment = null;

			for (Punishment potential : banpoints) {
				if (potential.threshold <= points)
					punishment = potential;
			}

			return punishment;
		}

		public Punishment getApplicableMutepointsPunishment(int points) {
			Punishment punishment = null;

			for (Punishment potential : mutepoints) {
				if (potential.threshold <= points)
					punishment = potential;
			}

			return punishment;
		}

		public Punishments() {
			banpoints = new ArrayList<Punishment>();
			banpoints.add(createPunishment(10, "1d"));
			banpoints.add(createPunishment(20, "2d"));
			banpoints.add(createPunishment(30, "3d"));
			banpoints.add(createPunishment(40, "4d"));
			banpoints.add(createPunishment(50, "5d"));
			banpoints.add(createPunishment(60, "6d"));
			banpoints.add(createPunishment(70, "7d"));
			banpoints.add(createPunishment(80, "14d"));
			banpoints.add(createPunishment(90, "28d"));
			banpoints.add(createPunishment(100, "168d"));

			mutepoints = new ArrayList<Punishment>();
			mutepoints.add(createPunishment(5, "5m"));
			mutepoints.add(createPunishment(10, "10m"));
			mutepoints.add(createPunishment(20, "30m"));
			mutepoints.add(createPunishment(30, "60m"));
			mutepoints.add(createPunishment(40, "60m", 1));
			mutepoints.add(createPunishment(50, "60m", 2));
			mutepoints.add(createPunishment(60, "60m", 3));
			mutepoints.add(createPunishment(70, "60m", 4));
			mutepoints.add(createPunishment(80, "60m", 5));
			mutepoints.add(createPunishment(90, "60m", 10));
			mutepoints.add(createPunishment(100, "60m", 20));
			mutepoints.add(createPunishment(110, "60m", 30));
			mutepoints.add(createPunishment(120, "60m", 50));
			mutepoints.add(createPunishment(130, "60m", 100));
		}

		@Setting(comment = "bans players when certain amounts of banpoints are incurred/added")
		public List<Punishment> banpoints;

		@Setting(comment = "mutes players when certain amounts of mutepoints are incurred/added")
		public List<Punishment> mutepoints;
	}

	@ConfigSerializable
	public static class Aliases {
		@Setting(comment = "aliases to use for the banpoints command  (default /bp or /banpoints)")
		public List<String> banpoints;

		@Setting(comment = "aliases to use for the mutepoints command (default /mp or /mutepoints)")
		public List<String> mutepoints;

		@Setting(comment = "aliases to use for the admin parent command (default /dtp or /dtpunish)")
		public List<String> admin;

		public Aliases() {
			banpoints = new ArrayList<String>();
			banpoints.add("banpoints");
			banpoints.add("bp");

			mutepoints = new ArrayList<String>();
			mutepoints.add("mutepoints");
			mutepoints.add("mp");

			admin = new ArrayList<String>();
			admin.add("dtp");
			admin.add("dtpunish");
		}
	}
}
