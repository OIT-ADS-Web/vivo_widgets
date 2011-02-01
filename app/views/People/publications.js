document.write('<ul>')
#{list publications}
  document.write('<li>${_.authors.mkString(", ")} (${_.year}) <a href="${_.vivoUri}">${_.title}</a>.</li>')
#{/list}
document.write('</ul>')
