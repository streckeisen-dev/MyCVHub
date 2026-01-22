#import "@preview/fontawesome:0.5.0": fa-icon

#let color-darkgray = rgb("#333333")

#let project_link(projectLink) = [
  #let linkIcon = "link"
  #if (projectLink.type == "GITHUB") {
    linkIcon = "github"
  } else if (projectLink.type == "DOCUMENT") {
    linkIcon = "file"
  } else if (projectLink.type == "WEBSITE") {
    linkIcon = "globe"
  }
  #link(
    projectLink.url,
    [
      #fa-icon(linkIcon, fill: color-darkgray) #projectLink.displayName
    ]
  )
]

#let getTitle(lang, german, english) = {
  if (lang == "de") {
    return german
  } else if (lang == "en") {
    return english
  }
  return ""
}

#let getEntryDescription(description, hasLinks: false) = {
  let listItems = description.split(regex("((^|\n)- )"))
  if (listItems.len() > 1) {
    list(..listItems.slice(1))
  } else {
    description
    if (hasLinks) {
      linebreak()
    }
  }
}