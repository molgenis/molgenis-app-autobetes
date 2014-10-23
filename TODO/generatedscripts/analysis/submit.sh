DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
touch $DIR/workflow_csv.started

#s00_LoadSensor_1
s00_LoadSensor_1=$(qsub -N s00_LoadSensor_1 s00_LoadSensor_1.sh)
echo $s00_LoadSensor_1
sleep 0

touch $DIR/workflow_csv.finished
