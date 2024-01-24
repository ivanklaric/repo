package dev.klax.sports.datamodel;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;
import java.util.Map;

/*
  Class sport represents the main sport entity.
 */
@Entity
public class Sport {
    @Id
    private UUID uuid;
    private String name;
    private String description;

    @ElementCollection
    @Column(columnDefinition = "json")
    private Map<String, String> i18names;

    @ElementCollection
    @Column(columnDefinition = "json")

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
