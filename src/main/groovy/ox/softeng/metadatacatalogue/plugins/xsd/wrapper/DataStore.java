package ox.softeng.metadatacatalogue.plugins.xsd.wrapper;

import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.DataClass;
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.DataType;
import ox.softeng.metadatacatalogue.core.facet.linkable.SemanticLink;
import ox.softeng.metadatacatalogue.core.facet.linkable.SemanticLinkService;
import ox.softeng.metadatacatalogue.core.type.facet.SemanticLinkType;
import ox.softeng.metadatacatalogue.core.user.CatalogueUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @since 04/09/2017
 */
class DataStore {

    private static final Logger logger = LoggerFactory.getLogger(DataStore.class);
    private final boolean createLinksRatherThanReferences;
    private final CatalogueUser user;
    private Map<String, Set<DataClass>> allDataClasses;
    private Map<String, DataClass> dataClasses;
    private Map<String, DataType> dataTypes;
    private SemanticLinkService semanticLinkService;
    private List<String> underConstruction;

    DataStore(CatalogueUser user, SemanticLinkService semanticLinkService, boolean createLinksRatherThanReferences) {
        dataClasses = new HashMap<>();
        allDataClasses = new HashMap<>();
        dataTypes = new HashMap<>();
        underConstruction = new ArrayList<>();
        this.user = user;
        this.createLinksRatherThanReferences = createLinksRatherThanReferences;
        this.semanticLinkService = semanticLinkService;
    }

    boolean addToConstruction(String s) {
        return !underConstruction.contains(s) && underConstruction.add(s);
    }

    DataType computeIfDataTypeAbsent(String key,
                                     Function<? super String, ? extends DataType> mappingFunction) {
        return dataTypes.computeIfAbsent(key, mappingFunction);
    }

    DataClass getDataClass(String key) {
        return dataClasses.get(key);
    }

    DataType getDataType(String key) {
        return dataTypes.get(key);
    }

    boolean isCreateLinksRatherThanReferences() {
        return createLinksRatherThanReferences;
    }

    void putAllDataTypes(Map<? extends String, ? extends DataType> m) {
        dataTypes.putAll(m);
    }

    void putDataClass(String key, DataClass value) {
        allDataClasses.compute(key, (type, existing) -> {
            if (existing == null) existing = new HashSet<>();

            else if (createLinksRatherThanReferences && !existing.isEmpty()) {
                logger.debug("Adding links for {} existing data classes with type {}", existing.size(), key);
                existing.forEach(dc -> {

                    SemanticLink linkTarget = semanticLinkService.createSemanticLink(user, value, dc, SemanticLinkType.REFINES);
                    SemanticLink linkSource = semanticLinkService.createSemanticLink(user, dc, value, SemanticLinkType.REFINES);

                    dc.addTo("sourceForLinks", linkSource);
                    value.addTo("targetForLinks", linkSource);

                    dc.addTo("targetForLinks", linkTarget);
                    value.addTo("sourceForLinks", linkTarget);
                });
            }

            existing.add(value);
            return existing;
        });
        dataClasses.put(key, value);
    }

    DataType putDataType(String key, DataType value) {
        return dataTypes.put(key, value);
    }

    void removeFromConstruction(String s) {
        String last = underConstruction.get(underConstruction.size() - 1);
        if (!s.equals(last)) logger.warn("Trying to remove {} from under construction but it isnt the most recent item, that is {}", s, last);
        underConstruction.remove(s);
    }
}
