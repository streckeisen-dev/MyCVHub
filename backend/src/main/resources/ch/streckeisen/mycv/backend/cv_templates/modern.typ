/*
Modified version of the typst modern-cv template: https://github.com/DeveloperPaul123/modern-cv
Changes: same formatting for firstname & lastname, increased skill category column-width, removed parts not needed for MyCVHub (cover letter), read cv details from JSON file
*/

#import "@preview/fontawesome:0.5.0": fa-icon
#import "shared.typ": project_link

#let color-darknight = rgb("#131A28")
#let color-darkgray = rgb("#333333")
#let color-gray = rgb("#5d5d5d")
#let default-accent-color = rgb("#262F99")
#let default-location-color = rgb("#333333")

#let phone-icon = box(fa-icon("square-phone", fill: color-darknight))
#let email-icon = box(fa-icon("envelope", fill: color-darknight))
#let birth-icon = box(fa-icon("cake", fill: color-darknight))
#let homepage-icon = box(fa-icon("home", fill: color-darknight))
#let website-icon = box(fa-icon("globe", fill: color-darknight))

#let profile = json("profile.json")

#let __justify_align(left_body, right_body) = {
  block[
    #left_body
    #box(width: 1fr)[
      #align(right)[
        #right_body
      ]
    ]
  ]
}

/// Right section for the justified headers
/// - body (content): The body of the right header
#let secondary-right-header(body) = {
  set text(
    size: 11pt,
    weight: "medium",
  )
  body
}

/// Right section of a tertiaty headers.
/// - body (content): The body of the right header
#let tertiary-right-header(body) = {
  set text(
    weight: "light",
    size: 9pt,
  )
  body
}

/// Justified header that takes a primary section and a secondary section. The primary section is on the left and the secondary section is on the right.
/// - primary (content): The primary section of the header
/// - secondary (content): The secondary section of the header
#let justified-header(primary, secondary) = {
  set block(
    above: 0.7em,
    below: 0.7em,
  )
  pad[
    #__justify_align[
      == #primary
    ][
      #secondary-right-header[#secondary]
    ]
  ]
}

/// Justified header that takes a primary section and a secondary section. The primary section is on the left and the secondary section is on the right. This is a smaller header compared to the `justified-header`.
/// - primary (content): The primary section of the header
/// - secondary (content): The secondary section of the header
#let secondary-justified-header(primary, secondary) = {
  __justify_align[
    === #primary
  ][
    #tertiary-right-header[#secondary]
  ]
}

#let __resume_footer(author, language, date) = {
  set text(
    fill: gray,
    size: 8pt,
  )
  __justify_align_3[
    #smallcaps[#date]
  ][
    #smallcaps[
      #author.firstname#sym.space#author.lastname
      #sym.dot.c
      #if (language == "de") { "Lebenslauf" } else { "Résumé" }
    ]
  ][
    #context {
      counter(page).display()
    }
  ]
}

