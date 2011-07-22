/**
 * @author Jeremy Bandini
 */

var prefs = new _IG_Prefs();
var userHistory = new SearchHistory("#searchHistory");

var settings = {
    currentTerm : '',
    endPoint : 'http://scholars-test.oit.duke.edu/widgets/search.jsonp?query='
};
function initialize() {

    userHistory.initialize();

    Search.execute(settings.currentTerm);

    // initializeHistory(prefs.getString("searchList"));
    initializeBehaviors();

}

// Commands

var PrefHandler = {

    to_array : function(pref) {
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
    },
    scan : function(term, searchArray) {

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
};

var Search = {
    execute : function(term) {
        if(term.length > 0) {
            startSearchAnimations();
            this.vivo(term);
        }
    },
    vivo : function(search) {
         this.load(settings.endPoint + search + '*');
    },
    load : function(url) {
        var surl = url + '&callback=?';
        $.getJSON(surl);
    }
}



// function executeSearch(term) {
    // if(term.length > 0) {
        // startSearchAnimations();
        // searchVivo(term);
    // }
// 
// }



// function loadData(url) {
// 
    // var surl = url + '&callback=?';
    // $.getJSON(surl);
// 
// }
// 
// function searchVivo(search) {
// 
    // loadData(settings.endPoint + search + '*');
// }





// Objects

function SearchHistory(dom_id) {
    this.dom_id = dom_id;

}

SearchHistory.prototype.initialize = function() {
    var searchTermsArray = PrefHandler.to_array(prefs.getString("searchList"));
    var lastSearch = searchTermsArray.pop();

    if(searchTermsArray.length > 0) {
        if(lastSearch !== "") {

            settings.currentTerm = lastSearch;
        }

    }

   this.updateHistory();
}
SearchHistory.prototype.saveSearch = function(search) {
    
    var search_terms = PrefHandler.to_array(prefs.getString("searchList"));

    var testResult = PrefHandler.scan(search, search_terms);

    if(!testResult.result) {
        search_terms.push(search);
        // 1994 character max!

        prefs.set("searchList", search_terms.join('|'));
    } else {
        prefs.set("searchList", testResult.patchedArray.join('|'));
    }
    settings.currentTerm = search;
}

SearchHistory.prototype.updateHistory = function() {

    $(this.dom_id).html(this.renderSearchList(PrefHandler.to_array(prefs.getString("searchList"))).join(" "));
    
    return false;
}
SearchHistory.prototype.renderSearchList = function(listArray) {
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
SearchHistory.prototype.clearAllHistory = function() {
    prefs.set("searchList", ' ');
    prefs.set("readList", ' ');

    this.updateHistory();
    return false;
}


// Scripts





function sortResults(searchArray, groups) {
    var resultsString = ['<ul id="result_summary_container">'];
    var searchArrayLength = searchArray.length;
    var filteredResults = {};
    if($.isEmptyObject(groups)) {
       
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


    // Bevaviors

function startSearchAnimations() {
    $('#loading').show("slide", {
        direction : "up"
    }, 50);

}

function initializeBehaviors() {

    $('#searchButton').click(function() {

        if($('[name=searchTerm]').val() !== "") {
            Search.execute($('[name=searchTerm]').val());
            userHistory.saveSearch($('[name=searchTerm]').val());
        }

    });
    $('#refreshHistory').click(function() {
        userHistory.updateHistory();

        if($('#history').is(":visible")) {
            $('#history').hide("slide", {
                direction : "up"
            }, 90);
        } else {
            $('#history').show("slide", {
                direction : "up"
            }, 90);
        }

    });
    $('#clearAllHistory').click(function() {
        userHistory.clearAllHistory();
    });
    $('.recent_search').live('click', function() {

        //tabs.setSelectedTab(0);
        Search.execute($(this).html());
        userHistory.saveSearch($(this).html());
        return false;

    });
    $('#search input').bind('keydown', function(e) {
        var code = (e.keyCode ? e.keyCode : e.which);
        if(code == 13) { //Enter keycode
            if($('[name=searchTerm]').val() !== "") {
                Search.execute($('[name=searchTerm]').val());
                userHistory.saveSearch($('[name=searchTerm]').val());
            }
        }

    });
}