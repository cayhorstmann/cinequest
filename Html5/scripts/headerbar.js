var str="<h1>Cinequest</h1>"+
    "<div data-role='controlgroup' data-type='horizontal' id='btn_group'>"+
    "<a href='index.html' data-role='button' data-inline='true'>Home</a>"+
    "<a href='swipe_page.html' data-role='button' rel='external'"+
    "data-inline='true' data-transition='flip'>Swipe Mode</a>"+
    "</div>"+
    "<a href='#' data-role='button' class='ui-btn-right'>Info</a>"+
    "<div data-role='navbar'>"+
    "<ul><li><a href='films.html' id='filmstag'>FILMS</a></li>"+
    "<li><a href='events.html'>EVENTS</a></li>"+
    "<li><a href='forum.html'>FORUMS</a></li>"+
    "<li><a href='#'>DVDS</a></li>"+
    "<li><a href='schedule.html'>MY SCHEDULE</a></li></ul></div></ul></div>";

$("header").html(str);