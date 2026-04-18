import { ContributionBoard, RoleType } from "~/common/contribution/ContributionBoard";

export function ProjectContributions({ projectId }: { projectId: number }) {
  return (
    <ContributionBoard
      endpointBase={`/api/v1/commercial/projects/${projectId}/contributions`}
      title="项目贡献看板"
      boardDescription="项目只维护建筑制作人员，贡献占比会根据填写的贡献分数自动重算。"
      displayRoles={[RoleType.BUILDER]}
      editableRoles={[RoleType.BUILDER]}
    />
  );
}
