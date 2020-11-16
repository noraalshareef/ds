// 
// Decompiled by Procyon v0.5.36
// 

package sa.gov.nic.impl.asic;

import java.util.Iterator;
import java.util.ArrayList;
import sa.gov.nic.DataFile;
import java.util.Collection;
import java.util.List;
import eu.europa.esig.dss.DSSDocument;

public class DetachedContentCreator
{
    private DSSDocument firstDetachedContent;
    private List<DSSDocument> detachedContentList;
    
    public DetachedContentCreator populate(final Collection<DataFile> dataFiles) throws Exception {
        this.detachedContentList = new ArrayList<DSSDocument>(dataFiles.size());
        if (dataFiles.isEmpty()) {
            return this;
        }
        this.populateDetachedContent(dataFiles);
        return this;
    }
    
    private void populateDetachedContent(final Collection<DataFile> dataFiles) {
        final Iterator<DataFile> dataFileIterator = dataFiles.iterator();
        this.firstDetachedContent = dataFileIterator.next().getDocument();
        this.detachedContentList.add(this.firstDetachedContent);
        while (dataFileIterator.hasNext()) {
            final DataFile dataFile = dataFileIterator.next();
            final DSSDocument document = dataFile.getDocument();
            this.detachedContentList.add(document);
        }
    }
    
    public List<DSSDocument> getDetachedContentList() {
        return this.detachedContentList;
    }
    
    public DSSDocument getFirstDetachedContent() {
        return this.firstDetachedContent;
    }
}
