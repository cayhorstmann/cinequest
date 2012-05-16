/********
Sync
Author: Animesh Dutta
*********/



//function for synchronizing the schedule
function sync(){
var user = localStorage.getItem("user");
var pass = localStorage.getItem("pass");

var name=prompt("Please enter your Username","");
if (name!=null && name!="")
  {
  localStorage.setItem("user",name);;
  }
var pass=prompt("Please enter your Password","");
if (pass!=null && pass!="")
  {
  localStorage.setItem("pass",pass);;
  }


$('#schedule').children().remove();
var local = new localstore();
 var user = localStorage.getItem("user");
 var pass = localStorage.getItem("pass");
 make_url = proxy+"type=SLGET&username="+user+"&password="+pass;
var str="";
 $.ajax({
    type: "POST",
    url: make_url,
    dataType: "xml",
    success: function(xml) { 
        $confirmed = $(xml).find('confirmed');
        $confirmed.find('schedule').each(function(){
            id = $(this).attr('id');
                        localStorage.setItem(id,"");
                        });
        $moved = $(xml).find('moved');
        $moved.find('schedule').each(function(){
            id = $(this).attr('id');
                        localStorage.setItem(id,"");
                        });
        $removed = $(xml).find('removed');
        $removed.find('schedule').each(function(){
            id = $(this).attr('id');
                        localStorage.removeItem(id,"");
                        });
        }
        });
        var timeStamp = get_timeStamp();
        var items = get_idString();

        put_url = proxy+"type=SLPUT&username="+user+"&password="+pass+"&lastChanged="+timeStamp+"&items="+items;
        $.ajax({
          type: "POST",
          url: put_url,
          dataType: "xml",
          success: function(xml) {

          $confirmed = $(xml).find('confirmed');
          $confirmed.find('schedule').each(function(){
                id = $(this).attr('id');
                        localStorage.setItem(id,"");
                        });
          $moved = $(xml).find('moved');
          $moved.find('schedule').each(function(){
            id = $(this).attr('id');
                        localStorage.setItem(id,"");
                        }); 
          $removed = $(xml).find('removed');
          $removed.find('schedule').each(function(){
            id = $(this).attr('id');
                        localStorage.removeItem(id,"");
                        });
          }
          });
loadcontents();
}
/**************

get all the confirmed schedule ids
and push them to the server
with a timestamp from the current machine.
 
*****************/
function get_idString(){
 var local = new localstore();
 var localids = local.get_allkeys();
         var id_str = "";
         for (var i = 0; i < localids.length; i++) {
           var test = $.inArray(localids[i], ids_arr);
               if(test != -1){
                 id_str = id_str+localids[i]+",";
                }
          }
        var fin_str = id_str.slice(0, -1);
        return fin_str;
}

function get_timeStamp() {
        var d = new Date();
        var curr_hour = d.getHours();
        var curr_min = d.getMinutes();
        var curr_sec = d.getSeconds();
        var curr_day = d.getDate();
        var curr_yr = d.getFullYear();
        var curr_mth = d.getMonth() + 1;
        var timeStamp = curr_yr+"-"+curr_mth+"-"+curr_day+" "+curr_hour+":"+curr_min+":"+curr_sec ;
        return timeStamp ;
}