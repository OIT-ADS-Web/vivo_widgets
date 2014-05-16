(function() {
  function fetchPreview(url) {

    $('#host').hide("blind", {  }, 150, function() {
      $('#loading').show("blind", { }, 150);
      $.ajax({
        url: url.url + '.html' + url.parameters,
        success: function(data) {
          $("#host").html(data);

          $('#loading').hide("blind", { }, 150, function() {
            $('#host').show("blind", { }, 150);

          });
        },
        error: function(xhr, status, errorThrown) {
          $("#host").html('<h4>Oops!</h4><p>There was a problem with your request:</p><strong>' + status + '</strong>');

          $('#loading').hide("blind", { }, 150, function() {
            $('#host').show("blind", { }, 150);

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

    if(viewModel.chosenStartDate() != '') {
      start = ", after " + viewModel.chosenStartDate()
    } else {
      start = ""
    };

    if(viewModel.chosenEndDate() != '') {
      end = ", before " + viewModel.chosenEndDate()
    } else {
      end = ""
    };

    $('#settings').html(viewModel.chosenLimit().label + ' ' +
        viewModel.chosenCollection() + start + end + ', in ' +
        viewModel.chosenFormat() + ' format <em>' +  style + '</em>');
  }

  /* Clipboard from http://code.google.com/p/zeroclipboard/ */
  function initializeClipboard() {
    var clip = null;

    clip = new ZeroClipboard.Client();
    clip.setHandCursor( true );

    // clip.addEventListener('load', function (client) {
    //   //alert("Flash movie loaded and ready.");
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

  // Initialization

  $( function() {
    ko.applyBindings(viewModel);

    viewModel.url = ko.dependentObservable( function() {
      var apiVersion = $('body').attr('data-api-version');
      var baseUri = "api/" + apiVersion + "/" + $('#group').attr('value');
      var groupUrl = window.location.toString().replace(/builder(.*)/,baseUri);
      var latestUrl = groupUrl + "/" + this.chosenCollection().toLowerCase() + "/" + this.chosenLimit().label;
      var latestParams = '?uri=' + $("#uri").attr("value") + '&formatting=' +
        this.chosenFormat() + '&style=' + this.chosenStyle() + '&start=' +
        this.chosenStartDate() + '&end=' + this.chosenEndDate();

      // Refresh Display
      fetchPreview({url: latestUrl, parameters: latestParams});
      renderSettings();
      // Update TextArea
      var script = '<script type="text/javascript" src="' + latestUrl + '.js' + latestParams + '"> <\/script>';
      $('#embed').val(script);
      // Update other feeds
      $("#rss").attr("href", latestUrl + ".rss" + latestParams);
      $("#json").attr("href", latestUrl +".json" + latestParams);
      $("#jsonp").attr("href", latestUrl +".jsonp" + latestParams);
      $("#html").attr("href", latestUrl +".html" + latestParams);
      $("#js").attr("href", latestUrl + ".js" + latestParams);

      var fullUrl = groupUrl + "/complete/all";
      var fullParams = '?uri=' + $("#uri").attr("value");
      $("#fullJson").attr("href", fullUrl + ".json" + fullParams);
      $("#fullJsonp").attr("href", fullUrl + ".jsonp" + fullParams);

      return {url: latestUrl, parameters: latestParams};
    }, viewModel);

    $('.help').click( function() {

      openHelp(this.id);

      return false;
    });

    $('#advanced').click( function() {
      if($('#otherFormats').is(":visible")) {
        $('#otherFormats').hide("blind", { }, 200);
      } else {
        $('#otherFormats').show("blind", { }, 200);
      }

      return false;
    });

    $('#embed').focus( function() {
      this.select();
    });

    initializeClipboard();

  });
})();
