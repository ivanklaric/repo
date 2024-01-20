package dev.klax.wikidata.importer.wdtkutils;

import dev.klax.sports.datamodel.Sport;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;

import java.util.HashMap;

/*
    SportEntityFactory class is meant to produce Sport entities from the relevant ItemDocument WikiData entities.
 */
public class SportEntityFactory {
    public static String englishLanguageCode = "en";
    public static String wikidataEntityProviderName = "wikidata";

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
}
