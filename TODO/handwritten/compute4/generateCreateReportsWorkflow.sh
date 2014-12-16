mcpath=/Users/mdijkstra/Documents/pompgemak/molgenis_distro/handwritten/compute4/molgenis_compute-7157a04
workflowdatapath=$mcpath/../../reportworkflow

sh $mcpath/molgenis_compute.sh \
-worksheet=$workflowdatapath/worksheet.csv \
-parameters=$workflowdatapath/parameters_create_report.csv \
-workflow=$workflowdatapath/workflow_create_report.csv \
-protocols=$workflowdatapath/protocols \
-outputdir=$mcpath/../../../generatedscripts/399454