package dev.klax.wikidata.importer.wdtkutils;

import dev.klax.sports.datamodel.Sport;
import dev.klax.sports.repository.SportRepository;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;

import java.util.List;

public class SportsDataImporter {

    public int persistSportEntities(SportRepository sportsRepository, List<Sport> sportEntities) {
        if (sportEntities == null) {
            return -1;
        }
        for (var sport : sportEntities) {
            sportsRepository.save(sport);
            System.out.println("Persisting " + sport);
            // TODO: replace with logger
        }
        return sportEntities.size();
    }


    public List<Sport> getSportsEntitiesFromWikidataDump(String filename) {
        var dumpProcessingController = new DumpProcessingController("wikidatawiki");
        var dumpFile = new MwLocalDumpFile(filename, DumpContentType.JSON, "latest", "wikidatawiki");
        var entityProcessor = new SportEntityProcessor();
        dumpProcessingController.registerEntityDocumentProcessor(entityProcessor, null, true);

        var entityTimerProcessor = new EntityTimerProcessor(100);
        dumpProcessingController.registerEntityDocumentProcessor(entityTimerProcessor, null, true);

        try {
            dumpProcessingController.processDump(dumpFile);
        } catch (EntityTimerProcessor.TimeoutException te) {
            entityTimerProcessor.close();
            return null;
        }
        return entityProcessor.getProcessedSports();
    }
}
