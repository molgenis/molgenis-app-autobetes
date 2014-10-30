DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

touch $DIR/workflow_create_report_csv.started
export PBS_O_WORKDIR=${DIR}
echo Starting with s00_MealPlot_1...
sh s00_MealPlot_1.sh
#Dependencies: 

