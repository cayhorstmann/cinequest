package edu.sjsu.cs160.comm.cinequestitem;

import java.util.Vector;

import net.rim.device.api.util.Comparator;

import edu.sjsu.cs160.comm.Platform;

public class UserSchedule
{
   public static final int CONFIRMED = 0;
   public static final int MOVED = 1;
   public static final int REMOVED = 2;
   public static final int NOT_PRESENT = -1;
   
   private Vector confirmed = new Vector(); // Contains Schedule elements
   private Vector moved = new Vector(); 
   private Vector removed = new Vector();
   private String lastChanged = "";
   private boolean updated;     
   private boolean saved = true;
   private boolean dirty = true;
   
   public boolean isDirty()
   {
      return dirty;
   }
   
   public void setDirty(boolean dirty)
   {
      this.dirty = dirty;
   }
   
   public void setLastChanged(String lastChanged)
   {
      this.lastChanged = lastChanged;
   }
   
   public String getLastChanged()
   {
      return lastChanged;
   }
   
   public void setUpdated(boolean updated)
   {
      this.updated = updated;
   }
   
   public boolean isUpdated()
   {
      return updated;
   }  
   
   public void setSaved(boolean saved)
   {
      this.saved = saved;
   }
   
   public boolean isSaved()
   {
      return saved;
   }
   
   public String getIds()
   {
      StringBuffer b = new StringBuffer();
      for (int i = 0; i < confirmed.size(); i++)
      {
         Schedule item = (Schedule) confirmed.elementAt(i);
         if (b.length() > 0) b.append(",");
         b.append("" + item.getId());
      }
      for (int i = 0; i < moved.size(); i++)
      {
         Schedule item = (Schedule) moved.elementAt(i);
         if (b.length() > 0) b.append(",");
         b.append("" + item.getId());
      }
      
      return b.toString();
   }
   
   public boolean isScheduled(Schedule s)
   {
      int type = getType(s); 
      return type == CONFIRMED || type == MOVED;
   }
   
   public boolean conflictsWith(Schedule s)
   {
      for (int i = 0; i < confirmed.size(); i++)
      {
         Schedule item = (Schedule) confirmed.elementAt(i);
         if (s.getId() != item.getId() && item.overlaps(s)) return true;
      }
      for (int i = 0; i < moved.size(); i++)
      {
         Schedule item = (Schedule) moved.elementAt(i);
         if (s.getId() != item.getId() && item.overlaps(s)) return true;
      }
      return false;      
   }
   
   public Vector getItemsOn(String date)
   {
      Vector result = new Vector();
      for (int i = 0; i < confirmed.size(); i++)
      {
         Schedule item = (Schedule) confirmed.elementAt(i);
         if (item.getStartTime().startsWith(date)) result.addElement(item);
      }
      for (int i = 0; i < moved.size(); i++)
      {
         Schedule item = (Schedule) moved.elementAt(i);
         if (item.getStartTime().startsWith(date)) result.addElement(item);
      }
      for (int i = 0; i < removed.size(); i++)
      {
         Schedule item = (Schedule) removed.elementAt(i);
         if (item.getStartTime().startsWith(date)) result.addElement(item);
      }
      // sort by time, then venue-- SimpleSortingVector
      return Platform.getInstance().sort(result, new Comparator()
       {
         public int compare(Object o1, Object o2) {
            Schedule s1 = (Schedule) o1;
            Schedule s2 = (Schedule) o2;
            int d = s1.getStartTime().compareTo(s2.getStartTime());
            if (d != 0) return d;            
            return s1.getVenue().compareTo(s2.getVenue());   
         }
      });
   }
   
   public int getType(Schedule s)
   {
      for (int i = 0; i < confirmed.size(); i++)
      {
         Schedule item = (Schedule) confirmed.elementAt(i);
         if (s.getId() == item.getId()) return CONFIRMED;
      }
      for (int i = 0; i < moved.size(); i++)
      {
         Schedule item = (Schedule) moved.elementAt(i);
         if (s.getId() == item.getId()) return MOVED;
      }
      for (int i = 0; i < removed.size(); i++)
      {
         Schedule item = (Schedule) removed.elementAt(i);
         if (s.getId() == item.getId()) return REMOVED;
      }      
      return NOT_PRESENT;
   }
   
   public void add(Schedule s) 
   {
      for (int i = 0; i < confirmed.size(); i++)
      {
         Schedule item = (Schedule) confirmed.elementAt(i);
         if (s.getId() == item.getId()) return;
      }      
      confirmed.addElement(s); 
      saved = false; 
      dirty = true;
   }
   
   public void remove(Schedule s) 
   {  
      for (int i = 0; i < confirmed.size(); i++)
      {
         Schedule item = (Schedule) confirmed.elementAt(i);
         if (s.getId() == item.getId()) 
         {
            confirmed.removeElementAt(i);
            saved = false;
            dirty = true;
            return;
         }
      }
      for (int i = 0; i < moved.size(); i++)
      {
         Schedule item = (Schedule) moved.elementAt(i);
         if (s.getId() == item.getId()) 
         {
            moved.removeElementAt(i);
            saved = false;
            dirty = true;
            return;
         }
      }
      for (int i = 0; i < removed.size(); i++)
      {
         Schedule item = (Schedule) removed.elementAt(i);
         if (s.getId() == item.getId()) 
         {
            removed.removeElementAt(i);
            saved = false;
            dirty = true;
            return;
         }
      }      
   }
   
   public void add(Schedule s, int type) 
   {
      if (type == CONFIRMED) confirmed.addElement(s);
      else if (type == MOVED) moved.addElement(s);
      if (type == REMOVED) removed.addElement(s);
      saved = false;
      dirty = true;
   }   
   
   public Schedule[] getScheduleItems()
   {
      int size1 = confirmed.size();
      Schedule[] result = new Schedule[size1 + moved.size()];
      for (int i = 0; i < size1; i++)
      {
         Schedule item = (Schedule) confirmed.elementAt(i);
         result[i] = item;
      }
      for (int i = 0; i < moved.size(); i++)
      {
         Schedule item = (Schedule) moved.elementAt(i);
         result[size1 + i] = item;
      }      
      return result;
   }
}
