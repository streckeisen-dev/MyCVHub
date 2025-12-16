import { Route, RouteObject, RouteProps, Routes } from 'react-router-dom'
import { ReactNode } from 'react'
import { ROUTES } from '@/config/RouteTree.tsx'

function toRouteProp(route: RouteObject): RouteProps {
  let base: RouteProps
  if (route.index) {
    base = {
      index: true
    }
  } else {
    base = {}
  }
  if (route.path !== undefined) base.path = route.path
  if (route.element !== undefined) base.element = route.element
  if (route.children) base.children = route.children.map(createRoute)
  return base
}

function createRoute(route: RouteObject): ReactNode {
  return <Route {...toRouteProp(route)} />
}

const routes = ROUTES.map((r) => createRoute(r))

function App() {
  return <Routes>{routes}</Routes>
}

export default App
