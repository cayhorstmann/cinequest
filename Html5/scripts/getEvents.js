/*
 * author: Jiajie Wu
 */
$(function() {
    $('#events').empty();
    $.ajax({
        type: "GET",
        url: "cinequestproxy.php?type=xml&name=events",
    dataType: "xml",
        success: function(xml) {
        var current_letter;
        var previous_letter;
        $events = $(xml).find('events');
        $forums = $events.find('special_events');
        $forums.find('schedule').each(function() {
                id = $(this).attr('mobile_item_id');
                title = $(this).attr('title');

                var main_list = document.getElementById('events');
                var list_item = document.createElement('li');
                list_item.setAttribute('id', 'link_'+ id);

                var split = title.split(" ");
                if (split[0] == "The" || split[0] == "A") 
                 {
                     previous_letter = current_letter;
                     current_letter = split[1].charAt(0);
                 } 
                 else 
                 {
                     previous_letter = current_letter;
                     current_letter = split[0].charAt(0);
                 }
                 if (previous_letter != current_letter)
                 {                  
                     $('<li data-role="list-divider"></li>').html(current_letter).appendTo('#events');
                 }

                var ckBox = document.createElement('input');
                ckBox.setAttribute('type', 'checkbox');
                ckBox.setAttribute('id', 'checkbox');
                ckBox.setAttribute('class', 'checkbox');
                list_item.appendChild(ckBox);

                 list_item.innerHTML += '<a href="content.html" id= "'+id+'" class="forum_name">' + title + '</a>';
                main_list.appendChild(list_item);
                 $('#events').listview('refresh');  
            });
        }
    });   
    
});

$('#list-content').click(function(event) {
    event.target.setAttribute('clicked', 'yes');
    var specific_id = $('a[clicked*="yes"]').attr('id');
    // $('#content').empty();

    $.ajax({
        type: "GET",
        url: 'cinequestproxy.php?type=xml&name=items&id='+ specific_id,
        dataType: "xml",
        success: function(xml) {
            var title = $(xml).find('program_item').find('title').text();
            var description = $(xml).find('program_item').find('description').text();
            var start_time = $(xml).find('program_item').find('schedule').attr('start_time');
            var end_time = $(xml).find('program_item').find('schedule').attr('end_time');
            var schedule = document.createElement('div');
            schedule.setAttribute('id', 'schedule');
            schedule.innerHTML = 'start time is ' + start_time + '</br>';
            schedule.innerHTML += 'end time is ' + end_time;
            var content = document.getElementById('content');
            $('<div id="content-title"></div>').html(title).appendTo(content);
            $('<div id="content-description"></div>').html(description).appendTo(content);
            content.appendChild(schedule);

        }
    });
    var remove_clicked = document.getElementById(specific_id);
    remove_clicked.removeAttribute('clicked');
});