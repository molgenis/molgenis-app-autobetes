



##### BEFORE #####
touch $PBS_O_WORKDIR/s21_FilterIndels_Test_DNA.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s21_FilterIndels_Test_DNA at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

getFile $WORKDIR/tmp//demo/output//Test_DNA.indels.bed

getFile $WORKDIR/tools/scripts/filterSingleSampleCalls.pl

perl $WORKDIR/tools/scripts/filterSingleSampleCalls.pl \
--calls $WORKDIR/tmp//demo/output//Test_DNA.indels.bed \
--max_cons_av_mm 3.0 \
--max_cons_nqs_av_mm 0.5 \
--mode ANNOTATE \
> $WORKDIR/tmp//demo/output//Test_DNA.indels.filtered.bed

putFile $WORKDIR/tmp//demo/output//Test_DNA.indels.filtered.bed

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s21_FilterIndels_Test_DNA at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s21_FilterIndels_Test_DNA.finished
######## END ########

