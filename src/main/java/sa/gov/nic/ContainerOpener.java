// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic;

import org.slf4j.LoggerFactory;
import sa.gov.nic.impl.pades.PadesContainer;
import org.apache.commons.io.IOUtils;
import sa.gov.nic.impl.asic.asice.bdoc.BDocContainer;
import sa.gov.nic.impl.asic.asice.AsicEContainer;
import sa.gov.nic.impl.asic.asics.AsicSContainer;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import sa.gov.nic.exceptions.DigiDoc4JException;
import sa.gov.nic.impl.ddoc.DDocOpener;
import java.io.File;
import sa.gov.nic.utils.Helper;
import org.slf4j.Logger;

public class ContainerOpener
{
    private static final Logger logger;
    
    public static Container open(final String path, final Configuration configuration) throws DigiDoc4JException {
        ContainerOpener.logger.debug("Opening container from path: " + path);
        try {
            if (Helper.isPdfFile(path)) {
                return openPadesContainer(path, configuration);
            }
            if (Helper.isZipFile(new File(path))) {
                return openBDocContainer(path, configuration);
            }
            return new DDocOpener().open(path, configuration);
        }
        catch (EOFException eof) {
            final String msg = "File is not valid.";
            ContainerOpener.logger.error(msg);
            throw new DigiDoc4JException(msg);
        }
        catch (IOException e) {
            ContainerOpener.logger.error(e.getMessage());
            throw new DigiDoc4JException(e);
        }
    }
    
    public static Container open(final String path) throws DigiDoc4JException {
        return open(path, Configuration.getInstance());
    }
    
    public static Container open(final InputStream stream, final boolean actAsBigFilesSupportEnabled) {
        ContainerOpener.logger.debug("Opening container from stream");
        final BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
        try {
            if (!Helper.isZipFile(bufferedInputStream)) {
                return new DDocOpener().open(bufferedInputStream);
            }
            if (Helper.isAsicSContainer(bufferedInputStream)) {
                return new AsicSContainer(bufferedInputStream);
            }
            if (Helper.isAsicEContainer(bufferedInputStream)) {
                return new AsicEContainer(bufferedInputStream);
            }
            return new BDocContainer(bufferedInputStream);
        }
        catch (IOException e) {
            ContainerOpener.logger.error(e.getMessage());
            throw new DigiDoc4JException(e);
        }
        finally {
            IOUtils.closeQuietly((InputStream)bufferedInputStream);
        }
    }
    
    public static Container open(final InputStream stream, final Configuration configuration) {
        ContainerOpener.logger.debug("Opening container from stream");
        final BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
        try {
            if (!Helper.isZipFile(bufferedInputStream)) {
                return new DDocOpener().open(bufferedInputStream, configuration);
            }
            if (Helper.isAsicSContainer(bufferedInputStream)) {
                return new AsicSContainer(bufferedInputStream, configuration);
            }
            if (Helper.isAsicEContainer(bufferedInputStream)) {
                return new AsicEContainer(bufferedInputStream, configuration);
            }
            return new BDocContainer(bufferedInputStream, configuration);
        }
        catch (IOException e) {
            ContainerOpener.logger.error(e.getMessage());
            throw new DigiDoc4JException(e);
        }
        finally {
            IOUtils.closeQuietly((InputStream)bufferedInputStream);
        }
    }
    
    private static Container openBDocContainer(final String path, final Configuration configuration) {
        configuration.loadConfiguration("digidoc4j.yaml", false);
        if (Helper.isAsicSContainer(path)) {
            return new AsicSContainer(path, configuration);
        }
        if (Helper.isAsicEContainer(path)) {
            return new AsicEContainer(path, configuration);
        }
        return new BDocContainer(path, configuration);
    }
    
    private static Container openPadesContainer(final String path, final Configuration configuration) {
        configuration.loadConfiguration("digidoc4j.yaml", false);
        return new PadesContainer(path, configuration);
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)ContainerOpener.class);
    }
}
