/**
 * @author Jeremy Bandini
 */

var prefs = new _IG_Prefs();
var userHistory = new SearchHistory("#searchHistory");
var results = new Results();

var settings = {
    currentTerm : '',
    endPoint : 'http://scholars-test.oit.duke.edu/widgets/search.jsonp?query='
};

function initialize() {
    userHistory.initialize();
    Search.execute(settings.currentTerm);
    Behaviors.initialize();
}

var Template = {
    results_header : function(term, count) {
        return '<li id="results_header"><strong>' + term + '</strong></li><li>Showing <strong>' + count + '</strong> results</li>';
    },
    results_item : function(uri, name) {
        return '<li><a target="_blank" href="' + uri + '">' + name + '</a></li>';
    },
    results_group_summary : function(name) {
        return '<li class="result_summary">' + name + '</li>';
      
    },
    results_group_header : function(name) {
        return '<li id="' + name + '" class="group">' + name + '</li>';
    },
    results_submenu : function(name, count) {
        return '<li><a href="#' + name + '">' + name + ': ' + count + '</a></li>';
    },
    history_menu : function(url, term) {
        return '<li><a class="recent_search" href="' + url + term + '*">' + term + '</a></li>'
      
    }
    
}


// Commands
var Prefs = {
        // history : {
            // searchList: "cat|dog|obesity|food|pharm"
        // },
        set : function (pref, new_val) {
            prefs.set(pref, new_val);
           //this.history[pref] = new_val;
        },
        getString : function(pref) {
            return prefs.getString(pref);
            //return this.history[pref];
           }
}

