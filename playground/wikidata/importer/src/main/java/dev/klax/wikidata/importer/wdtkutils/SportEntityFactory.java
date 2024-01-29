package dev.klax.wikidata.importer.wdtkutils;

import dev.klax.sports.datamodel.Competitor;
import dev.klax.sports.datamodel.Sport;
import dev.klax.sports.datamodel.SportsClub;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;

import java.util.HashMap;
import java.util.UUID;

/*
    SportEntityFactory class is meant to produce Sport entities from the relevant ItemDocument WikiData entities.
 */
public class SportEntityFactory {
    public static final String englishLanguageCode = "en";
    public static final String wikidataEntityProviderName = "wikidata";

    public static Sport buildSportFrom(ItemDocument itemDoc) {
        var ret = new Sport();
        ret.setUuid(java.util.UUID.randomUUID());

        var labelsMap = itemDoc.getLabels();
        if (labelsMap != null && labelsMap.containsKey(englishLanguageCode)) {
            ret.setName(labelsMap.get(englishLanguageCode).getText());
        }
        if (labelsMap != null) {
            var i18Labels = new HashMap<String, String>();
            for (var langCode : labelsMap.keySet()) {
                i18Labels.put(langCode, labelsMap.get(langCode).getText());
            }
            ret.setI18names(i18Labels);
        }

        var descriptionsMap = itemDoc.getDescriptions();
        if (descriptionsMap != null && descriptionsMap.containsKey(englishLanguageCode)) {
            ret.setDescription(descriptionsMap.get(englishLanguageCode).getText());
        }

        var idsMap = new HashMap<String, String>();
        idsMap.put(wikidataEntityProviderName, itemDoc.getEntityId().getId());
        ret.setIds(idsMap);

        return ret;
    }

    public static SportsClub buildSportsClubFrom(ItemDocument itemDoc) {
        var ret = new SportsClub();
        ret.setUuid(java.util.UUID.randomUUID());

        var labelsMap = itemDoc.getLabels();
        if (labelsMap != null && labelsMap.containsKey(englishLanguageCode)) {
            ret.setName(labelsMap.get(englishLanguageCode).getText());
        }

        var descriptionsMap = itemDoc.getDescriptions();
        if (descriptionsMap != null && descriptionsMap.containsKey(englishLanguageCode)) {
            ret.setDescription(descriptionsMap.get(englishLanguageCode).getText());
        }

        return ret;
    }

    public static Competitor buildCompetitor() {
        return Competitor.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setName("Foo")
                .setDescription("Bar")
                .build();
    }
}
