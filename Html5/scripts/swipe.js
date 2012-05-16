/*
 * @author: Jerry
 */
/*Object for films*/
function FDNode() {
    this.id = 0;
    this.title = "";
    this.description = "";
    this.tagline = "";
    this.genre = "";
    this.imageURL = "";
    this.director = "";
    this.producer = "";
    this.writer = "";
    this.cinematographer = "";
    this.editor = "";
    this.cast = "";
    this.country = "";
    this.language = "";
    this.film_info = "";
    this.schedule = new Array();
    this.schedule_count = 0;
    
}
/*Object for events and forums*/
function EFNode() {
    this.id = 0;
    this.mobile_item_id = 0;
    this.title = "";
    this.start_time = "";
    this.end_time = "";
    this.venue = "";
    this.description = "";
}

/*Object for the schedule of a film*/
function fSchedule() {
    this.start_time = "";
    this.end_time = "";
    this.venue = "";
}

//Array to store films
var films_array = new Array();

//Array to store events
var events_array = new Array();

//Array to store forums
var forums_array = new Array();

//counter for films_array
var films_counter = 0;

//counter for events_array
var events_counter = 0;

//counter for forums array
var forums_counter = 0;

//used to search in the films_array
var filmIndex = 0;

//used to search in the event_array
var eventIndex = 0;

//used to search in the forums_array
var forumsIndex = 0;

/*used for different categories: 
*films = 0, events=1, forums=2, DVDs=3
*/
var mode = -1;

/*
 * Call the php file to fetch the xml of events and forums from the Cinquest 
 * server. The fetched xml will be stored individually in events_array and
 * forums_array.  
 */
$(document).bind('pageinit', function(event) {
    event.stopImmediatePropagation();
    
    /*
     * This ajax get function is used to get the information of all the films
     * all the information of each film is stored in each element of the 
     * films_array.
     */
    $.ajax({
        type: 'GET',
        url:'cinequestproxy.php?type=films',
        dataType: 'xml',
        // async: false,
        success: function(xml) {
            $(xml).find('films').find('film').each(function() {
                var new_film = new FDNode();
                new_film.id = $(this).attr('id');
                films_array[films_counter] = new FDNode();
                films_array[films_counter] = new_film;
                films_counter++;
            });
        },
        
        /*get the information of each film*/
        complete: function() {
            for(var i = 0; i < films_counter; i++) {
                $.ajax({
                    type: 'GET',
                    url: proxy+'type=film&id='+films_array[i].id,
                    ajaxI: i,
                    dataType: 'xml',
                    success: function(xml) {
                        i = this.ajaxI;
                        var new_film = new FDNode();
                        new_film.id = films_array[i].id;
                        new_film.title = $(xml).find('title').text();
                        new_film.description =
                            $(xml).find('description').text();
                        new_film.tagline = $(xml).find('tagline').text();
                        new_film.genre = $(xml).find('genre').text();
                        new_film.imageURL = $(xml).find('imageURL').text();
                        new_film.director = $(xml).find('director').text();
                        new_film.producer = $(xml).find('producer').text();
                        new_film.writer = $(xml).find('writer').text();
                        new_film.cinematographer = 
                            $(xml).find('cinematographer').text();
                        new_film.editor = $(xml).find('editor').text();
                        new_film.cast = $(xml).find('cast').text();
                        new_film.country = $(xml).find('country').text();
                        new_film.language = $(xml).find('language').text();
                        new_film.film_info = $(xml).find('film_info').text();
                        // new_film.schedule = new fSchedule();
                        // new_film.schedule_count = 0;
                        $(xml).find('schedules').find('schedule').each(
                            function() {
                                var temp = new fSchedule();
                                temp.start_time = $(this).attr('start_time');
                                temp.end_time = $(this).attr('end_time');
                                temp.venue = $(this).attr('venue');
                                new_film.schedule[new_film.schedule_count] =
                                    new fSchedule();
                                new_film.schedule[new_film.schedule_count] =
                                    temp;
                                new_film.schedule_count++;
                        });
                        films_array[i] = new_film;
                    }
                });
            }
        }
    });
    
    /*
     * This ajax get function gets the information of all the events and forums.
     * The information is stored in the events_array and forums_array
     */
    $.ajax({
        type: "GET",
        url: proxy+'type=xml&name=events',
        dataType: "xml",
        success: function(xml) {
            //console.log("executing the xml");
            
            var events = $(xml).find('events').find('special_events');
            events.find('schedule').each(function() {
                var new_event = events_array[events_counter] = new EFNode();
                new_event.id = $(this).attr('id');
                new_event.mobile_item_id = $(this).attr('mobile_item_id');
                new_event.title = $(this).attr('title');
                new_event.start_time = $(this).attr('start_time');
                new_event.end_time = $(this).attr('end_time');
                new_event.venue = $(this).attr('venue');
                events_counter++;
            });
            
            var forums = $(xml).find('events').find('forums');
            forums.find('schedule').each(function() {
                var new_forms = forums_array[forums_counter] = new EFNode();
                new_forms.id = $(this).attr('id');
                new_forms.mobile_item_id = $(this).attr('mobile_item_id');
                new_forms.title = $(this).attr('title');
                new_forms.start_time = $(this).attr('start_time');
                new_forms.end_time = $(this).attr('end_time');
                new_forms.venue = $(this).attr('venue');
                forums_counter++;
            });
            
        },
        
        /*
         * To fetch the description of each event and forum
         */
        complete: function() {
            /*get the description of each event*/
            for(var i = 0; i < events_counter; i++) {
                 $.ajax({
                     type: 'GET',
                     url: proxy+'type=xml&name=items&id='+
                             events_array[i].mobile_item_id,
                     // async: false,
                     ajaxI: i,
                     dataType: 'xml',
                     success: function(xml) {
                         i = this.ajaxI;
                         events_array[i].description = 
                         $(xml).find('program_item').find('description').text();
                     }
                 });
            }
            /*get the description of each forums*/
            for(var i = 0; i < forums_counter; i++) {
                $.ajax({
                    type: 'GET',
                    url: proxy+'type=xml&name=items&id='+
                        forums_array[i].mobile_item_id,
                    ajaxI: i,
                    dataType: 'xml',
                    success: function(xml) {
                        i = this.ajaxI;
                        forums_array[i].description = 
                        $(xml).find('program_item').find('description').text();
                    }
                });
            }
        }
    });   
});

