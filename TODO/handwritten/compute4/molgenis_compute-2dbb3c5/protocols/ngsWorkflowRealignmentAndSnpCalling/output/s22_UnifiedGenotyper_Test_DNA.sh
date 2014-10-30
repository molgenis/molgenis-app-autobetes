



##### BEFORE #####
touch $PBS_O_WORKDIR/s22_UnifiedGenotyper_Test_DNA.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s22_UnifiedGenotyper_Test_DNA at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=46:00:00 mem=8 cores=5
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
getFile $WORKDIR/resources/hg19/dbsnp/dbsnp_129_b37_human_g1k_v37.rod
getFile $WORKDIR/resources/hg19/dbsnp/dbsnp_129_b37_human_g1k_v37.rod.idx

module load GATK/1.0.5069

java -Xmx8g -Djava.io.tmpdir=$WORKDIR/tmp/processing/ -XX:+UseParallelGC -XX:ParallelGCThreads=1 -jar \
$GATK_HOME/GenomeAnalysisTK.jar \
-l INFO \
-T UnifiedGenotyper \
-I $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
--out $WORKDIR/tmp//demo/output//Test_DNA.snps.vcf \
-R $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
-D $WORKDIR/resources/hg19/dbsnp/dbsnp_129_b37_human_g1k_v37.rod \
-stand_call_conf 30.0 \
-stand_emit_conf 10.0 \
-nt 4 \
--metrics_file $WORKDIR/tmp//demo/output//Test_DNA.snps.vcf.metrics

putFile $WORKDIR/tmp//demo/output//Test_DNA.snps.vcf
putFile $WORKDIR/tmp//demo/output//Test_DNA.snps.vcf.metrics

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s22_UnifiedGenotyper_Test_DNA at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s22_UnifiedGenotyper_Test_DNA.finished
######## END ########

