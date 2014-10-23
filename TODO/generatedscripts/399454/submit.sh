DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
touch $DIR/workflow_create_report_csv.started

#s00_MealPlot_1
s00_MealPlot_1=$(qsub -N s00_MealPlot_1 s00_MealPlot_1.sh)
echo $s00_MealPlot_1
sleep 0

touch $DIR/workflow_create_report_csv.finished