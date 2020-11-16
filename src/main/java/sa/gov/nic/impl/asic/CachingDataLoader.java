// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import sa.gov.nic.Configuration;
import eu.europa.esig.dss.client.http.commons.FileCacheDataLoader;

public class CachingDataLoader extends FileCacheDataLoader
{
    public CachingDataLoader(final Configuration configuration) {
        DataLoaderDecorator.decorateWithProxySettings((CommonsDataLoader)this, configuration);
        DataLoaderDecorator.decorateWithSslSettings((CommonsDataLoader)this, configuration);
    }
}
