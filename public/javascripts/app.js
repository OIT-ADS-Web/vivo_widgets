function fetchPreview(url) {
    $('#host').hide("drop", {  }, 150, function() {
        $('#loading').show("drop", { }, 150);

        $.getJSON(url + "&callback=?");

    });
}

function vivoWidgetResult(data) {
    if(data.results.length > 0) {
        var resultHtml = ["<ul>"];
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


// var format = [{name: 'abbreviated', value: 'abbr'}, {name: 'detailed', 'detail'}];
var viewModel = {

    personId: 1234,
    chosenCollection: ko.observable(collections),
    chosenLimit: ko.observable(noItems),
    chosenFormat: ko.observable('abbreviated'),
    chosenStyle: ko.observable('yes'),
    latestUrl: ''

};

$(function() {
    ko.applyBindings(viewModel);

    viewModel.url = ko.dependentObservable( function() {
        latestUrl = 'http://localhost:9000/people/smithjm/publications/' + this.chosenLimit().label + '.jsonp?collections=' 
          + this.chosenCollection().collectionName 
          // + '&items=' + this.chosenLimit().label 
          + '&format=' + this.chosenFormat()
          + '&style=' + this.chosenStyle();

        fetchPreview(latestUrl);
        var script = '<script type="text/javascript" src="' + latestUrl + '"> <\/script>';
        $('#embed').val(script);
        //this.latestUrl = latestUrl;
        return latestUrl;
    }, viewModel);
    $('#preview').click( function() {
              fetchPreview(viewModel.url());
        return false;
    });
})

