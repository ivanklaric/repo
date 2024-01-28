package dev.klax.wikidata.importer.commands;

import dev.klax.sports.repository.SportRepository;
import dev.klax.wikidata.importer.wdtkutils.SportsDataImporter;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;


@ShellComponent
public class WikiDataImporter {
    private final SportRepository sportRepository;

    public WikiDataImporter(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    @ShellMethod(key = "import", value="processes the wikidata dump and imports it to the database")
    public String importWikidata(String filename) {
        if (filename== null || filename.isEmpty()) {
            return "Needs filename parameter.";
        }
        File f = new File(filename);
        if (!f.exists() || f.isDirectory()) {
            return filename + " is not a valid filename.";
        }

        var importer = new SportsDataImporter();
        var listOfSports = importer.getSportsEntitiesFromWikidataDump(filename);
        if (listOfSports == null || listOfSports.isEmpty()) {
            return "No sports found in the dump!";
        }
        int persistedSports = importer.persistSportEntities(sportRepository, listOfSports);
        if (persistedSports <= 0 || persistedSports != listOfSports.size()) {
            return "Found " + listOfSports.size() +" sports but persisted " + persistedSports;
        }
        return "Found and persisted " + persistedSports + " sports";
    }
}
