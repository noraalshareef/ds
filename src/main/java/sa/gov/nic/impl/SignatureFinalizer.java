// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl;

import sa.gov.nic.Signature;
import java.io.Serializable;

public interface SignatureFinalizer extends Serializable
{
    Signature finalizeSignature(final byte[] p0);
}
