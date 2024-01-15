package dev.klax.wikidata.importer.wdtkutils;

import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.*;

public class CustomEntityProcessor implements EntityDocumentProcessor {

    static final Value sportFilterValue = Datamodel.makeWikidataItemIdValue("Q31629");

    @Override
    public void processItemDocument(ItemDocument itemDoc) {
        for (StatementGroup sg : itemDoc.getStatementGroups()) {
            if (sg.getProperty().getId().equals("P31")) { // instanceof
                var isSport = statementGroupHasValue(sg, sportFilterValue);
                if (isSport) {
                    System.out.println("Found sport: " + itemDoc.getEntityId());
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
}
