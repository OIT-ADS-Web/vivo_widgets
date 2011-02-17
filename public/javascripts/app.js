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
  }
});
    });
}
// deprecated jsonp
function vivoWidgetResult(data) {
    if(data.results.length > 0) {
        var resultHtml = ["<h1>Preview Settings</h1><ul>"];
        $.each(data.results, function(i,item) {
            resultHtml.push('<li>' + item.citation + '</li>');
        });
        resultHtml.push('</ul>');
        $("#host").html(resultHtml.join(''));

        $('#loading').hide("drop", { }, 150, function() {
            $('#host').show("drop", { }, 150);

        });
    } else {
        $("#host").html('Move along, there are no results to see here...');
    }

}

function openHelp(helpId) {
    var $dialog = $('<div></div>')
    .html(help[helpId].details)
    .dialog({
        autoOpen: false,
        title: help[helpId].title
    });
    $dialog.dialog('open');
}

$( function() {
    ko.applyBindings(viewModel);

    viewModel.url = ko.dependentObservable( function() {
        latestUrl = 'http://localhost:9000/people/smithjm/publications/' + this.chosenLimit().label;
        latestParams = '?collections='
        + this.chosenCollection().collectionName
        // + '&items=' + this.chosenLimit().label
        + '&formating=' + this.chosenFormat()
        + '&style=' + this.chosenStyle();

        fetchPreview({url: latestUrl, parameters: latestParams});
        var script = '<script type="text/javascript" src="' + latestUrl + '.js' + latestParams + '"> <\/script>';
        $('#embed').val(script);
        //this.latestUrl = latestUrl;
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
})

