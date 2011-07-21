/**
 * @author Jeremy Bandini
 */

var prefs = new _IG_Prefs();
var settings = {
    currentTerm : '',
    endPoint : 'http://scholars-test.oit.duke.edu/widgets/search.jsonp?query='
};
function initialize() {

    var search_terms = prefs.getString("searchList");
    var searchTermsArray = search_terms.split('|');
    var lastSearch = searchTermsArray.pop();

    if(searchTermsArray.length > 0) {
        if(lastSearch !== "") {
            executeSearch(lastSearch);
            settings.currentTerm = lastSearch;
        }

    }

    // Bevaviors
    $('#searchButton').click(function() {

        if($('[name=searchTerm]').val() !== "") {
            executeSearch($('[name=searchTerm]').val());
            saveSearch($('[name=searchTerm]').val());
        }

    });
    $('#showSearch').click(function() {
        stopSearchAnimations();
    });
    $('#hideSearch').click(function() {
        $('#searchContainer').hide("fold", {
            direction : "up"
        }, 150, function() {
            // $('#showSearch').show("slide", {
            // direction: "up"
            // }, 150);

        });
    });
    $('#refreshHistory').click(function() {
        updateHistory();
        // $('#search').hide("slide", {
        // direction: "right"
        // }, 150, function() {
        if($('#history').is(":visible")) {
            $('#history').hide("fold", {
                direction : "up"
            }, 150);
        } else {
            $('#history').show("fold", {
                direction : "down"
            }, 150);
        }

    });
    // });
    // $('#hideHistory').click( function() {
    //
    // $('#history').hide("fold", {
    // direction: "up"
    // }, 150, function() {
    // // $('#search').show("slide", {
    // // direction: "right"
    // // }, 150);
    //
    // });
    // });
    $('#clearAllHistory').click(function() {
        clearAllHistory();
    });
    // $('.read_list').live('change', function() {
    //
    // if($(this).is(':checked') && searchReadList($(this).prev().attr('href')) === false) {
    //
    // $(this).prev().removeClass('unread');
    // $(this).attr("disabled", true);
    // var read_list = prefs.getString("readList");
    //
    // prefs.set("readList", read_list + '|' +  $(this).prev().attr('href'));
    //
    // }
    //
    // });
    $('.recent_search').live('click', function() {

        //tabs.setSelectedTab(0);
        executeSearch($(this).html());
        saveSearch($(this).html());
        return false;

    });
    $('#search input').bind('keydown', function(e) {
        var code = (e.keyCode ? e.keyCode : e.which);
        if(code == 13) { //Enter keycode
            if($('[name=searchTerm]').val() !== "") {
                executeSearch($('[name=searchTerm]').val());
                saveSearch($('[name=searchTerm]').val());
            }
        }

    });
    updateHistory();

}

function executeSearch(term) {
    startSearchAnimations();
    searchVivo(term);

}

function startSearchAnimations() {
    $('#loading').show("slide", {
        direction : "up"
    }, 50);
    // $('#searchContainer').hide("fold", {
    // direction: "up"
    // }, 150, function() {
    // // $('#showSearch').show("slide", {
    // // direction: "up"
    // // }, 150);
    //
    // });
}

function stopSearchAnimations() {
    // $('#showSearch').hide("slide", {
    // direction: "up"
    // }, 150, function() {
    // $('#searchContainer').show("fold", {
    // direction: "up"
    // }, 150);
    // });
}

function loadData(url) {

    var surl = url + '&callback=?';
    $.getJSON(surl);

}

function scanForDuplicateSearch(term, searchArray) {
    var searchArrayLength = searchArray.length;
    for(var i = 0; i < searchArrayLength; i++) {
        if(searchArray[i] === term) {
            var caughtSearch = searchArray.splice(i, 1);
            searchArray.push(caughtSearch);
            return {
                result : true,
                patchedArray : searchArray
            }
        }
    }

    return {
        result : false,
        patchedArray : undefined
    };
}

function saveSearch(search) {
    item = search;

    var search_terms = prefToArray(prefs.getString("searchList"));
    // var searchArray = search_terms.split('|');
    var testResult = scanForDuplicateSearch(item, search_terms);
    if(!testResult.result) {
        search_terms.push(item);
        // 1994 character max!

        prefs.set("searchList", search_terms.join('|'));
    } else {
        prefs.set("searchList", testResult.patchedArray.join('|'));
    }
    settings.currentTerm = item;
}

function searchVivo(search) {

    loadData(settings.endPoint + search + '*');
}

