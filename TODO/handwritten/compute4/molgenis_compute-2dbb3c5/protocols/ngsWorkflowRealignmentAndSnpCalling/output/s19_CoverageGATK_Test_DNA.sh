



##### BEFORE #####
touch $PBS_O_WORKDIR/s19_CoverageGATK_Test_DNA.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s19_CoverageGATK_Test_DNA at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=66:00:00 nodes=1 cores=1 mem=12
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

getFile $WORKDIR/tools/scripts/plot_cumulative_coverage-1.1.R

getFile $WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_exons_hg19_human_g1k_v37.chr1.interval_list

module load GATK/1.0.5069

#export R_LIBS=$WORKDIR/tools/GATK-1.3-24-gc8b1c92/gsalib/

java -Djava.io.tmpdir=$WORKDIR/tmp/processing/ -Xmx12g -jar \
$GATK_HOME/GenomeAnalysisTK.jar \
-T DepthOfCoverage \
-R $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
-I $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
-o $WORKDIR/tmp//demo/output//Test_DNA.coveragegatk \
-ct 1 -ct 2 -ct 5 -ct 10 -ct 15 -ct 20 -ct 30 -ct 40 -ct 50 \
-L $WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_exons_hg19_human_g1k_v37.chr1.interval_list

#Create coverage graphs for sample
$WORKDIR/tools/R//bin/Rscript $WORKDIR/tools/scripts/plot_cumulative_coverage-1.1.R \
--in $WORKDIR/tmp//demo/output//Test_DNA.coveragegatk.sample_cumulative_coverage_proportions \
--out $WORKDIR/tmp//demo/output//Test_DNA.coveragegatk.cumulative_coverage.pdf \
--max-depth 100 \
--title "Cumulative coverage Test_DNA"

putFile $WORKDIR/tmp//demo/output//Test_DNA.coveragegatk
putFile $WORKDIR/tmp//demo/output//Test_DNA.coveragegatk.sample_cumulative_coverage_counts
putFile $WORKDIR/tmp//demo/output//Test_DNA.coveragegatk.sample_cumulative_coverage_proportions
putFile $WORKDIR/tmp//demo/output//Test_DNA.coveragegatk.sample_interval_statistics
putFile $WORKDIR/tmp//demo/output//Test_DNA.coveragegatk.sample_interval_summary
putFile $WORKDIR/tmp//demo/output//Test_DNA.coveragegatk.sample_statistics
putFile $WORKDIR/tmp//demo/output//Test_DNA.coveragegatk.sample_summary
putFile $WORKDIR/tmp//demo/output//Test_DNA.coveragegatk.cumulative_coverage.pdf

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s19_CoverageGATK_Test_DNA at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s19_CoverageGATK_Test_DNA.finished
######## END ########

