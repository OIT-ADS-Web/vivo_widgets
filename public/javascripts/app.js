function fetchPreview(url) {

    $('#host').hide("drop", {  }, 150, function() {
        $('#loading').show("drop", { }, 150);

        //$.getJSON(url.url + '.jsonp' + url.parameters + "&callback=?");
        $.ajax({
            url: url.url + '.html' + url.parameters,
            success: function(data) {
                $("#host").html(data);

                $('#loading').hide("drop", { }, 150, function() {
                    $('#host').show("drop", { }, 150);

                });
            },
            error: function(xhr, status, errorThrown) {
                $("#host").html('<h4>Oops!</h4><p>There was a problem with your request:</p><strong>' + status + '</strong>');

                $('#loading').hide("drop", { }, 150, function() {
                    $('#host').show("drop", { }, 150);

                });
            }
        });
    });
}

function openHelp(helpId) {
    var $dialog = $('<div></div>')
    .html(help[helpId].details)
    .dialog({
        autoOpen: false,
        title: help[helpId].title,
        show: 'drop',
        hide: 'drop'
    });
    $dialog.dialog('open');
}

function renderSettings() {
    var style = '';
    if(viewModel.chosenStyle() === 'yes') {
        style="styled"
    } else {
        style='unstyled'
    };
    $('#settings').html('Current Settings: ' + viewModel.chosenLimit().label + ' ' + viewModel.chosenCollection().collectionName + ' in ' + viewModel.chosenFormat() + ' format and ' +  style);
}
/* Clipboard from http://code.google.com/p/zeroclipboard/ */
function initializeClipboard() {
		var clip = null;
		
		// function $(id) { return document.getElementById(id); }
		// 
		
			
			clip = new ZeroClipboard.Client();
			clip.setHandCursor( true );
			
			clip.addEventListener('load', function (client) {
				//alert("Flash movie loaded and ready.");
			});
			
			clip.addEventListener('mouseOver', function (client) {
				// update the text on mouse over
				clip.setText( $('#embed').val() );
			});
			
			clip.addEventListener('complete', function (client, text) {
				//alert("Copied text to clipboard: " + text );
				 $('#embed').effect("highlight", {}, 1500);

			});
			
			clip.glue( 'd_clip_button', 'd_clip_container' );
	
		
		// function debugstr(msg) {
		// 	var p = document.createElement('p');
		// 	p.innerHTML = msg;
		// 	$('d_debug').appendChild(p);
		// }

}

// Initialization

$(function() {
    ko.applyBindings(viewModel);

    	viewModel.url = ko.dependentObservable( function() {
	        latestUrl = 'http://localhost:9000/people/smithjm/publications/' + this.chosenLimit().label;
	        latestParams = '?collections='
		        + this.chosenCollection().collectionName
		        + '&formatting=' + this.chosenFormat()
		        + '&style=' + this.chosenStyle();

	        fetchPreview({url: latestUrl, parameters: latestParams});
	        var script = '<script type="text/javascript" src="' + latestUrl + '.js' + latestParams + '"> <\/script>';
	        $('#embed').val(script);
	      
	        renderSettings();
	        return {url: latestUrl, parameters: latestParams};
    	}, viewModel);
    
    $('#preview').click( function() {
        fetchPreview(viewModel.url());
        return false;
    });
    $('.help').click( function() {

        openHelp(this.id);

        return false;
    });
    $('#embed').focus(function(){ this.select(); })
    initializeClipboard();
    
});


