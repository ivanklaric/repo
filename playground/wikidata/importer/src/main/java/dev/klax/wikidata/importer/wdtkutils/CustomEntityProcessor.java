package dev.klax.wikidata.importer.wdtkutils;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

public class CustomEntityProcessor implements EntityDocumentProcessor {

    private int itemDocsCount = 0;
    private int propertyDocsCount = 0;

    @Override
    public void processItemDocument(ItemDocument itemDoc) {
        itemDocsCount++;
    }

    @Override
    public void processPropertyDocument(PropertyDocument propDoc) {
        propertyDocsCount++;
    }

    public int getItemDocsCount() {
        return itemDocsCount;
    }

    public int getPropertyDocsCount() {
        return propertyDocsCount;
    }
}
