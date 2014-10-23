



##### BEFORE #####
touch $PBS_O_WORKDIR/s11_CovariatesBefore_1.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s11_CovariatesBefore_1 at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

echo Running on node: `hostname`

sleep 60
###### MAIN ######

#MOLGENIS walltime=66:00:00

#
# =====================================================
# $Id$
# $URL$
# $LastChangedDate$
# $LastChangedRevision$
# $LastChangedBy$
# =====================================================
#

#FOREACH

module load GATK/1.0.5069

getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.matefixed.bam
getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.matefixed.bam.bai
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

java -jar -Xmx4g \
$GATK_HOME/GenomeAnalysisTK.jar -l INFO \
-T CountCovariates \
-U ALLOW_UNINDEXED_BAM \
-R $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
--DBSNP $WORKDIR/resources/hg19/dbsnp/dbsnp_129_b37_human_g1k_v37.rod \
-I $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.matefixed.bam \
-cov ReadGroupcovariate \
-cov QualityScoreCovariate \
-cov CycleCovariate \
-cov DinucCovariate \
-recalFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.matefixed.covariates.table.csv

putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.matefixed.covariates.table.csv
###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s11_CovariatesBefore_1 at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s11_CovariatesBefore_1.finished
######## END ########