#let resume(
  author: (:),
  profile-picture: image,
  date: datetime.today().display("[month repr:long] [day], [year]"),
  accent-color: default-accent-color,
  colored-headers: true,
  show-footer: true,
  language: "en",
  font: ("Source Sans Pro", "Source Sans 3"),
  header-font: ("Roboto"),
  paper-size: "a4",
  body,
) = {
  if type(accent-color) == "string" {
    accent-color = rgb(accent-color)
  }

  show: body => context {
    set document(
      author: author.firstname + " " + author.lastname,
      title: if (language == "de") {"Lebenslauf"} else {"Résumé"},
    )
    body
  }

  set text(
    font: font,
    lang: language,
    size: 11pt,
    fill: color-darkgray,
    fallback: true,
  )
  
  set page(
    paper: paper-size,
    margin: (left: 15mm, right: 15mm, top: 10mm, bottom: 10mm),
    footer: if show-footer [#__resume_footer(
        author,
        language,
        date,
      )] else [],
    footer-descent: 0pt,
  )
  
  // set paragraph spacing
  set par(
    spacing: 0.75em,
    justify: true,
  )
  
  set heading(
    numbering: none,
    outlined: false,
  )
  
  show heading.where(level: 1): it => [
    #set text(
      size: 16pt,
      weight: "regular",
    )
    #set align(left)
    #set block(above: 1em)
    #let color = if colored-headers {
      accent-color
    } else {
      color-darkgray
    }
    #text[#strong[#text(color)[#it.body]]]
    #box(width: 1fr, line(length: 100%))
  ]
  
  show heading.where(level: 2): it => {
    set text(
      color-darkgray,
      size: 12pt,
      style: "normal",
      weight: "bold",
    )
    it.body
  }
  
  show heading.where(level: 3): it => {
    set text(
      size: 10pt,
      weight: "regular",
    )
    smallcaps[#it.body]
  }
  
  let name = {
    align(center)[
      #pad(bottom: 5pt)[
        #block[
          #set text(
            size: 32pt,
            style: "normal",
            font: header-font,
          )
          #text(accent-color, weight: "thin")[#author.firstname #author.lastname]
        ]
      ]
    ]
  }
  
  let positions = {
    set text(
      accent-color,
      size: 9pt,
      weight: "regular",
    )
    align(center)[
      #smallcaps[
        #author.positions.join(
          text[#"  "#sym.dot.c#"  "],
        )
      ]
    ]
  }
  
  let address = {
    set text(
      size: 9pt,
      weight: "regular",
    )
    align(center)[
      #if ("address" in author) [
        #author.address
      ]
    ]
  }
  
  let contacts = {
    set box(height: 9pt)
    
    let separator = box(width: 5pt)
    
    align(center)[
      #set text(
        size: 9pt,
        weight: "regular",
        style: "normal",
      )
      #block[
        #align(horizon)[
          #if ("birth" in author) [
            #birth-icon
            #box[#text(author.birth)]
            #separator
          ]
          #if ("phone" in author) [
            #phone-icon
            #box[#text(author.phone)]
            #separator
          ]
          #if ("email" in author) [
            #email-icon
            #box[#link("mailto:" + author.email)[#author.email]]
          ]
          #if ("homepage" in author) [
            #separator
            #homepage-icon
            #box[#link(author.homepage)[#author.homepage]]
          ]
          #if ("github" in author) [
            #separator
            #github-icon
            #box[#link("https://github.com/" + author.github)[#author.github]]
          ]
          #if ("linkedin" in author) [
            #separator
            #linkedin-icon
            #box[
              #link("https://www.linkedin.com/in/" + author.linkedin)[#author.firstname #author.lastname]
            ]
          ]
          #if ("twitter" in author) [
            #separator
            #twitter-icon
            #box[#link("https://twitter.com/" + author.twitter)[\@#author.twitter]]
          ]
          #if ("scholar" in author) [
            #let fullname = str(author.firstname + " " + author.lastname)
            #separator
            #google-scholar-icon
            #box[#link("https://scholar.google.com/citations?user=" + author.scholar)[#fullname]]
          ]
          #if ("orcid" in author) [
            #separator
            #orcid-icon
            #box[#link("https://orcid.org/" + author.orcid)[#author.orcid]]
          ]
          #if ("website" in author) [
            #separator
            #website-icon
            #box[#link(author.website)[#author.website]]
          ]
        ]
      ]
    ]
  }
  
  if profile-picture != none {
    grid(
      columns: (100% - 4cm, 4cm),
      rows: (100pt),
      gutter: 10pt,
      [
        #name
        #positions
        #address
        #contacts
      ],
      align(left + horizon)[
        #block(
          clip: true,
          stroke: 0pt,
          radius: 2cm,
          width: 4cm,
          height: 4cm,
          profile-picture,
        )
      ],
    )
  } else {
    name
    positions
    address
    contacts
  }

  body

}

