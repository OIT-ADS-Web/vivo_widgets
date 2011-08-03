${stylesheets.add("/css/smoothness/jquery-ui-1.8.9.custom.css")}
${stylesheets.add("/css/individual/individual-widget-links.css")}
<script type="text/javascript">
  WidgetConfig = {
    "urlBase" : window.location.protocol + "//" + window.location.host + "/widgets",
    "vivoId"  : "${individual.uri?split('/')?last?js_string}",
    "builderLink"    : function() {
      return this.urlBase + "/builder/" + this.vivoId
    },
    "collectionLink" : function(collection) {
      return '<a href="' + this.urlBase + '/people/' + this.vivoId + '/' + collection + '/5.js" class="mysite">[Add to my web site]</a>'
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
  <ul>
    <li><textarea id="embed"  rows="5" name="embed" ></textarea></li>
    <li><a class="widgetBuilderLink" href="#">Need more options or help?</a></li>
    <li>
      <div id="d_clip_container" style="position:relative">
        <div id="d_clip_button"  class="action">Copy</div>
      </div>
    </li>
  </ul>
</div>

${headScripts.add("/js/individual/ZeroClipboard.js")}
${headScripts.add("https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.10/jquery-ui.min.js")}
${scripts.add("/js/individual/widgetUtils.js")}