var PrefHandler = {

    to_array : function(pref) {
        var prefArray = pref.split('|');
        var finalArray = [];
        var prefLength = prefArray.length;
        for(var i = 0; i < prefLength; i++) {
            if(prefArray[i] !== " ") {
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
            $('[name=searchTerm]').val(term);
            Behaviors.history_hide();
            Behaviors.start_loading();
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


// Objects
function Results() {
    this.groups = {};
}
Results.prototype.sort = function(searchArray) {
    var resultsString = ['<ul id="result_summary_container">'];
    var searchArrayLength = searchArray.length;
    var filteredResults = {};
    if($.isEmptyObject(this.groups)) {
       
        return searchArray;
    }
    for(var key in this.groups) {
        filteredResults[key] = [{
            uri : '#',
            name : key
        }];
        resultsString.push(Template.results_submenu(key, this.groups[key]));
       
    }
    resultsString.push("</ul>");
    for(var i = 0; i < searchArrayLength; i++) {

        if(filteredResults[searchArray[i].group]) {
            filteredResults[searchArray[i].group].push(searchArray[i]);
        }

    }
    var sortedResults = [{
        uri : '#result',
        name : resultsString.join(" ")
    }];

    for(var key in this.groups) {
        sortedResults = sortedResults.concat(filteredResults[key]);

    }
   
    return sortedResults;
}
Results.prototype.render = function(data) {
    Behaviors.stop_loading();

    results.groups = data.groups;
   
    $('#results').html(this.html(results.sort(data.items)).join(" "));
}
Results.prototype.html  = function(sorted_data) {
    var html = [];
    var count = sorted_data.length;
    console.log("rendering current: " + settings.currentTerm);
    html.push(Template.results_header(settings.currentTerm, count));
    
    for(var i = 0; i < count; i++) {
        if(sorted_data[i].uri !== '#' && sorted_data[i].uri !== '#result') {
            html.push(Template.results_item(sorted_data[i].uri, sorted_data[i].name));
        } else {
            if(sorted_data[i].uri === '#result') {
                html.push(Template.results_group_summary(sorted_data[i].name));
                
            } else {
                html.push(Template.results_group_header(sorted_data[i].name));
            }

        }

    };
    return html;
}

function SearchHistory(dom_id) {
    this.dom_id = dom_id;

}

SearchHistory.prototype.initialize = function() {
    var searchTermsArray = PrefHandler.to_array(Prefs.getString("searchList"));
       if(searchTermsArray.length > 0) {
    var lastSearch = searchTermsArray.pop();
  

        console.log("setting current: " + lastSearch);
        if(lastSearch !== "") {
            
            settings.currentTerm = lastSearch;
           
        }

    }

   this.updateHistory();
}
SearchHistory.prototype.saveSearch = function(search) {
    
    var search_terms = PrefHandler.to_array(Prefs.getString("searchList"));

    var testResult = PrefHandler.scan(search, search_terms);

    if(!testResult.result) {
        search_terms.push(search);
        // 1994 character max!

        Prefs.set("searchList", search_terms.join('|'));
    } else {
        Prefs.set("searchList", testResult.patchedArray.join('|'));
    }
    settings.currentTerm = search;
    console.log("saving current: " + settings.currentTerm);
}

SearchHistory.prototype.updateHistory = function() {
    $(this.dom_id).html(this.renderSearchList(PrefHandler.to_array(Prefs.getString("searchList"))).join(" "));
    
    return false;
}
SearchHistory.prototype.renderSearchList = function(listArray) {
    finalHtml = ["<ul>"];
    var listArrayLength = listArray.length;
    for(var i = 0; i < listArrayLength; i++) {
        if(listArray[i]) {
            finalHtml.push(Template.history_menu(settings.endPoint, listArray[i]));
           
        }

    }
    finalHtml.push("</ul>");

    return finalHtml;
}
SearchHistory.prototype.clearAllHistory = function() {
    Prefs.set("searchList", ' ');
  

    this.updateHistory();
    return false;
}

    // Bevaviors

var Behaviors = {
    initialize : function() {
        that = this;
        $('#searchButton').click(function() {
            that.search();
    
        });
        $('#refreshHistory').click(function() {
           that.history();
    
        });
        $('#clearAllHistory').click(function() {
           that.clear_history();
        });
        $('.recent_search').live('click', function() {
    
            return that.select_history_item(this);
    
        });
        $('#search input').bind({      
            'keydown' : function(e) {
                that.keyboard(e);
            },
            focus : function() {
                Behaviors.history_hide();
            }
        });

    },
    search : function() {   
        term = $('[name=searchTerm]').val();
        if(term !== "") {
            Search.execute(term);
            userHistory.saveSearch(term);
        }
    },
    history_hide : function() {
          if($('#history').is(":visible")) {
              $('#recent_img').attr("src","http://gadgets-dev.oit.duke.edu/vivo_social/recent.gif");
             $('#history').hide("slide", {
                    direction : "up"
             }, 90);
            }
    },
    history : function() {
         userHistory.updateHistory();

        if($('#history').is(":visible")) {
           this.history_hide();
        } else {
            $('#recent_img').attr("src","http://gadgets-dev.oit.duke.edu/vivo_social/recent-on.gif");
            $('#history').show("slide", {
                direction : "up"
            }, 90);
        }
    },
    clear_history : function() {
        userHistory.clearAllHistory();
    },
    select_history_item : function(caller) {
       
        Search.execute($(caller).html());
        userHistory.saveSearch($(caller).html());
        return false;
    },
    keyboard : function(e) {
        var code = (e.keyCode ? e.keyCode : e.which);
        if(code == 13) { 
            if($('[name=searchTerm]').val() !== "") {
                Search.execute($('[name=searchTerm]').val());
                userHistory.saveSearch($('[name=searchTerm]').val());
            }
        }
    },
    start_loading: function() {
        $('#loading').show("slide", {
            direction : "up"
        }, 50);
    },
    stop_loading: function() {
     $('#loading').hide("slide", {
        direction : "up"
         }, 150, function() {
        if(!$('#results').is(":visible")) {
            $('#results').show("slide", {
                direction : "up"
            }, 150);
        }

    });
    }
}
// Script
function vivoSearchResult(data) {
    results.render(data);

}

