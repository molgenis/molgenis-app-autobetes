



##### BEFORE #####
touch $PBS_O_WORKDIR/s04_SamToBam_1.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s04_SamToBam_1 at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

echo Running on node: `hostname`

sleep 60
###### MAIN ######

#
# =====================================================
# $Id$
# $URL$
# $LastChangedDate$
# $LastChangedRevision$
# $LastChangedBy$
# =====================================================
#

#MOLGENIS walltime=35:59:00 mem=3
#FOREACH

module load picard-tools/1.61

getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.sam

java -jar -Xmx3g $PICARD_HOME//SamFormatConverter.jar \
INPUT=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.sam \
OUTPUT=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.bam \
VALIDATION_STRINGENCY=LENIENT \
MAX_RECORDS_IN_RAM=2000000 \
TMP_DIR=$WORKDIR/tmp/processing/

putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.bam

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s04_SamToBam_1 at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s04_SamToBam_1.finished
######## END ########

