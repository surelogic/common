
var frames = {};

function makeToc() {
    var id=0;
    $('.toc a').each(function() {
        var toc = '<ol>\n';
        var a = $(this);
        var divId = a.prop('href').match(/#.*/)[0];
        $(divId).children('h3').each(function () {
            var txt = $(this).text();
            var tagId = 'vid' + id++;
            var frame = $(this).next();
            var desc = frame.next();
            frames[tagId] = frame.get(0);
            var thumbLink;
            var suffix;
            var icon;
            if(frames[tagId].src) {
                var vidId = frames[tagId].src.match(/embed\/([^&]+)\?/)[1];
                thumbLink = 'http://img.youtube.com/vi/' + vidId + '/default.jpg';
                suffix='';
                icon='<i class="thumb-icon fa fa-play-circle-o fa-3x"></i>';
            } else {
                thumbLink = '/images/favicon.ico';
                suffix = ' stub';
                icon = '<i class="stub-icon">Coming Soon</i>';
            }
            var parent = desc.wrap('<div></div>').parent();
            parent.prepend('<a class="thumb' +suffix + '" href="#' + tagId + '"><img src="' + thumbLink + '"/>' + icon + '</a>');
            parent.after('<div class="clear"></div>');
            frame.remove();
            $(this).attr('id',tagId);
            toc += '\t<li><a href="#' + tagId + '">' + txt + '</a></li>\n';
        });
        toc += '</ol>';
//        a.parent().append(toc);
    });
     $('.video-section a:not(.stub)').click(function(e) {
         var id = $(this).prop('href').match(/#(.*)/)[1];
         $('#wrapper').html(frames[id]);
         openOverlay();
         e.preventDefault();
     });
     $('.video-section a.stub').click(function(e) {
         e.preventDefault();
     });
    
}

function closeOverlay() {
    $('.overlay').animate({
        top : '-=300',
        opacity : 0
    }, 400, function() {
        $('#overlay-shade').fadeOut(300);
        $(this).css('display','none');
        $('#wrapper').html('');
    });
}

function openOverlay() {
    computeOverlaySize();
    var ol = $('#overlay');
    if ($('#overlay-shade').length == 0) {
        $('body').prepend('<div id="overlay-shade"></div>');
    }
    $('#overlay-shade').fadeTo(300, 0.6, function() {
        var props = {
            oLayWidth       : ol.width(),
            scrTop          : $(window).scrollTop(),
            viewPortWidth   : $(window).width()
        };

        var leftPos = (props.viewPortWidth - props.oLayWidth) / 2;

        ol
            .css({
                display : 'block',
                opacity : 0,
                top : '-=300',
                left : leftPos+'px'
            })
            .animate({
                top : props.scrTop + 40,
                opacity : 1
            }, 600);
    });
    $('#overlay-shade, .overlay a').click(function(e) {
        closeOverlay();
        if ($(this).attr('href') == '#') e.preventDefault();
    });
    $(document).keyup(function(e) {
        if (e.keyCode == 27) { closeOverlay(); }   // esc
    });
}
var vidx = 1280;
var vidy = 720;
var minx = 640;
var miny = 360;
var padx = 100;

function computeOverlaySize() {
    var windowWidth = $(window).width();
    var calcvidx = vidx;
    if(windowWidth < calcvidx + padx) {
            calcvidx = Math.max(minx , windowWidth - padx);
    }
    var calcvidy = calcvidx * (vidy / vidx);
    $('#wrapper iframe').css('width', calcvidx + 'px');
    $('#wrapper iframe').css('height', Math.round(calcvidy) + 'px');
    var leftPos = (windowWidth - $('#overlay').width()) / 2;
    $('#overlay').css('left',leftPos+'px');//.css('width', overx + 'px')

}

$(document).ready(function() {
    makeToc();
    $(window).resize(computeOverlaySize);
});




