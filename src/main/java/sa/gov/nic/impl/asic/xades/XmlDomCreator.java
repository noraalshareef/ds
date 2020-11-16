// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades;

import org.w3c.dom.DOMImplementation;
import javax.xml.parsers.ParserConfigurationException;
import sa.gov.nic.exceptions.TechnicalException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;

public class XmlDomCreator
{
    public static final String ASICS_NS = "asic:XAdESSignatures";
    private static DocumentBuilderFactory documentBuilderFactory;
    
    public static Document createDocument(final String namespaceURI, final String qualifiedName, final Element element) {
        ensureDocumentBuilder();
        try {
            final DOMImplementation domImpl = XmlDomCreator.documentBuilderFactory.newDocumentBuilder().getDOMImplementation();
            final Document newDocument = domImpl.createDocument(namespaceURI, qualifiedName, null);
            final Element newElement = newDocument.getDocumentElement();
            newDocument.adoptNode(element);
            newElement.appendChild(element);
            return newDocument;
        }
        catch (ParserConfigurationException e) {
            throw new TechnicalException("Failed to initialize DOM document builder factory", e);
        }
    }
    
    private static void ensureDocumentBuilder() {
        if (XmlDomCreator.documentBuilderFactory == null) {
            initializeDocumentBuilderFactory();
        }
    }
    
    private static synchronized void initializeDocumentBuilderFactory() {
        if (XmlDomCreator.documentBuilderFactory == null) {
            (XmlDomCreator.documentBuilderFactory = DocumentBuilderFactory.newInstance()).setNamespaceAware(true);
            try {
                XmlDomCreator.documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                XmlDomCreator.documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                XmlDomCreator.documentBuilderFactory.setXIncludeAware(false);
                XmlDomCreator.documentBuilderFactory.setExpandEntityReferences(false);
            }
            catch (ParserConfigurationException e) {
                throw new TechnicalException("Failed to initialize DOM document builder factory", e);
            }
        }
    }
}
