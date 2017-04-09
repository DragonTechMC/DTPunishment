package me.morpheus.dtpunishment.configuration;

import java.time.Duration;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public class PunishmentLengthSerializer implements TypeSerializer<PunishmentLength> {

    @Override
    public PunishmentLength deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        String textValue = value.getString();

        // Parse the value
        return getPunishmentLength(textValue);
    }

    @Override
    public void serialize(TypeToken<?> type, PunishmentLength obj, ConfigurationNode value)
            throws ObjectMappingException {

        // Handle simple cases - since these return "longs"
        if (obj.duration.toDays() > 0) {
            value.setValue(obj.duration.toDays() + "d");
        } else if (obj.duration.toHours() > 0) {
            value.setValue(obj.duration.toDays() + "h");
        } else if (obj.duration.toMinutes() > 0) {
            value.setValue(obj.duration.toMinutes() + "m");
        } else {
            value.setValue("0d");
        }
    }

    public static PunishmentLength getPunishmentLength(String textValue) {
        PunishmentLength pl = new PunishmentLength();

        if (textValue.endsWith("d")) {
            long days = Long.parseLong(textValue.substring(0, textValue.length() - 1));
            pl.duration = Duration.ofDays(days);
        } else if (textValue.endsWith("h")) {
            long hours = Long.parseLong(textValue.substring(0, textValue.length() - 1));
            pl.duration = Duration.ofHours(hours);
        } else if (textValue.endsWith("m")) {
            long minutes = Long.parseLong(textValue.substring(0, textValue.length() - 1));
            pl.duration = Duration.ofMinutes(minutes);
        } else {
            // Default to days if no period suffix was specified
            long amount = Long.parseLong(textValue);
            pl.duration = Duration.ofDays(amount);
        }

        return pl;
    }
}