#let resume-item(body) = {
  set text(
    size: 10pt,
    style: "normal",
    weight: "light",
    fill: color-darknight,
  )
  set block(
    above: 0.75em,
    below: 1.25em,
  )
  set par(leading: 0.65em)
  block(above: 0.5em)[
    #body
  ]
}

#let resume-entry(
  title: none,
  location: "",
  date: "",
  description: "",
  title-link: none,
  accent-color: default-accent-color,
  location-color: default-location-color,
) = {
  let title-content
  if type(title-link) == "string" {
    title-content = link(title-link)[#title]
  } else {
    title-content = title
  }
  block(above: 1em, below: 0.65em)[
    #pad[
      #justified-header(title-content, location)
      #if description != "" or date != "" [
        #secondary-justified-header(description, date)
      ]
    ]
  ]
}

#let resume-skill-item(category, items) = {
  set block(below: 0.65em)
  set pad(top: 2pt)
  
  pad[
    #grid(
      columns: (30fr, 70fr),
      gutter: 10pt,
      align(left)[
        #set text(hyphenate: false)
        == #category
      ],
      align(left)[
        #set text(
          size: 11pt,
          style: "normal",
          weight: "light",
        )
        #items.join(", ")
      ],
    )
  ]
}

#resume(
  author: (
    firstname: profile.firstName,
    lastname: profile.lastName,
    email: profile.email,
    //homepage: "",
    phone: profile.phone,
    //github: "",
    birth: profile.birthday,
    //linkedin: "",
    address: profile.address,
    positions: (
      profile.jobTitle,
    ),
  ),
  profile-picture: image("profile.jpg"),
  header-font: "Libertinus Serif",
  font: "Libertinus Serif",
  date: datetime.today().display(),
  language: profile.language,
  colored-headers: false,
  show-footer: false,
  accent-color: rgb("#000000"),
  [
    #if (profile.at("bio", default: none) != none) {
      if (profile.language == "de") [
        = Über Mich
      ] else [
        = About Me
      ]
      profile.bio
    }

    #if (profile.at("workExperiences", default: none) != none and profile.workExperiences.len() > 0) {
      if (profile.language == "de") [
        = Erfahrung
      ] else [
        = Experience
      ]

      for experience in profile.workExperiences [
        #resume-entry(
          title: experience.title,
          location: experience.location,
          date: [#experience.startDate - #experience.endDate],
          description: experience.institution,
        )

        #if (experience.at("description", default: none) != none) [
          #resume-item[#experience.description]
        ]
      ]
    }

    #if (profile.at("education", default: none) != none and profile.education.len() > 0) {
      if (profile.language == "de") [
        = Ausbildung
      ] else [
        = Education
      ]

      for edu in profile.education [
        #resume-entry(
          title: edu.title,
          location: edu.location,
          date: [#edu.startDate - #edu.endDate],
          description: edu.institution
        )

        #if (edu.at("description", default: none) != none) [
          #resume-item[#edu.description]
        ]
      ]
    }

    #if (profile.at("projects", default: none) != none and profile.projects.len() > 0) {
      if (profile.language == "de") [
        = Projekte
      ] else [
        = Projects
      ]

      for project in profile.projects [
        #resume-entry(
          title: project.title,
          location: [
            #if (project.at("links", default: none) != none) {
              for link in project.links {
                project_link(link)
              }
            }
          ],
          date: [#project.startDate - #project.endDate],
          description: project.institution
        )

        #if (project.at("description", default: none) != none) [
          #resume-item[#project.description]
        ]
      ]
    }

    #if (profile.at("skills", default: none) != none and profile.skills.len() > 0) {
      if (profile.language == "de") [
        = Fähigkeiten
      ] else [
        = Skills
      ]

      for skills in profile.skills {
        resume-skill-item(skills.skillType, skills.skills)
      }

    }
])