



##### BEFORE #####
touch $PBS_O_WORKDIR/s16_MergeBam_Test_DNA.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s16_MergeBam_Test_DNA at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=23:59:00 mem=6 cores=2
#FOREACH externalSampleID

getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.recal.sorted.bam
getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.recal.sorted.bam.bai

module load picard-tools/1.61

	#cp $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.recal.sorted.bam $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam
	#cp $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.recal.sorted.bam.bai $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam.bai
	ln -s $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.recal.sorted.bam $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam
	ln -s $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.recal.sorted.bam.bai $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam.bai

putFile $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam
putFile $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam.bai

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s16_MergeBam_Test_DNA at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s16_MergeBam_Test_DNA.finished
######## END ########

