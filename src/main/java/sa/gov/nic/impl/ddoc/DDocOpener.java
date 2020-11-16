// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.ddoc;

import org.apache.commons.lang3.StringUtils;
import ee.sk.digidoc.factory.SAXDigiDocFactory;
import ee.sk.digidoc.factory.DigiDocFactory;
import sa.gov.nic.exceptions.DigiDoc4JException;
import java.util.List;
import java.io.InputStream;
import ee.sk.digidoc.SignedDoc;
import ee.sk.digidoc.DigiDocException;
import java.util.ArrayList;
//import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;
import sa.gov.nic.Configuration;
import org.slf4j.Logger;
import java.io.Serializable;

public class DDocOpener implements Serializable
{
    private static final Logger logger;
    private String temporaryDirectoryPath;
    
    public DDocContainer open(final String path) {
        return this.open(path, Configuration.getInstance());
    }
    
    public DDocContainer open(final String fileName, final Configuration configuration) {
        //final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger("ROOT");
        //root.setLevel(Level.INFO);
        DDocOpener.logger.info("Opening DDoc container from file: " + fileName);
        final DDocFacade facade = new DDocFacade(configuration);
        final ArrayList<DigiDocException> containerOpeningExceptions = new ArrayList<DigiDocException>();
        final SignedDoc signedDoc = this.openSignedDoc(fileName, containerOpeningExceptions);
        this.validateOpenedContainerExceptions(containerOpeningExceptions);
        facade.setContainerOpeningExceptions(containerOpeningExceptions);
        return this.createContainer(facade, signedDoc);
    }
    
    public DDocContainer open(final InputStream stream) {
        return this.open(stream, Configuration.getInstance());
    }
    
    public DDocContainer open(final InputStream stream, final Configuration configuration) {
        //final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger("ROOT");
       // root.setLevel(Level.INFO);
        DDocOpener.logger.info("Opening DDoc from stream");
        final ArrayList<DigiDocException> containerOpeningExceptions = new ArrayList<DigiDocException>();
        final DDocFacade facade = new DDocFacade(configuration);
        final SignedDoc signedDoc = this.openSignedDoc(stream, containerOpeningExceptions);
        this.validateOpenedContainerExceptions(containerOpeningExceptions);
        facade.setContainerOpeningExceptions(containerOpeningExceptions);
        return this.createContainer(facade, signedDoc);
    }
    
    public void useTemporaryDirectoryPath(final String temporaryDirectoryPath) {
        this.temporaryDirectoryPath = temporaryDirectoryPath;
    }
    
    private SignedDoc openSignedDoc(final String fileName, final ArrayList<DigiDocException> openContainerExceptions) throws DigiDoc4JException {
        try {
            final DigiDocFactory digFac = this.createDigiDocFactory();
            final boolean isBdoc = false;
            return digFac.readSignedDocOfType(fileName, isBdoc, (List)openContainerExceptions);
        }
        catch (DigiDocException e) {
            DDocOpener.logger.error("Failed to open DDoc from file " + fileName + ": " + e.getMessage());
            throw new DigiDoc4JException((Exception)e);
        }
    }
    
    private SignedDoc openSignedDoc(final InputStream stream, final ArrayList<DigiDocException> openContainerExceptions) throws DigiDoc4JException {
        try {
            final DigiDocFactory digFac = this.createDigiDocFactory();
            final SignedDoc signedDoc = digFac.readSignedDocFromStreamOfType(stream, false, (List)openContainerExceptions);
            DDocOpener.logger.info("DDoc container opened from stream");
            return signedDoc;
        }
        catch (DigiDocException e) {
            DDocOpener.logger.error("Failed to open DDoc from stream: " + e.getMessage());
            throw new DigiDoc4JException((Exception)e);
        }
    }
    
    private DigiDocFactory createDigiDocFactory() {
        final DigiDocFactory digFac = (DigiDocFactory)new SAXDigiDocFactory();
        if (StringUtils.isNotBlank((CharSequence)this.temporaryDirectoryPath)) {
            DDocOpener.logger.debug("Using temporary directory " + this.temporaryDirectoryPath);
            digFac.setTempDir(this.temporaryDirectoryPath);
        }
        return digFac;
    }
    
    private void validateOpenedContainerExceptions(final ArrayList<DigiDocException> openContainerExceptions) {
        if (SignedDoc.hasFatalErrs((ArrayList)openContainerExceptions)) {
            final DigiDocException fatalError = this.getFatalError(openContainerExceptions);
            DDocOpener.logger.error("Container has a fatal error: " + fatalError.getMessage());
            throw new DigiDoc4JException((Exception)fatalError);
        }
    }
    
    private DigiDocException getFatalError(final List<DigiDocException> openContainerExceptions) {
        DigiDocException exception = null;
        for (final DigiDocException openContainerException : openContainerExceptions) {
            if (openContainerException.getCode() == 75 && openContainerException.getMessage() != null && openContainerException.getMessage().contains("Invalid xml file")) {
                exception = new DigiDocException(75, "Invalid input file format.", openContainerException.getNestedException());
            }
            else {
                exception = openContainerException;
            }
        }
        return exception;
    }
    
    private DDocContainer createContainer(final DDocFacade facade, final SignedDoc signedDoc) {
        facade.setSignedDoc(signedDoc);
        return new DDocContainer(facade);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)DDocOpener.class);
    }
}
