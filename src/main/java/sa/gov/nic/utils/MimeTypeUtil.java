// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.utils;

import org.slf4j.LoggerFactory;
import eu.europa.esig.dss.MimeType;
import org.slf4j.Logger;

public final class MimeTypeUtil
{
    private static final Logger log;
    
    private MimeTypeUtil() {
    }
    
    public static MimeType mimeTypeOf(String mimeType) {
        final String s = mimeType;
        switch (s) {
            case "txt.html": {
                MimeTypeUtil.log.warn("Incorrect Mime-Type <{}> detected, fixing ...", (Object)mimeType);
                mimeType = "text/html";
                break;
            }
            case "file": {
                MimeTypeUtil.log.warn("Incorrect Mime-Type <{}> detected, fixing ...", (Object)mimeType);
                mimeType = "application/octet-stream";
                break;
            }
        }
        if (mimeType.indexOf(92) > 0) {
            MimeTypeUtil.log.warn("Incorrect Mime-Type <{}> detected, fixing ...", (Object)mimeType);
            mimeType = mimeType.replace("\\", "/");
        }
        return MimeType.fromMimeTypeString(mimeType);
    }
    
    static {
        log = LoggerFactory.getLogger((Class)MimeTypeUtil.class);
    }
}
