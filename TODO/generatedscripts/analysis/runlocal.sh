DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

touch $DIR/workflow_csv.started
export PBS_O_WORKDIR=${DIR}
echo Starting with s00_LoadSensor_1...
sh s00_LoadSensor_1.sh
#Dependencies: 

