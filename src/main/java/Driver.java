import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import graph.LoadDataNoOSM;

public class Driver {

  private String[] args = null;
  private Options options = new Options();

  private String help = "h";
  private String function = "f";
  private String graphPath = "gp";
  private String entityPath = "ep";
  private String labelListPath = "lp";
  private String dbPath = "dp";
  private String dataset = "d";

  // function names
  private String risoTreeSkeleton = "tree";

  public Driver(String[] args) {
    this.args = args;
    options.addOption(help, "help", false, "show help.");
    options.addOption(function, "function", true, "function name");
    options.addOption(graphPath, "graph-path", true, "graph path");
    options.addOption(entityPath, "entity-path", true, "enitty path");
    options.addOption(labelListPath, "labellist-path", true, "label list path");
    options.addOption(dbPath, "db-path", true, "db path");
    options.addOption(dataset, "dataset", true, "dataset for naming the layer");

  }

  public void parser() {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);

      if (cmd.hasOption("h")) {
        help();
      }

      if (cmd.hasOption(function)) {
        String functionName = cmd.getOptionValue(function);
        if (functionName.equals(risoTreeSkeleton)) {
          String graphPathVal = cmd.getOptionValue(graphPath);
          String entityPathVal = cmd.getOptionValue(entityPath);
          String labelListPathVal = cmd.getOptionValue(labelListPath);
          String dbPathVal = cmd.getOptionValue(dbPath);
          String datasetVal = cmd.getOptionValue(dataset);

          LoadDataNoOSM.batchRTreeInsertOneHopAware(dbPathVal, dataset, graphPathVal, entityPathVal,
              labelListPathVal);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  private void help() {
    HelpFormatter formater = new HelpFormatter();
    formater.printHelp("Main", options);
    System.exit(0);
  }


  public static void main(String[] args) {
    Driver driver = new Driver(args);
    driver.parser();
  }

}