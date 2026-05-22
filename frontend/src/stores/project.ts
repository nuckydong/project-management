import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Workspace, Project, ProjectCreateRequest } from '@/types'
import { listProjects as apiListProjects, createProject as apiCreateProject } from '@/api/project'

export const useProjectStore = defineStore('project', () => {
  const currentWorkspace = ref<Workspace | null>(null)
  const projects = ref<Project[]>([])
  const currentProject = ref<Project | null>(null)

  async function loadProjects(workspaceId?: number) {
    const res = await apiListProjects(workspaceId)
    projects.value = res.data
    return res.data
  }

  async function createProject(data: ProjectCreateRequest) {
    const res = await apiCreateProject(data)
    projects.value.push(res.data)
    return res.data
  }

  function setCurrentProject(project: Project | null) {
    currentProject.value = project
  }

  function setCurrentWorkspace(workspace: Workspace | null) {
    currentWorkspace.value = workspace
  }

  return {
    currentWorkspace,
    projects,
    currentProject,
    loadProjects,
    createProject,
    setCurrentProject,
    setCurrentWorkspace
  }
})
