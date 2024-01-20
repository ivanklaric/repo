package dev.klax.wikidata.importer.wdtkutils;

import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.*;

public class SportEntityProcessor implements EntityDocumentProcessor {
    private final StringBuilder entityProcessingLog = new StringBuilder();

    static final Value typeOfSportEntity = Datamodel.makeWikidataItemIdValue("Q31629");
    static final PropertyIdValue instanceOfStatementId = Datamodel.makeWikidataPropertyIdValue("P31");

    @Override
    public void processItemDocument(ItemDocument itemDoc) {
        for (StatementGroup sg : itemDoc.getStatementGroups()) {
            if (sg.getProperty().getId().equals(instanceOfStatementId.getId()) ) {
                var isSport = statementGroupHasValue(sg, typeOfSportEntity);
                if (isSport) {
                    var sport = SportEntityFactory.buildSportFrom(itemDoc);
                    entityProcessingLog.append("Found sport: ").append(sport);
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
