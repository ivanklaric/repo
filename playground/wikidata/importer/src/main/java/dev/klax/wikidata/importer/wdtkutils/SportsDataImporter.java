package dev.klax.wikidata.importer.wdtkutils;

import dev.klax.sports.datamodel.Sport;
import dev.klax.sports.datamodel.SportsClub;
import dev.klax.sports.repository.SportRepository;
import dev.klax.sports.repository.SportsClubRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;

import java.util.List;

public class SportsDataImporter {

    private static final Logger logger = LoggerFactory.getLogger(SportEntityProcessor.class);

    public int persistSportEntities(SportRepository sportsRepository, List<Sport> sportEntities) {
        if (sportEntities == null) {
            return -1;
        }
        for (var sport : sportEntities) {
            sportsRepository.save(sport);
            logger.info("Persisting " + sport);
        }
        return sportEntities.size();
    }

    public int persistSportsClubEntities(SportsClubRepository sportsClubRepo, List<SportsClub> sportsClubs) {
        if (sportsClubs == null) {
            return -1;
        }
        for (var club : sportsClubs) {
            sportsClubRepo.save(club);
            logger.info("Persisting " + club);
        }
        return sportsClubs.size();
    }


    public List<Sport> getSportsEntitiesFromWikidataDump(String filename) {
        var dumpProcessingController = new DumpProcessingController("wikidatawiki");
        var dumpFile = new MwLocalDumpFile(filename, DumpContentType.JSON, "latest", "wikidatawiki");
        var entityProcessor = new SportEntityProcessor();
        dumpProcessingController.registerEntityDocumentProcessor(entityProcessor, null, true);

        // TODO: fix this, should be configurable externally
        var entityTimerProcessor = new EntityTimerProcessor(100);
        dumpProcessingController.registerEntityDocumentProcessor(entityTimerProcessor, null, true);

        try {
            dumpProcessingController.processDump(dumpFile);
        } catch (EntityTimerProcessor.TimeoutException te) {
            entityTimerProcessor.close();
        }
        return entityProcessor.getProcessedSports();
    }
}