/*
 * When the film nav bar is clicked, The 15 Summer Later will be shown in the 
 * content div.
 */
$('#film_bar').click(function(event) {
    event.stopImmediatePropagation();
    event.preventDefault();
    mode = 0;
    $('#swipe_content').empty();
    filmIndex = 0;
    $('#swipe_content').html(generateContent(filmIndex));
});

/*
 * When the event nav bar is clicked, The Lady - Opening Night Film will be
 * shown in the content div. 
 */
$('#event_bar').click(function(event) {
    event.stopImmediatePropagation();
    event.preventDefault();
    mode = 1;
    $('#swipe_content').empty();
    eventIndex = 0;
    // alert("got " + events_array.length)
    $('#swipe_content').html(generateContent(eventIndex));
});

/*
 * When the forums nav bar is clicked, The Rough Cut will be shown in the 
 * content div.
 */
$('#forums_bar').click(function(event){
    event.stopImmediatePropagation();
    event.preventDefault();
    mode = 2;
    $('#swipe_content').empty();
    forumsIndex = 0;
    $('#swipe_content').html(generateContent(forumsIndex));
});

/*Swipe right function. Show the next item*/
$(document).bind('swiperight', function(event) {
    event.stopImmediatePropagation();
    event.preventDefault();
    if(mode == 0) {
        fillContent(++filmIndex);
    } else if(mode == 1) {
        fillContent(++eventIndex);
    } else if(mode == 2) {
        fillContent(++forumsIndex);
    }
    // alert("swipe right detacted");
    
});

/*Swipe left function. Show the last item*/
$(document).bind('swipeleft', function(event) {
    event.stopImmediatePropagation();
    event.preventDefault();
    if(mode == 0) {
        fillContent(--filmIndex);
    } else if(mode == 1) {
        fillContent(--eventIndex);
    } else if(mode == 2) {
        fillContent(--forumsIndex);
    }
    
});

/*
 * Fill the content div in the swipe.html with specific film, event, or forums
 * according to mode and index. Index is the position of the node in the array
 */
function fillContent(index){
    $('#swipe_content').empty();
    $('#swipe_content').hide();
    var content;
    if(mode == 0) {
        if(index < films_counter && index >= 0) {
            content = generateContent(index);
        } else if(index >= films_counter) {
            alert("This is the last film");
        } else {
            alert("This is the first film");
        }
    } else if(mode == 1) {
        if(index < events_counter && index >= 0) {
            content = generateContent(index);
        } else if(index >= events_counter) {
            alert("this is the end of the page");
        } else
            alert("this is the beginning of the page");
    } else if(mode == 2) {
        if(index < forums_counter && index >= 0) {
            content = generateContent(index);
        } else if(index >= forums_counter) {
            alert("This is the last forum");
        } else
            alert("This is the first forum");
    }
    $('#swipe_content').html(content);
    $('#swipe_content').slideDown();
}

/*
 * Generate a string involves the title, description, time of films, events,
 * and forums.
 */
function generateContent(index) {
    var spec_event, content;
    if(mode == 0) {
        spec_event = films_array[index];
        content = spec_event.title + '<br/>' + spec_event.description + '<br/>'+
            spec_event.tagline + '<br/>' + spec_event.genre + '<br/>' +
            spec_event.director + '<br/>' + spec_event.producer + '<br/>' +
            spec_event.writer + '<br/>' + spec_event.cinematographer  +
            '<br/>' + spec_event.editor + '<br/>' + spec_event.cast + '<br/>' +
            spec_event.country + '<br/>' + spec_event.language + '<br/>' +
            spec_event.film_info + '<br/>';
    } else if(mode == 1 || mode == 2) {
        if(mode == 1) spec_event = events_array[index];
        else if(mode == 2) spec_event = forums_array[index];
        content = spec_event.title + '<br/><br/>' ;
        content += spec_event.description + '<br/>';
        content += spec_event.start_time + '<br/>';
        content += spec_event.end_time + '<br/>';
        content += spec_event.venue + '<br/>';
    }
    return content;
}