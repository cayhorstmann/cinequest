package edu.sjsu.cinequest.client;

import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.Persistable;
import edu.sjsu.cinequest.comm.Callback;
import edu.sjsu.cinequest.comm.Platform;
import edu.sjsu.cinequest.comm.cinequestitem.Schedule;
import edu.sjsu.cinequest.comm.cinequestitem.UserSchedule;

public class User
{
   private String email;
   private String password;
   private UserSchedule schedule;
   private boolean loggedIn;
   private boolean failedAuthorization;
   private LoginDialog1 dialog;
   
   // key produced by: echo -n "edu.sjsu.cinequest.comm.cinequestitem.User" | md5sum | cut -c1-16
   private static final long PERSISTENCE_KEY = 0x74dbb8dc65739d30L;   
   
   public User()
   {
      schedule = new UserSchedule();
      UserData data = (UserData) Platform.getInstance().loadPersistentObject(PERSISTENCE_KEY);
      if (data == null)
      {
         email = "";
         password = "";
      }
      else
      {
         email = data.email;
         Object decryptedPassword = Platform.getInstance().crypt(data.encryptedPassword, /* decrypt = */ true);
         if (decryptedPassword == null) password = "";
         else password = (String) decryptedPassword;
         for (int i = 0; i < data.scheduleItems.length; i++) schedule.add(data.scheduleItems[i], UserSchedule.CONFIRMED);
         schedule.setLastChanged(data.lastChanged);
         loggedIn = true;
      }
   }  
       
   public void persistSchedule()
   {
      if (!schedule.isSaved())
      {
         UserData data = new UserData();
         data.email = email;
         data.encryptedPassword = Platform.getInstance().crypt(password, /* decrypted = */ false);
         data.scheduleItems = schedule.getScheduleItems();
         data.lastChanged = schedule.getLastChanged();
         Platform.getInstance().storePersistentObject(PERSISTENCE_KEY, data);
         schedule.setSaved(true);
      }
   }
   
   public void loadSchedule(final UserScheduleScreen screen)
   {
      if (!schedule.isSaved()) 
      {
         int answer = Dialog.ask(Dialog.D_YES_NO, "Really discard the current schedule?");
         if (answer != Dialog.OK) return;
      }      
      if (!loggedIn || failedAuthorization)
      {
         dialog = new LoginDialog1(loggedIn ? "Load schedule" : "Log in", email, password,
               new Runnable()
         {
            public void run()
            {
               Ui.getUiEngine().popScreen(dialog);
               email = dialog.getEmail();
               password = dialog.getPassword();
               readSchedule(screen);               
            }
         });
         Ui.getUiEngine().pushScreen(dialog);         
      }
      else
         readSchedule(screen);
   }
   

   public void readSchedule(final UserScheduleScreen screen)
   {
      final Callback callback = new ProgressMonitorCallback()
      {                  
         public void invoke(Object result)
         {
            super.invoke(result);
            if (result == null) // login failed
            {
               failedAuthorization = true;
               Dialog.alert(loggedIn ? "Unable to load schedule" : "Login failed."); 
            }
            else
            {
               failedAuthorization = false;
               loggedIn = true;
               schedule = (UserSchedule) result;
               persistSchedule();
               screen.setScheduleItems();
            }            
         }
      };
      
      Main.getQueryManager().getSchedule(callback, email, password);
   }
   
   public void saveSchedule()
   {
      if (schedule.isSaved()) return;
      if (!loggedIn || failedAuthorization)
      {
         dialog = new LoginDialog1("Save schedule", email, password, new Runnable() {
            public void run() {
               Ui.getUiEngine().popScreen(dialog);
               email = dialog.getEmail();
               password = dialog.getPassword();
               writeSchedule();
            }            
         });
         Ui.getUiEngine().pushScreen(dialog);
      }
      else writeSchedule();
   }
   
   public void writeSchedule() 
   {
      final Callback callback = new ProgressMonitorCallback()
      {         
         public void invoke(Object result)
         {
            super.invoke(result);
            if (result == null)
            {               
               failedAuthorization = true; // Next save prompts for username/password
               Dialog.alert("Unable to save schedule.");
            }
            else
            {
               failedAuthorization = false;
               loggedIn = true; // they might have called this from SchedulesScreen
               UserSchedule newSchedule = (UserSchedule) result;
               Platform.getInstance().log("lastChanged=" + newSchedule.getLastChanged());
               if (newSchedule.isUpdated()) 
               {
                  schedule = newSchedule;
                  persistSchedule(); 
               }
               else 
               {
                  int answer = Dialog.ask(Dialog.D_YES_NO, "Conflicting schedule on server. Really save this schedule?");
                  if (answer == Dialog.OK)
                  {
                     schedule.setLastChanged(newSchedule.getLastChanged());
                     writeSchedule();
                  }
               }
            }
         }
      };
      
      Main.getQueryManager().saveSchedule(callback, email, password, schedule);
   }
   
   public UserSchedule getSchedule() { return schedule; }
   
   public boolean isLoggedIn()
   {
      return loggedIn;
   }
   
   
   public void logout()
   {
      if (!schedule.isSaved())
      {
         int answer = Dialog.ask(Dialog.D_YES_NO, "Save schedule?");
         if (answer == Dialog.OK)
         {
            saveSchedule();
         }         
      }
      schedule = new UserSchedule();
      loggedIn = false;      
   }
   
   static class UserData implements Persistable 
   {
      String email;
      Object encryptedPassword;
      Schedule[] scheduleItems;
      String lastChanged;
   }
}
