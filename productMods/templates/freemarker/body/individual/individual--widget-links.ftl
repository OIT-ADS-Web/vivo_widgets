${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual-widget-links.css" />')}

<script type="text/javascript">
  WidgetConfig = {
    "urlBase" : window.location.protocol + "//" + window.location.host + "/widgets",
    "builderLink"    : function() {
      return this.urlBase + "/builder?uri=${individual.uri}"
    },
    "collectionLink" : function(collection) {
      return '<a href="' + this.urlBase + '/people/' + collection + '/5.js?uri=${individual.uri}" class="mysite" collection="' + collection + '">[Add to my web site]</a>'
    },
    "collectionLinkMap" : {
       "h3#authorInAuthorship" : "publications",
       "h3#hasResearcherRole"  : "grants",
       "h3#hasPrincipalInvestigatorRole"  : "grants",
       "h3#hasTeacherRole"     : "courses"
    }
  }
</script>
<a class="widgetBuilderLink" href="#">[Add data from this page to my web site]</a>
<div id="modal">
  <a id="close" href="#">X</a>
  <h5 id="embedDescription"></h5>
  <ul>
    <li><textarea id="embed"  rows="5" name="embed" ></textarea></li>
    <li>
      <div id="d_clip_container" style="position:relative">
        <div id="d_clip_button"  class="action">Copy</div>
      </div>
    </li>
    <li><a class="widgetBuilderLink" href="#">Need more options?</a></li>
  </ul>
</div>

${headScripts.add('<script type="text/javascript" src="${urls.base}/js/individual/ZeroClipboard.js"></script>',
                  '<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.10/jquery-ui.min.js"></script>')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/individual/widgetUtils.js"></script>')}

