package dev.klax.wikidata.importer.commands;

import dev.klax.sports.datamodel.Sport;
import dev.klax.wikidata.importer.wdtkutils.SportEntityProcessor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;

import java.util.List;


@ShellComponent
public class WikiDataImporter {

    private void persistSportEntities(List<Sport> sportEntities) {
        for (var sport : sportEntities) {
            System.out.println("Persisting " + sport);
        }
    }

    @ShellMethod(key = "import", value="processes the wikidata dump and imports it to the database")
    public String importWikidata(String filename) {
        if (filename== null || filename.isEmpty()) {
            return "Needs filename parameter";
        }
        //TODO: add file exists check

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
        }
        persistSportEntities(entityProcessor.getProcessedSports());

        return entityProcessor.getOutputLog();
    }
}
