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