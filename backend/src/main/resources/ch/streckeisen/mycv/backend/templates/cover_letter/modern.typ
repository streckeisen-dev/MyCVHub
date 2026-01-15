#import "@preview/fontawesome:0.5.0": fa-icon

#let default-accent-color = black
#let color-darknight = rgb("#131A28")
#let color-darkgray = rgb("#333333")
#let color-gray = rgb("#5d5d5d")

#let phone-icon = box(fa-icon("square-phone", fill: color-darknight))
#let email-icon = box(fa-icon("envelope", fill: color-darknight))
#let github-icon = box(fa-icon("github", fill: color-darknight))
#let linkedin-icon = box(fa-icon("linkedin", fill: color-darknight))
#let website-icon = box(fa-icon("globe", fill: color-darknight))

#let config = json("config.json")

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

#let coverletter(
  author: (:),
  profile-picture: image,
  date: datetime.today().display("[month repr:long] [day], [year]"),
  accent-color: default-accent-color,
  language: "en",
  font: ("Source Sans Pro", "Source Sans 3"),
  show-footer: true,
  signature: none,
  closing: none,
  paper-size: "a4",
  body,
) = {
  if type(accent-color) == "string" {
    accent-color = rgb(accent-color)
  }

  show: body => context {
    set document(
      author: author.firstName + " " + author.lastName,
      title: if (language == "de") {"Bewerbungsschreiben"} else {"Cover Letter"},
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
    footer: if show-footer [#__coverletter_footer(
        author,
        language,
        date,
        lang_data,
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
  
  show heading: it => [
    #set block(
      above: 1em,
      below: 1em,
    )
    #set text(
      size: 16pt,
      weight: "regular",
    )
    
    #align(left)[
      #text[#strong[#text(accent-color)[#it.body]]]
      #box(width: 1fr, line(length: 100%))
    ]
  ]
  
  let name = {
    align(right)[
      #pad(bottom: 5pt)[
        #block[
          #set text(
            size: 32pt,
            style: "normal",
            font: font,
          )
          #text()[#author.firstName #author.lastName]
        ]
      ]
    ]
  }
  
  let positions = {
    set text(
      black,
      size: 9pt,
      weight: "regular",
    )
    align(right)[
      #smallcaps[
        #author.jobTitle
      ]
    ]
  }
  
  let address = {
    set text(
      size: 9pt,
      weight: "bold",
      fill: color-gray,
    )
    align(right)[
      #if ("address" in author) [
        #author.address
      ]
    ]
  }
  
  let contacts = {
    set box(height: 9pt)
    
    let separator = [  #box(sym.bar.v)  ]
    let author_list = ()

    if ("phone" in author) {
      author_list.push[
        #phone-icon
        #box[#text(author.phone)]
      ]
    }
    if ("email" in author) {
      author_list.push[
        #email-icon
        #box[#link("mailto:" + author.email)[#author.email]]
      ]
    }
    if ("github" in author) {
      author_list.push[
        #github-icon
        #box[#link("https://github.com/" + author.github)[#author.github]]
      ]
    }
    if ("linkedin" in author) {
      author_list.push[
        #linkedin-icon
        #box[
          #link("https://www.linkedin.com/in/" + author.linkedin)[#author.firstName #author.lastName]
        ]
      ]
    }
    if ("website" in author) {
      author_list.push[
        #website-icon
        #box[#link(author.website)[#author.website]]
      ]
    }


    align(right)[
      #set text(
        size: 8pt,
        weight: "light",
        style: "normal",
      )
      #author_list.join(separator)
    ]
  }
  
  let letter-heading = {
    grid(
      columns: (1fr, 2fr),
      rows: (100pt),
      align(left + horizon)[
        #block(
          clip: true,
          stroke: 0pt,
          radius: 2cm,
          width: 4cm,
          height: 4cm,
          if (config.mirrorProfileImage) {
            scale(x:-100%, profile-picture)
          } else {
            profile-picture
          },
        )
      ],
      [
        #name
        #positions
        #address
        #contacts
      ],
    )
  }
  
  let signature = {
    text(weight: "light", if(signature != none) {signature} else { if (language == "de") {"Mit freundlichen Grüssen"} else {"Kind regards"}})
    linebreak()
    linebreak()
    linebreak()
    text(weight: "bold")[#author.firstName #author.lastName]
    linebreak()
    linebreak()
  }
  
  // actual content
  letter-heading
  body
  linebreak()
  signature
  align(bottom,
    closing
  )
}

/// Cover letter heading that takes in the information for the hiring company and formats it properly.
/// - entity-info (content): The information of the hiring entity including the company name, the target (who's attention to), street address, and city
/// - date (date): The date the letter was written (defaults to the current date)
#let hiring-entity-info(
  target: none,
  name: "",
  street: "",
  city: "",
  date: datetime.today().display("[day]. [month repr:long] [year]"),
) = {
  set par(leading: 1em)
  pad(top: 1.5em, bottom: 1.5em)[
    #__justify_align[
      #if(target != none) [#text(weight: "bold", size: 9pt)[#target.firstName #target.lastName]]
    ][
      #text(weight: "light", style: "italic", size: 9pt)[#date]
    ]
    
    #pad(bottom: 0.65em)[
      #text(weight: "regular", fill: color-gray, size: 9pt)[
        #smallcaps[#name] \
        #street \
        #city \
      ]
    ]
  ]
}

/// Letter heading for a given job position and addressee.
/// - job-position (string): The job position you are applying for
/// - salutation (string): field for redefining the salutation
/// - addressee (string): The person you are addressing the letter to
#let letter-heading(job-position: "", salutation: "", addressee: "") = {
  
  underline(evade: false, stroke: 0.5pt, offset: 0.3em)[
    #text(weight: "bold", size: 12pt)[#if (config.language == "de") [Bewerbung als] else [Application as] #job-position]
  ]
  pad(top: 1em, bottom: 1em)[
    #text[
      #salutation #addressee
    ]
  ]
}

/// Cover letter content paragraph. This is the main content of the cover letter.
/// - content (content): The content of the cover letter
#let coverletter-content(content) = {
  pad(top: 0.75em, bottom: 0.75em)[
    #set text(weight: "light")
    #content
  ]
}

#show: coverletter.with(
  author: config.author,
  profile-picture: image("profile.jpg"),
  language: config.language,
  font: "Times New Roman",
  show-footer: false,
  closing: {
    if (config.documents != none) {
      if (config.language == "de") [Anhang:] else [Attached Documents:]
      list(..config.documents.map(d => [#d]))
    }
  }
)

#let companyAddress = config.application.companyAddress.split(", ")
#hiring-entity-info(
  target: if (config.application.contactPerson != none) {config.application.contactPerson} else {none},
  name: config.application.company,
  street: companyAddress.at(0),
  city: companyAddress.at(1),
)

#letter-heading(
  job-position: config.application.jobTitle,
  salutation: config.application.salutation,
  addressee: if (config.application.contactPerson != none) {config.application.contactPerson.lastName} else {config.application.addressee},
)

#for paragraph in config.application.content.split("\n\n") {
  coverletter-content(paragraph)
}