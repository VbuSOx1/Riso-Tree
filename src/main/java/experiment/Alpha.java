package experiment;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import commons.Enums;
import commons.ReadWriteUtil;
import commons.Util;

public class Alpha {

  public static void alphaExperiment(String dbPaths, String dataset, int MAX_HOP, String queryPath,
      int queryCount, String password, boolean clearCache, Enums.ClearCacheMethod clearCacheMethod,
      String outputPath) throws Exception {
    Enums.ExperimentMethod method = Enums.ExperimentMethod.RISOTREE;
    String[] dbPathsList = dbPaths.split(",");
    Util.checkPathExist(dbPathsList);
    String header = ExperimentUtil.getHeader(method);
    ReadWriteUtil.WriteFile(outputPath, true, queryPath + "\n");
    ReadWriteUtil.WriteFile(outputPath, true, "dbPath\t" + header + "\n");
    for (String dbPath : dbPathsList) {
      List<ResultRecord> records = ExperimentUtil.runExperiment(dbPath, dataset, method, MAX_HOP,
          queryPath, queryCount, password, clearCache, clearCacheMethod);
      String string = ExperimentUtil.getAverageResultOutput(records, method);
      ReadWriteUtil.WriteFile(outputPath, true, StringUtils.joinWith("\t", dbPath, string) + "\n");
    }
  }
}
