



##### BEFORE #####
touch $PBS_O_WORKDIR/s20_IndelGenotyper_Test_DNA.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s20_IndelGenotyper_Test_DNA at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=33:00:00 mem=8
#FOREACH externalSampleID

getFile $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam
getFile $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam.bai
getFile $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa
getFile $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa.amb
getFile $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa.ann
getFile $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa.bwt
getFile $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa.fai
getFile $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa.pac
getFile $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa.rbwt
getFile $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa.rpac
getFile $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa.rsa
getFile $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa.sa

module load GATK/1.0.5069

java -Xmx8g -jar $GATK_HOME/GenomeAnalysisTK.jar \
-l INFO \
-T IndelGenotyperV2 \
-I $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
-o $WORKDIR/tmp//demo/output//Test_DNA.indels.vcf \
--bedOutput $WORKDIR/tmp//demo/output//Test_DNA.indels.bed \
-R $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
-verbose $WORKDIR/tmp//demo/output//Test_DNA.indels.verboseoutput.txt \
--window_size 300

putFile $WORKDIR/tmp//demo/output//Test_DNA.indels.vcf
putFile $WORKDIR/tmp//demo/output//Test_DNA.indels.bed
putFile $WORKDIR/tmp//demo/output//Test_DNA.indels.verboseoutput.txt

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s20_IndelGenotyper_Test_DNA at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s20_IndelGenotyper_Test_DNA.finished
######## END ########

