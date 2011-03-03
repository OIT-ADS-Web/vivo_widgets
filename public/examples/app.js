var firstRun = true;
function openModal(hrefUrl) {
	var embedSrc = '<textarea id="embed"  rows="5" name="embed" ><script type="text/javascript" src="' + hrefUrl
	+ '"> </script></textarea><a href="/builder/">Need more options or help?</a><br /><br />'
	+ '<div id="d_clip_container" style="position:relative"><div id="d_clip_button"  class="action">'
	+ 'Copy</div></div>';

	var $dialog = $('<div></div>')
	.html(embedSrc)
	.dialog({
		autoOpen: false,
		title: 'Embed in your web site',
		show: 'fade',
		hide: 'fade',
		modal: true,
		width: 460
	});
	$dialog.dialog('open');
	if (firstRun) {

		initializeClipboard();
		firstRun = false;
	}

}

/* Clipboard from http://code.google.com/p/zeroclipboard/ */
function initializeClipboard() {
	var clip = null;

	clip = new ZeroClipboard.Client();
	clip.setHandCursor( true );

	// clip.addEventListener('load', function (client) {
	// 	//alert("Flash movie loaded and ready.");
	// });
	//
	clip.addEventListener('mouseOver', function (client) {
		clip.setText( $('#embed').val() );
	});
	clip.addEventListener('complete', function (client, text) {
		$('#embed').effect("highlight", {}, 1500);

	});
	clip.glue( 'd_clip_button', 'd_clip_container' );

}

$( function() {
	$('.mysite').click( function() {

		openModal(this.href);

		return false;
	});
	$('#embed').live('focus', function() {

		this.select();

	});
});
