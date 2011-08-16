function closeModal() {
  $('#modal').hide("drop", { }, 200, function() {

   });
}

function openModal(obj) {
  var embedSrc = '<script type="text/javascript" src="' + obj.href + '"> </script>';

  $('#embed').val(embedSrc);
  $('#embedDescription').html("Embed code for 5 " + obj.getAttribute('collection') + ", in detailed format with default style rules applied:")
  var pos = $(obj).offset();
  var width = $(obj).width();

  $("#modal").css( { "left": (pos.left - width) + "px", "top":pos.top + "px" } );

  $('#modal').show("drop", { }, 150, function() {

      });
}

/* Clipboard from http://code.google.com/p/zeroclipboard/ */
function initializeClipboard() {
  var clip = null;

  clip = new ZeroClipboard.Client();
  clip.setHandCursor( true );

  clip.addEventListener('load', function (client) {

      });
  //
    clip.addEventListener('mouseOver', function (client) {
        clip.setText( $('#embed').val() );
        document});
  clip.addEventListener('complete', function (client, text) {
      $('#embed').effect("highlight", {}, 1500);

      });
  clip.glue( 'd_clip_button', 'd_clip_container' );
  $('#modal').hide();
}

$(document).ready(function(){
    $(".widgetBuilderLink").attr('href',WidgetConfig.builderLink());
    for(var linkSelector in WidgetConfig.collectionLinkMap) {
      if (WidgetConfig.collectionLinkMap.hasOwnProperty(linkSelector)) {
        $(linkSelector).append(' ' + WidgetConfig.collectionLink(WidgetConfig.collectionLinkMap[linkSelector]))
      }
    }
    $('.body').click( function() {
      if($('#modal').is(':visible')) {
      closeModal();
    }

   return false;
  });
	$('.mysite').click( function() {
		var that = this;
		openModal(that);

		return false;
	});
	$('#embed').focus( function() {

		this.select();

	});
	$('#close').click( function() {
		closeModal();

		return false;
	});
	initializeClipboard();

});

