package ox.softeng.metadatacatalogue.plugins.xsd.wrapper;

import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Appinfo;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Documentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @since 25/08/2017
 */
class AnnotationContentWrapper {

    static final String DOCUMENTATION_CONTENT = "documentation";
    static final String APPINFO_CONTENT = "appinfo";

    private static final Logger logger = LoggerFactory.getLogger(AnnotationContentWrapper.class);

    private final Object annotationContent;

    AnnotationContentWrapper(Object annotationContent) {
        this.annotationContent = annotationContent;
    }

    String getContentType() {
        if (annotationContent instanceof Appinfo) {
            return APPINFO_CONTENT;
        }
        if (annotationContent instanceof Documentation) {
            return DOCUMENTATION_CONTENT;
        }
        logger.warn("Unknown annotation content type inside wrapper {}", annotationContent.getClass().getCanonicalName());
        return null;
    }

    List<Object> getContents() {
        if (annotationContent instanceof Appinfo) {
            return ((Appinfo) annotationContent).getContent();
        }
        if (annotationContent instanceof Documentation) {
            return ((Documentation) annotationContent).getContent();
        }
        logger.warn("Unknown annotation content type inside wrapper {}", annotationContent.getClass().getCanonicalName());
        return Collections.emptyList();
    }
}
