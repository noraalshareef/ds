// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic.xades.validation;

import sa.gov.nic.SignatureValidationResult;
import java.io.Serializable;

public interface SignatureValidator extends Serializable
{
    SignatureValidationResult extractValidationErrors();
}
