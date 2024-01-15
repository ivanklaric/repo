package dev.klax.wikidata.importer.commands;

import dev.klax.wikidata.importer.wdtkutils.CustomEntityProcessor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;




@ShellComponent
public class WikiDataImporter {


    @ShellMethod(key = "import", value="processes the wikidata dump and imports it to the database")
    public String importWikidata(String filename) {
        var dumpProcessingController = new DumpProcessingController("wikidatawiki");
        var dumpFile = new MwLocalDumpFile(filename, DumpContentType.JSON, "latest", "wikidatawiki");
        var entityProcessor = new CustomEntityProcessor();
        dumpProcessingController.registerEntityDocumentProcessor(entityProcessor, null, true);

        var entityTimerProcessor = new EntityTimerProcessor(20);
        dumpProcessingController.registerEntityDocumentProcessor(entityTimerProcessor, null, true);

        try {
            dumpProcessingController.processDump(dumpFile);
        } catch (EntityTimerProcessor.TimeoutException te) {
            entityTimerProcessor.close();
        }

        return "Processed " + entityProcessor.getItemDocsCount() +" items and "
                + entityProcessor.getPropertyDocsCount() + " properties.";
    }
}