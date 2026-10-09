export interface AdminDashboardMetricPointDto {
  date: string
  value: number
}

export interface AdminDashboardDto {
  totalRegisteredUsers: number
  dailyActiveUsers: number
  weeklyActiveUsers: number
  monthlyActiveUsers: number
  signupSeries: AdminDashboardMetricPointDto[]
  activeUserSeries: AdminDashboardMetricPointDto[]
  verifiedAccountCount: number
  unverifiedAccountCount: number
  oauthSignupCount: number
  passwordSignupCount: number
  asOf: string
}
