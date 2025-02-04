export type ProjectLink = {
  url: string
  type: ProjectLinkType
}

export enum ProjectLinkType {
  GITHUB = 'GITHUB',
  WEBSITE = 'WEBSITE',
  DOCUMENT = 'DOCUMENT',
  OTHER = 'OTHER'
}
