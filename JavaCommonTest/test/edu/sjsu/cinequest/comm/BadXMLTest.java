package edu.sjsu.cinequest.comm;

import java.util.Vector;

import edu.sjsu.cinequest.comm.cinequestitem.ProgramItem;
import edu.sjsu.cinequest.javase.JavaSEPlatform;
import junit.framework.TestCase;

public class BadXMLTest extends TestCase {
    private QueryManager mgr;
    protected void setUp() throws Exception
    {
        Platform.setInstance(new JavaSEPlatform());
        mgr = new QueryManager();
    }

    public void testAllTitles() {
        TestCallback callback = new TestCallback();
        mgr.getAllPrograms(callback);
        @SuppressWarnings("unchecked") Vector<ProgramItem> items = (Vector<ProgramItem>) callback.getResult();
        for (ProgramItem item : items)
        {
        	TestCallback callback2 = new TestCallback();
        	mgr.getProgramItem(item.getId(), callback2);
        	ProgramItem pi = (ProgramItem) callback2.getResult();
        	assertNotNull(pi.getTitle());
        	assertNotNull(pi.getDescription());
        }

    }

}
