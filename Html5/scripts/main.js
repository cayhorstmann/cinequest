$(function() {
    $( "#activities" ).accordion({ autoHeight: false });
});

$.ajax({
    type: "GET",
    url: "cinequestproxy.php?type=films",
    dataType: "xml",
    success: function(xml) { 
        $films = $(xml).find('films');
        $films.find('film').each(function(){
            id = $(this).attr('id');
            title = $(this).find('title').text();
            $('<div class="film" id="link_'+id+'"></div>').html(
                title).appendTo('#films');
        });
    }
});

//
