#import "@preview/modern-cv:0.8.0": *

#let project_link(projectLink) = [
  #let linkIcon = "link"
  #if (projectLink.type == "GITHUB") {
    linkIcon = "github"
  } else if (projectLink.type == "DOCUMENT") {
    linkIcon = "file"
  } else if (projectLink.type == "WEBSITE") {
    linkIcon = "globe"
  }
  #fa-icon(linkIcon, fill: color-darkgray) #link(
    projectLink.url,
    projectLink.displayName,
  )
]

#let profile = json("profile.json")

#show: resume.with(
  author: (
    firstname: profile.firstName,
    lastname: profile.lastName,
    email: profile.email,
    //homepage: "https://streckeisen.dev",
    phone: profile.phone,
    //github: "lstreckeisen",
    birth: profile.birthday,
    //linkedin: "lukas-streckeisen",
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
  accent-color: rgb("#000000")
)

#if (profile.at("bio", default: none) != none) {
  if (profile.language == "de") [
    = Über Mich
  ] else [
    = About Me
  ]
  profile.bio
}

#if (profile.language == "de") [
  = Erfahrung
] else [
  = Experience
]

#if (profile.at("workExperiences", default: none) != none) {
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

#if (profile.language == "de") [
  = Ausbildung
] else [
  = Education
]

#if (profile.at("education", default: none) != none) {
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

#if (profile.language == "de") [
  = Projekte
] else [
  = Projects
]

#if (profile.at("projects", default: none) != none) {
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

#if (profile.at("skills", default: none) != none) {

  if (profile.language == "de") [
    = Fähigkeiten
  ] else [
    = Skills
  ]

  for skills in profile.skills {
    resume-skill-item(skills.skillType, skills.skills)
  }

}

