#import "@preview/fontawesome:0.5.0": fa-icon
#import "shared.typ": project_link, getTitle, getEntryDescription

#set page(
  margin: (x: 5mm, y: 1cm),
  background: place(start, grid(
    columns: (7.5cm, auto),
    block(fill: black, height: 100%, width: 100%)
  ))
)
#set text(font: "Helvetica", size: 10pt)

#let personalInfoBackgroundColor = rgb(0, 0, 0)

#let profile = json("profile.json")

#let section-header(icon, title) = {
  let cells = (
    [
      #block(
        stroke: black,
        width: 25pt,
        height: 25pt,
        radius: 12.5pt,
        [
          #set align(center + horizon)
          #fa-icon(icon, size: 12pt)
        ]
      )
    ],
    [
      #pad(top: 7pt)[
        == #title
      ]
    ]
  )

  return grid.header(
    ..cells
  )
}

#let timeline-entry(startDate, endDate, institution, location, title, description, links: ()) = {
  let lines = 3
  let hasLinks = links != none and links.len() > 0
  let content = grid(
    columns: (2cm, auto),
    [#startDate - #endDate],
    [
      #text(weight: "bold")[#institution, #location]#linebreak()
      #title #linebreak()
      #if (description != none) {
        lines += (description.len() / 55)
        getEntryDescription(description, hasLinks: hasLinks)
      }
      #if (links != none and links.len() > 0) {
        for link in links {
          project_link(link)
          h(5pt)
        }
      }
    ]
  )
  let linkBuffer = 0pt
  if (hasLinks) {
    linkBuffer = 14pt
  }

  return (
    pad(left: 0.34cm,
      stack(
        pad(left: 0.7mm)[
          #rect(width: 0.4mm, height: 7pt, fill: black)
        ],
        // Circle marker (hollow)
        circle(
          radius: 1mm,
          stroke: black,
          fill: none
        ),
        // Spacer below the circle so the line begins below it
        pad(left: 0.7mm)[
          #rect(width: 0.4mm, height: (lines * (11pt + 3pt) + linkBuffer), fill: black)
        ]
      
      )
    ),
    block(content, breakable: false)
  )
}

#let skill-entry(skillType, skills) = {
  return (
    [
      #pad(left: 1.28cm,
        text(weight: "bold", skillType)
      )
      
    ],
    [
      #skills.join(", ")
    ]
  )
}

#let education-section() = {
  if (profile.education.len() == 0) {
    return
  }
  grid(
      columns: (1.2cm, auto),
      section-header("book", getTitle(profile.language, "Ausbildung", "Education")),
      // header
      [
        #pad(left: 0.42cm, top: 3pt)[
          #rect(width: 0.4mm, height: 3mm, fill: black)
        ]
      ],
      [],
      // end header

      let eduContent = (),
      for edu in profile.education {
        eduContent.push(timeline-entry(edu.startDate, edu.endDate, edu.institution, edu.location, edu.title, edu.description))
      },
      ..eduContent.flatten()
    )
}

#let work-experience-section() = {
  if (profile.workExperiences.len() == 0) {
    return
  }

  grid(
      columns: (1.2cm, auto),
      section-header("pen", getTitle(profile.language, "Berufserfahrung", "Work Experience")),
      // header
      [
        #pad(left: 0.42cm, top: 3pt)[
          #rect(width: 0.4mm, height: 3mm, fill: black)
        ]
      ],
      [],
      // end header

      let workContent = (),
      for work in profile.workExperiences {
        workContent.push(timeline-entry(work.startDate, work.endDate, work.institution, work.location, work.title, work.description))
      },
      ..workContent.flatten()
    )
}

#let projects-section() = {
  if (profile.projects.len() == 0) {
    return
  }

  // workaround to prevent the grid header from standing alone on the bottom of the page
  block(breakable: false, v(6cm))
  v(-6cm)

  grid(
      columns: (1.2cm, auto),
      section-header("gears", getTitle(profile.language, "Projekte", "Projects")),
      // header
      [
        #pad(left: 0.42cm, top: 3pt)[
          #rect(width: 0.4mm, height: 3mm, fill: black)
        ]
      ],
      [],
      // end header

      let projectContents = (),
      for proj in profile.projects {
        let projectContent = timeline-entry(proj.startDate, proj.endDate, proj.institution, proj.location, proj.title, proj.description, links: proj.links)
        projectContents.push(projectContent)
      },
      ..projectContents.flatten()
    )
}

#let skills-section() = {
  if (profile.at("skills", default: none) == none or profile.skills.len() == 0) {
    return
  }

  grid(
    columns: (1.2cm, auto),
    section-header("toolbox", getTitle(profile.language, "Fähigkeiten", "Skills")),
  )
  grid(
    columns: 2,
    gutter: 3mm,
    let skillContent = (),
    for skills in profile.skills {
      skillContent.push(skill-entry(skills.skillType, skills.skills))
    },
    ..skillContent.flatten()
  )
}

#grid(
  columns: (7cm, auto),
  column-gutter: 15pt,
  row-gutter: 0pt,
  stroke: none,
  [
    #block(
      height: 100%,
      width: 100%,
      [
        #set align(left + top)
        #set text(fill: white)
        #pad(left: 1cm, right: 10pt, bottom: 1cm, [
          #block(
            clip: true,
            stroke: (thickness: 2pt, paint: white),
            radius: 2.25cm,
            width: 4.5cm,
            height: 4.5cm,
            image("profile.jpg", width: 4.5cm)
          )
          
          #v(25pt)

          #let personalDetailsTitle = getTitle(profile.language, "Persönliche Angaben", "Personal Details")

          #text(weight: "bold", size: 13pt, personalDetailsTitle)
          #grid(
            columns: 2,
            column-gutter: 15pt,
            row-gutter: 20pt,
            box(fa-icon("user", size: 15pt)),
            [#profile.firstName #profile.lastName],
            box(fa-icon("map-marker", size: 15pt)),
            [
              #let addressParts = profile.address.split(",")
              #addressParts.at(0) #linebreak()
              #addressParts.at(1)
            ],
            box(fa-icon("phone", size: 15pt)),
            [#profile.phone],
            box(fa-icon("envelope", size: 15pt)),
            [#profile.email],
            box(fa-icon("birthday-cake", size: 15pt)),
            [#profile.birthday]
          )
          
          #v(25pt)


          #let aboutMeTitle = getTitle(profile.language, "Über mich", "About me")
          #text(weight: "bold", size: 13pt, aboutMeTitle)#linebreak()
          #v(5pt)
          #profile.bio
        ])
      ]
    )   
  ],
  [
    #text(weight: "bold", size: 30pt)[#upper(profile.firstName + " " + profile.lastName)] #linebreak()
    #v(1pt)
    #text(size: 15pt)[#upper(profile.jobTitle)]
    
    #education-section()
    #work-experience-section()
    #projects-section()
    #block(breakable: false,
      skills-section()
    )
  ]
)