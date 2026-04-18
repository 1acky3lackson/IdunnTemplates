import { ContributionBoard, RoleType } from "~/common/contribution/ContributionBoard";

export function ProductContributions({ productId }: { productId: number }) {
  return (
    <ContributionBoard
      endpointBase={`/api/v1/commercial/products/${productId}/contributions`}
      title="商品贡献看板"
      boardDescription="商品页展示建筑制作、修改美化、包装宣传三类贡献。建筑制作从所属项目继承展示，商品内可维护修改美化与包装宣传人员。"
      displayRoles={[RoleType.BUILDER, RoleType.MODIFIER, RoleType.UPLOADER]}
      editableRoles={[RoleType.MODIFIER, RoleType.UPLOADER]}
    />
  );
}
