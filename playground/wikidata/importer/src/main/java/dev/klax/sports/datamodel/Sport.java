package dev.klax.sports.datamodel;

import java.util.UUID;
import java.util.Map;

/*
  Class sport is meant to represent the sport entity.
 */
public class Sport {
    private UUID uuid;
    private String name;
    private String description;
    private Map<String, String> i18names;
    private Map<String, String> ids;

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getI18names() {
        return i18names;
    }

    public void setI18names(Map<String, String> i18names) {
        this.i18names = i18names;
    }

    public Map<String, String> getIds() {
        return ids;
    }

    public void setIds(Map<String, String> ids) {
        this.ids = ids;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Sport{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", ids=" + ids +
                '}';
    }
}