// function searchReadList(term) {
// var read_list = prefs.getString("readList");
// var readArray = read_list.split('|');
// var loopCount = readArray.length;
// for(var i=0; i < loopCount; i++) {
// if (readArray[i] === term) {
// return true;
// }
// }
// return false;
// }

function sortResults(searchArray, groups) {
    var resultsString = ['<ul id="result_summary_container">'];
    var searchArrayLength = searchArray.length;
    var filteredResults = {};
    if($.isEmptyObject(groups)) {
        console.log(groups);
        return searchArray;
    }
    for(var key in groups) {
        //var obj = groups[key];

        // alert(prop + " = " + obj[prop]);
        filteredResults[key] = [{
            uri : '#',
            name : key
        }];
        resultsString.push('<li><a href="#' + key + '">' + key + ': ' + groups[key] + '</a></li>');
    }
    resultsString.push("</ul>");
    for(var i = 0; i < searchArrayLength; i++) {
        //console.log(searchArray[i].group);
        if(filteredResults[searchArray[i].group]) {
            filteredResults[searchArray[i].group].push(searchArray[i]);
        }

    }
    var sortedResults = [{
        uri : '#result',
        name : resultsString.join(" ")
    }];

    for(var key in groups) {
        //	var obj = groups[key];

        // alert(prop + " = " + obj[prop]);
        sortedResults = sortedResults.concat(filteredResults[key]);

    }
    //var sortedResults = filteredResults.publications.concat(filteredResults.activities,filteredResults.organizations, filteredResults.people, filteredResults.locations);

    return sortedResults;
}

function vivoSearchResult(data) {
    $('#loading').hide("slide", {
        direction : "up"
    }, 150, function() {
        if(!$('#results').is(":visible")) {
            $('#results').show("slide", {
                direction : "up"
            }, 150);
        }

    });
    //console.profile()
    var search_terms = prefs.getString("searchList");
    var searchArray = search_terms.split('|');

    var groups = data.groups;
    var finalArray = sortResults(data.items, groups);
    var dataItems = finalArray.length;

    var finalStr = [];

    finalStr.push('<li id="results_header"><strong>' + settings.currentTerm + '</strong></li><li>Showing <strong>' + dataItems + '</strong> results</li>');

    for(var i = 0; i < dataItems; i++) {
        if(finalArray[i].uri !== '#' && finalArray[i].uri !== '#result') {

            // if(!searchReadList(finalArray[i].uri)) {
            finalStr.push('<li><a target="_blank" href="' + finalArray[i].uri + '">' + finalArray[i].name + '</a></li>');
            // } else {
            // finalStr.push('<li><a target="_blank" href="' + finalArray[i].uri + '">' + finalArray[i].name + '</a><input class="read_list" checked="true" disabled="true" type="checkbox" /></li>')
            // }
        } else {
            if(finalArray[i].uri === '#result') {
                finalStr.push('<li class="result_summary">' + finalArray[i].name + '</li>');
            } else {
                finalStr.push('<li id="' + finalArray[i].name + '" class="group">' + finalArray[i].name + '</li>');
            }

        }

    };
    $('#results').html(finalStr.join(" "));
    //console.profileEnd()

}

function prefToArray(pref) {
    var prefArray = pref.split('|');
    var finalArray = [];
    var prefLength = prefArray.length;
    for(var i = 0; i < prefLength; i++) {

        if(prefArray[i] !== " ") {
            //alert(prefArray[i].charCodeAt(0));
            finalArray.push(prefArray[i]);
        }
    }

    return finalArray;
}

function renderSearchList(listArray) {
    finalHtml = ["<ul>"];
    var listArrayLength = listArray.length;
    for(var i = 0; i < listArrayLength; i++) {
        if(listArray[i]) {
            finalHtml.push('<li><a class="recent_search" href="' + settings.endPoint + listArray[i] + '*">' + listArray[i] + '</a></li>');
            console.log("<li>" + listArray[i] + "</li>");
        }

    }
    finalHtml.push("</ul>");

    return finalHtml;
}

function updateHistory() {

    $('#searchHistory').html(renderSearchList(prefToArray(prefs.getString("searchList"))).join(" "));
    $('#readHistory').html('<h4>Read Item URLs</h4>' + prefToArray(prefs.getString("readList")).join('<br />'));
    return false;
}

function clearAllHistory() {
    prefs.set("searchList", ' ');
    prefs.set("readList", ' ');

    updateHistory();
    return false;
}