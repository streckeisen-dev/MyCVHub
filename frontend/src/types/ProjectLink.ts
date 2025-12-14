export interface ProjectLink {
  url: string
  displayName: string
  type: ProjectLinkType
}

export enum ProjectLinkType {
  GITHUB = 'GITHUB',
  WEBSITE = 'WEBSITE',
  DOCUMENT = 'DOCUMENT',
  OTHER = 'OTHER'
}
