



##### BEFORE #####
touch $PBS_O_WORKDIR/s09_Realign_1.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s09_Realign_1 at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=35:59:00 mem=10 cores=1
#FOREACH

module load GATK/1.0.5069

getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.dedup.bam
getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.dedup.bam.bai
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
getFile $WORKDIR/resources/hg19/dbsnp/dbsnp_129_b37_human_g1k_v37.rod
getFile $WORKDIR/resources/hg19/dbsnp/dbsnp_129_b37_human_g1k_v37.rod.idx
getFile $WORKDIR/resources/hg19/indels/1kg_pilot_release_merged_indels_sites_hg19_human_g1k_v37.vcf
getFile $WORKDIR/resources/hg19/indels/1kg_pilot_release_merged_indels_sites_hg19_human_g1k_v37.vcf.idx
getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.realign.target.intervals

java -Djava.io.tmpdir=$WORKDIR/tmp/processing/ -Xmx10g -jar \
$GATK_HOME/GenomeAnalysisTK.jar \
-l INFO \
-T IndelRealigner \
-U ALLOW_UNINDEXED_BAM \
-I $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.dedup.bam \
--out $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.realigned.bam \
-targetIntervals $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.realign.target.intervals \
-R $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
-D $WORKDIR/resources/hg19/dbsnp/dbsnp_129_b37_human_g1k_v37.rod \
-B:indels,VCF $WORKDIR/resources/hg19/indels/1kg_pilot_release_merged_indels_sites_hg19_human_g1k_v37.vcf \
-knownsOnly \
-LOD 0.4 \
-maxReads 2000000

putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.realigned.bam

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s09_Realign_1 at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s09_Realign_1.finished
######## END ########

