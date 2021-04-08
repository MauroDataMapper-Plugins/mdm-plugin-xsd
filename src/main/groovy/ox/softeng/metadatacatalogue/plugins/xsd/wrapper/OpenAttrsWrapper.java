package ox.softeng.metadatacatalogue.plugins.xsd.wrapper;

import ox.softeng.metadatacatalogue.core.catalogue.CatalogueItem;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Annotation;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Documentation;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.OpenAttrs;
import ox.softeng.metadatacatalogue.plugins.xsd.spi.XsdSchemaService;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.time.OffsetDateTime;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @since 24/08/2017
 */
public abstract class OpenAttrsWrapper<K extends OpenAttrs> implements Comparable<OpenAttrsWrapper> {

    public static final String XS_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    public static final String XS_PREFIX = "xs";

    protected K wrappedElement;
    String givenName;
    XsdSchemaService xsdSchemaService;

    OpenAttrsWrapper(XsdSchemaService xsdSchemaService, K wrappedElement) {
        this.xsdSchemaService = xsdSchemaService;
        this.wrappedElement = wrappedElement;
    }

    OpenAttrsWrapper(XsdSchemaService xsdSchemaService, K wrappedElement, String name) {
        this(xsdSchemaService, wrappedElement);
        this.givenName = name;
    }

    @Override
    public int compareTo(OpenAttrsWrapper that) {
        return this.getName().compareTo(that.getName());
    }

    public void debug(String msg, Object... args) {
        getLogger().debug("{} - " + msg, buildLoggingArgs(args));
    }

    public void error(String msg, Object... args) {
        getLogger().error("{} - " + msg, buildLoggingArgs(args));
    }

    public String getLoggingName() {
        return getName();
    }

    public abstract String getName();

    public K getWrappedElement() {
        return wrappedElement;
    }

    public void info(String msg, Object... args) {
        getLogger().info("{} - " + msg, buildLoggingArgs(args));
    }

    public String toString() {
        return getName();
    }

    public void trace(String msg, Object... args) {
        getLogger().trace("{} - " + msg, buildLoggingArgs(args));
    }

    public void warn(String msg, Object... args) {
        getLogger().warn("{} - " + msg, buildLoggingArgs(args));
    }

    boolean isElementWithName(String name, JAXBElement element) {
        return name.equalsIgnoreCase(element.getName().getLocalPart());
    }

    private Object[] buildLoggingArgs(Object... args) {
        Object[] arguments = new Object[args.length + 1];
        arguments[0] = getLoggingName();
        System.arraycopy(args, 0, arguments, 1, args.length);
        return arguments;
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    static Annotation createAnnotation(Object element) {
        if (element == null) return null;
        if (element instanceof String && Strings.isNullOrEmpty((String) element)) return null;
        Annotation annotation = new Annotation();
        Documentation documentation = new Documentation();
        documentation.getContent().add(element);
        annotation.getAppinfosAndDocumentations().add(documentation);

        return annotation;
    }

    @SafeVarargs
    static Annotation createAnnotationDocumentation(Pair<String, String>... dataPairs) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ignored) {
            return null;
        }

        Document doc = docBuilder.newDocument();

        org.w3c.dom.Element element = doc.createElement("p");
        for (Pair<String, String> pair : dataPairs) {
            if (!Strings.isNullOrEmpty(pair.getValue())) {
                Text text = doc.createTextNode(pair.getKey() + ": " + pair.getValue());
                element.appendChild(text);
                element.appendChild(doc.createElement("br"));
            }
        }

        return createAnnotation(element);
    }

    static String createComplexTypeName(CatalogueItem catalogueItem) {
        return SimpleTypeWrapper.PRIMITIVE_XML_TYPES.contains(catalogueItem.getLabel()) ? catalogueItem.getLabel() :
               createValidTypeName(catalogueItem.getLabel() + " Type", catalogueItem.getLastUpdated());
    }

    static String createSimpleTypeName(CatalogueItem catalogueItem) {
        return SimpleTypeWrapper.PRIMITIVE_XML_TYPES.contains(catalogueItem.getLabel()) ? catalogueItem.getLabel() :
               createValidTypeName(catalogueItem.getLabel(), catalogueItem.getLastUpdated());
    }

    static QName getTypeForName(String name) {
        return SimpleTypeWrapper.PRIMITIVE_XML_TYPES.contains(name) ?
               new QName(XS_NAMESPACE, name) :
               new QName(name);
    }

    static String createValidXsdName(String name) {
        String underscoreName = name.replaceAll("[ -]", "_");
        String camelName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, underscoreName);
        return camelName.matches("^\\d.*") ? "_" + camelName : camelName;
    }

    private static String createValidTypeName(String name, OffsetDateTime lastUpdated) {
        String validXsdName = createValidXsdName(name);
        return validXsdName + "-" + Math.abs(lastUpdated.hashCode());
    }
}
