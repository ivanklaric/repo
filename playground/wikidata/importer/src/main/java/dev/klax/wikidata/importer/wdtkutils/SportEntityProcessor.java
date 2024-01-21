package dev.klax.wikidata.importer.wdtkutils;

import dev.klax.sports.datamodel.Sport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.util.ArrayList;
import java.util.List;

public class SportEntityProcessor implements EntityDocumentProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SportEntityProcessor.class);
    private final StringBuilder entityProcessingLog = new StringBuilder();

    public List<Sport> getProcessedSports() {
        return processedSports;
    }

    private List<Sport> processedSports;
    static final Value typeOfSportEntity = Datamodel.makeWikidataItemIdValue("Q31629");
    static final PropertyIdValue instanceOfStatementId = Datamodel.makeWikidataPropertyIdValue("P31");

    @Override
    public void processItemDocument(ItemDocument itemDoc) {
        processedSports = new ArrayList<>();
        for (StatementGroup sg : itemDoc.getStatementGroups()) {
            if (sg.getProperty().getId().equals(instanceOfStatementId.getId()) ) {
                var isSport = statementGroupHasValue(sg, typeOfSportEntity);
                if (isSport) {
                    var sport = SportEntityFactory.buildSportFrom(itemDoc);
                    entityProcessingLog.append("Found sport: ").append(sport).append("\n");
                    logger.info("Found sport: " + sport.getName() + " " +sport.getIds().get("wikidata"));
                    processedSports.add(sport);
                }
            }
        }

    }

    private boolean statementGroupHasValue(StatementGroup sg, Value value) {
        for (Statement stmt : sg.getStatements()) {
            if (value.equals(stmt.getValue())) {
                return true;
            }
        }
        return false;
    }

    public String getOutputLog() {
        return entityProcessingLog.toString();
    }
}
