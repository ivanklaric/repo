package dev.klax.wikidata.importer;

import dev.klax.wikidata.importer.wdtkutils.SportsDataImporter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SportsDataImporterTest {

    @Test
    public void testSingleEntity() {
        var importer = new SportsDataImporter();
        var listOfSports = importer.getSportsEntitiesFromWikidataDump("src/test/resources/single_entity.json");
        assertThat(listOfSports).isNotNull();
        assertThat(listOfSports.size()).isEqualTo(1);
        var firstSport = listOfSports.getFirst();
        assertThat(firstSport).isNotNull();
        assertThat(firstSport.getName()).isEqualTo("judo");
    }

    @Test
    public void testMultipleEntities() {
        var importer = new SportsDataImporter();
        var listOfSports = importer.getSportsEntitiesFromWikidataDump("src/test/resources/ten_entities.json");
        assertThat(listOfSports).isNotNull();
        assertThat(listOfSports.size()).isEqualTo(4);
        var judo = listOfSports.getFirst();
        assertThat(judo).isNotNull();
        assertThat(judo.getName()).isEqualTo("judo");
        var waterPolo = listOfSports.getLast();
        assertThat(waterPolo).isNotNull();
        assertThat(waterPolo.getName()).isEqualTo("water polo");
    }
}
