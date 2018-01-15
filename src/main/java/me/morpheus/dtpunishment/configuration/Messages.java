package me.morpheus.dtpunishment.configuration;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Frani on 14/01/2018.
 */
@ConfigSerializable
public class Messages {

    @Setting
    public String PREFIX = "[DTP] ";

    @Setting
    public String USERNAME_HAS_BANNED_WORDS = "You cannot join the server because your username contains a banned word (%s)";

    @Setting
    public String PLAYER_MUTED_MESSAGE = "You have been muted until %s due to violating chat policy. If you believe this is an error, contact a staff member (your mutepoints: %s)";

    @Setting
    public String PLAYER_MUTED_MESSAGE_STAFF = "%s has been muted for %s for exceeding %d mutepoint(s)";

    @Setting
    public String PLAYER_SPAMMING_MESSAGE = "You are spamming chat, %s mutepoint(s) have been added automatically, you now have %s. If you believe this is an error, contact a staff member.";

    @Setting
    public String PLAYER_SPAMMING_CHARACTERS_MESSAGE = "You are spamming characters in chat, %s mutepoint(s) have been added automatically, you now have %s. If you believe this is an error, contact a staff member.";

    @Setting
    public String PLAYER_SPAMMING_CHARACTERS_STAFF = "%s has spammed characters; %d mutepoint(s) have been added automatically, they now have %d";

    @Setting
    public String PLAYER_SAID_BANNED_WORD = "You said a banned word '%s'; %d mutepoint(s) have been added automatically, you now have %d. If you believe this is an error, contact a staff member.";

    @Setting
    public String PLAYER_SAID_BANNED_WORD_STAFF = "%s said a banned word '%s'; %d mutepoint(s) have been added automatically, they now have %d";

    @Setting
    public String PLAYER_EXCEEDED_MAX_CAPS = "You have exceeded the max percentage of caps allowed; %s mutepoint(s) have been added automatically, you now have %s. If you believe this is an error, contact a staff member.";

    @Setting
    public String PLAYER_EXCEEDED_MAX_CAPS_STAFF = "%s has exceeded the max percentage of caps allowed; %s mutepoint(s) have been added automatically, they now have %s";

    @Setting
    public String PLAYER_EXCEEDED_POINTS = "You have been muted for %s for exceeding %d points";

    @Setting
    public String PLAYER_BANNED_EXCEEDED_POINTS = "You have been banned for %s because you exceeded %d points";

    @Setting
    public String PLAYER_BANNED_EXCEEDED_POINTS_STAFF = "%s has been banned for %s for exceeding %d banpoint(s)";

}
