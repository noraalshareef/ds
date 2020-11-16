// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.asice.bdoc;

import org.slf4j.LoggerFactory;
import sa.gov.nic.impl.asic.xades.validation.SignatureValidator;
import sa.gov.nic.impl.asic.xades.XadesSignature;
import org.slf4j.Logger;
import sa.gov.nic.impl.asic.asice.AsicESignature;

public class BDocSignature extends AsicESignature
{
    private static final Logger logger;
    
    public BDocSignature(final XadesSignature xadesSignature, final SignatureValidator validator) {
        super(xadesSignature, validator);
        BDocSignature.logger.debug("New BDoc signature created");
    }
    
    static {
        logger = LoggerFactory.getLogger((Class)BDocSignature.class);
    }
}
