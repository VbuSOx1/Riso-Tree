#!/bin/bash
./package.sh

# server
dir="/hdd/code/yuhansun"
dataset="wikidata"
data_dir="${dir}/data/wikidata_risotree"
code_dir="${dir}/code"

# local test setup
# dir="/Users/zhouyang/Google_Drive/Projects/tmp/risotree"
# dataset="Yelp"
# data_dir="${dir}/${dataset}"
# code_dir="/Users/zhouyang/Google_Drive/Projects/github_code"

graph_path="${data_dir}/graph.txt"
entity_path="${data_dir}/entity.txt"
label_path="${data_dir}/graph_label.txt"
labelStrMapPath="${data_dir}/entity_string_label.txt"

jar_path="${code_dir}/Riso-Tree/target/Riso-Tree-0.0.1-SNAPSHOT.jar"

split_mode="Gleenes"
alpha="0.5"
maxPNSize="100"

db_path="${data_dir}/neo4j-community-3.4.12_${split_mode}_${alpha}_${maxPNSize}/data/databases/graph.db"
containID_path="${data_dir}/containID_${split_mode}_${alpha}_${maxPNSize}.txt"
PNPathAndPrefix="${data_dir}/PathNeighbors_${split_mode}_${alpha}_${maxPNSize}"


# convert single graph to bidirectional
# java -Xmx100g -jar ${jar_path} -f convertSingleToBidirectinalGraph -dataDir ${data_dir}

java -Xmx100g -jar ${jar_path} -f wikiGenerateContainSpatialID \
	-dp ${db_path} -d ${dataset} -c ${containID_path}

###### Wikidata Construct Path Neighbors for leaf nodes ###### (not used)
# java -Xmx100g -jar ${jar_path} -f wikiConstructPNTime \
# 	-dp ${db_path} -c ${containID_path} -gp ${graph_path} -labelStrMapPath ${labelStrMapPath}\
# 	-lp ${label_path} -MAX_HOPNUM ${MAX_HOPNUM} -PNPrefix ${PNPathAndPrefix}

# 0-hop
java -Xmx100g -jar ${jar_path} -f wikiConstructPNTimeSingleHop \
	-dp ${db_path} -c ${containID_path} -gp ${graph_path} -labelStrMapPath ${labelStrMapPath}\
	-lp ${label_path} -hop 0 -PNPrefix ${PNPathAndPrefix} -maxPNSize ${maxPNSize}
java -Xmx100g -jar ${jar_path} -f wikiLoadPN \
	-dp ${db_path} -c ${containID_path} -gp ${graph_path} -labelStrMapPath ${labelStrMapPath}\
	-lp ${label_path} -hop 0 -PNPrefix ${PNPathAndPrefix} -maxPNSize ${maxPNSize}

# 1-hop
java -Xmx100g -jar ${jar_path} -f wikiConstructPNTimeSingleHop \
	-dp ${db_path} -c ${containID_path} -gp ${graph_path} -labelStrMapPath ${labelStrMapPath}\
	-lp ${label_path} -hop 1 -PNPrefix ${PNPathAndPrefix} -maxPNSize ${maxPNSize}
java -Xmx100g -jar ${jar_path} -f wikiLoadPN \
	-dp ${db_path} -c ${containID_path} -gp ${graph_path} -labelStrMapPath ${labelStrMapPath}\
	-lp ${label_path} -hop 1 -PNPrefix ${PNPathAndPrefix} -maxPNSize ${maxPNSize}

# 2-hop
# java -Xmx100g -jar ${jar_path} -f wikiConstructPNTimeSingleHop \
# 	-dp ${db_path} -c ${containID_path} -gp ${graph_path} -labelStrMapPath ${labelStrMapPath}\
# 	-lp ${label_path} -hop 2 -PNPrefix ${PNPathAndPrefix} -maxPNSize 100
# java -Xmx100g -jar ${jar_path} -f wikiLoadPN \
# 	-dp ${db_path} -c ${containID_path} -gp ${graph_path} -labelStrMapPath ${labelStrMapPath}\
# 	-lp ${label_path} -hop 2 -PNPrefix ${PNPathAndPrefix}


###### Load PathNeighbor into db ######
# java -Xmx100g -jar ${jar_path} -f loadPN -PNPrefix ${PNPathAndPrefix} -MAX_HOPNUM ${MAX_HOPNUM} -dp ${db_path}
