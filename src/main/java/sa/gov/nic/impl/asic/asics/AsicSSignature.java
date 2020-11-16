// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.asics;

import sa.gov.nic.impl.asic.xades.validation.SignatureValidator;
import sa.gov.nic.impl.asic.xades.XadesSignature;
import sa.gov.nic.impl.asic.AsicSignature;

public class AsicSSignature extends AsicSignature
{
    public AsicSSignature(final XadesSignature xadesSignature, final SignatureValidator validator) {
        super(xadesSignature, validator);
    }
}
