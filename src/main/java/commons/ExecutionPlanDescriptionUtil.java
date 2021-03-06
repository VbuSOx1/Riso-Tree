package commons;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.ExecutionPlanDescription;


public class ExecutionPlanDescriptionUtil {
  public static enum PlanExecutionType {
    Filter, Expand, NodeByLabelScan, Other,
  }

  public static enum ArgKey {
    ExpandExpression, LabelName,
  }

  /**
   * Find the first operator with the given {@code type}.
   *
   * @param description
   * @param type
   * @return null if the given type does not exist.
   */
  public static ExecutionPlanDescription findFirstNodeType(ExecutionPlanDescription description,
      PlanExecutionType type) {
    if (getPlanExecutionType(description.getName()).equals(type)) {
      return description;
    }
    for (ExecutionPlanDescription childrenDescription : description.getChildren()) {
      ExecutionPlanDescription resultDescription = findFirstNodeType(childrenDescription, type);
      if (resultDescription != null) {
        return resultDescription;
      }
    }
    return null;
  }

  public static String[] getEdgeInExpandExpressionPlanNode(
      ExecutionPlanDescription planDescription) {
    // Util.println(planDescription);
    Object value = planDescription.getArguments().get(ArgKey.ExpandExpression.name());
    String[] nodeVariables = StringUtils.substringsBetween(value.toString(), "(", ")");
    String nodeVariable1 = nodeVariables[0];
    if (nodeVariable1.contains(":")) {
      nodeVariable1 = StringUtils.split(nodeVariable1, ":")[0];
    }

    String nodeVariable2 = nodeVariables[1];
    if (nodeVariable2.contains(":")) {
      nodeVariable2 = StringUtils.split(nodeVariable2, ":")[0];
    }
    return new String[] {nodeVariable1, nodeVariable2};
  }

  public static PlanExecutionType getPlanExecutionType(String keyword) {
    if (keyword.contains("Expand")) {
      return PlanExecutionType.Expand;
    } else {
      try {
        return PlanExecutionType.valueOf(keyword);
      } catch (Exception e) {
        return PlanExecutionType.Other;
      }
    }
  }

  public static List<ExecutionPlanDescription> getRequired(ExecutionPlanDescription root) {
    return getRequired(root, PlanExecutionType.Expand);
  }

  public static List<ExecutionPlanDescription> getRequired(ExecutionPlanDescription root,
      PlanExecutionType typeWanted) {
    List<ExecutionPlanDescription> res = new LinkedList<>();
    Queue<ExecutionPlanDescription> queue = new LinkedList<ExecutionPlanDescription>();
    queue.add(root);
    if (isUseful(root)) {
      res.add(root);
    }
    while (queue.isEmpty() == false) {
      ExecutionPlanDescription planDescription = queue.poll();
      for (ExecutionPlanDescription childPlan : planDescription.getChildren()) {
        queue.add(childPlan);
        if (isUseful(childPlan, typeWanted)) {
          res.add(childPlan);
        }
      }
    }
    return res;
  }

  public static boolean isUseful(ExecutionPlanDescription planDescription) {
    return isUseful(planDescription, PlanExecutionType.Expand);
  }

  public static boolean isUseful(ExecutionPlanDescription planDescription,
      PlanExecutionType typeWanted) {
    PlanExecutionType type = getPlanExecutionType(planDescription.getName());
    if (type.equals(typeWanted)) {
      return true;
    }
    return false;
  }
}
