$.ajax({
    type: "GET",
    url: "cinequestproxy.php?type=films",
    dataType: "xml",
    success: function(xml) {
        $films = $(xml).find('films');
        var current_letter;
        var previous_letter;
        $films.find('film').each(function(){
            id = $(this).attr('id');
            title = $(this).find('title').text();
            var split = title.split(" ");
            if (split[0] == "The" || split[0] == "A") {
                previous_letter = current_letter;
                current_letter = split[1].charAt(0);
            } else {
                previous_letter = current_letter;
                current_letter = split[0].charAt(0);
            }
            if (previous_letter != current_letter)
            $('<li data-role="list-divider"></li>').html(current_letter).appendTo('#films');
            $('<li></li>').html('<a href="#" id="link_'+id+'">'+title+'</a>').appendTo('#films');
            $('#films').listview('refresh');
        });
     }
});

$('#list-content').click(function(event) {
    event.target.setAttribute('clicked', 'yes');
    var specific_id = $('a[clicked*="yes"]').attr('id');
    $('#filmdetail').empty();
    
    $.ajax({
         type: "GET",
         url: "cinequestproxy.php?type=film&id="+specific_id,
         dataType: "xml",
         success: function(xml) {
             $film = $(xml).find('film');
             $schedules = $(xml).find('schedules');
             var title = $film.find('title').text();
             var description = $film.find('description').text();
             var genre = $film.find('genre').text();
             var cinematographer = $film.find('cinematographer').text();
             var director = $film.find('director').text();
             var writer = $film.find('writer').text();
             var language = $film.find('language').text();
             var editor = $film.find('editor').text();
             var cast = $film.find('cast').text();
             var producer = $film.find('producer').text();
             var country = $film.find('country').text();
             var image = $film.find('imageURL').text();
             $('<div id="content-title" align="center"></div>').html('<h3>'+title+'</h3>').appendTo('#filmdetail');
             $('<div style="text-align: center;"></div>').html('<img src="'+image+'" alt="Alternate image"/>').appendTo('#filmdetail');
             $('<br>').appendTo('#filmdetail');
             $('<div id="content-description"></div>').html(description).appendTo('#filmdetail');
             $('<br>Genre: '+genre+'<br>Cinematographer: '+cinematographer+'<br>Director: '+director+'<br>Writer: '+writer+'<br>Language: '+language+'<br>Editor: '+editor+'<br>Cast: '+cast+'<br>Producer: '+producer+'<br>Country: '+country+'<br><br>').appendTo('#filmdetail');

             //work on this schedule part
             $('<div id="schedule-content"></div>').appendTo('#filmdetail');
             $('#schedule-content').html($('<ul data-role="listview" id="scheduleslist" data-inset="true"></ul>'));
             $('#schedule-content').trigger('create');
             $('#scheduleslist').append($('<li data-role="list-divider">Schedules</li>')); 
             $('#scheduleslist').listview('refresh');

         $schedules.find('schedule').each(function() {
                 s_id = $(this).attr('id');
                 s_program_item_id = $(this).attr('program_item_id');
                 s_start_time = $(this).attr('start_time');
                 s_end_time = $(this).attr('end_time');
                 s_venue = $(this).attr('venue');
                 var ts = s_start_time.split(" ");
                 var ds = ts[0].split("-");
                 var d = new Date(ds[0]+"/"+ds[1]+"/"+ds[2]); 
                 //var now = new Date();
                 //var ss = dateFormat(now, "dddd, mmmm d, yyyy, h:MM:ss TT");
                 //var dd = $.datepicker.formatDate('yy-mm-dd', new Date(2007, 1 - 1, 26));

                  $('<li id="'+id+'"></li>').html('Date: ' + d.toDateString() +'<br />Time: ').appendTo('#scheduleslist');
                  $('#scheduleslist').listview('refresh');
               });
             }
        });
    var remove_clicked = ('#'+specific_id);
    remove_clicked.removeAttribute('clicked');
});
