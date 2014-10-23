



##### BEFORE #####
touch $PBS_O_WORKDIR/s23_MakeIndelMask_Test_DNA.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s23_MakeIndelMask_Test_DNA at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=40:00:00
#FOREACH externalSampleID

getFile $WORKDIR/tmp//demo/output//Test_DNA.indels.filtered.bed

getFile $WORKDIR/tools/scripts/makeIndelMask.py

module load Python/2.7.3

python $WORKDIR/tools/scripts/makeIndelMask.py \
$WORKDIR/tmp//demo/output//Test_DNA.indels.filtered.bed \
10 \
$WORKDIR/tmp//demo/output//Test_DNA.indels.mask.bed

putFile $WORKDIR/tmp//demo/output//Test_DNA.indels.mask.bed

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s23_MakeIndelMask_Test_DNA at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s23_MakeIndelMask_Test_DNA.finished
######## END ########

