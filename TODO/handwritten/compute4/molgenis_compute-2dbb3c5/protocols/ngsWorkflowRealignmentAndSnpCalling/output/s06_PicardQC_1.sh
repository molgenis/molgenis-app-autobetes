



##### BEFORE #####
touch $PBS_O_WORKDIR/s06_PicardQC_1.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s06_PicardQC_1 at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=35:59:00 mem=4
#TARGETS

module load picard-tools/1.61
module load R/2.14.2

getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.sorted.bam
getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.sorted.bam.bai
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
getFile $WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_baits_hg19_human_g1k_v37.chr1.interval_list
getFile $WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_exons_hg19_human_g1k_v37.chr1.interval_list

java -jar -Xmx4g $PICARD_HOME//CollectAlignmentSummaryMetrics.jar \
I=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.sorted.bam \
O=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.alignmentmetrics \
R=$WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
VALIDATION_STRINGENCY=LENIENT \
TMP_DIR=$WORKDIR/tmp/processing/

java -jar -Xmx4g $PICARD_HOME//CollectGcBiasMetrics.jar \
R=$WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
I=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.sorted.bam \
O=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.gcbiasmetrics \
CHART=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.gcbiasmetrics.pdf \
VALIDATION_STRINGENCY=LENIENT \
TMP_DIR=$WORKDIR/tmp/processing/

	java -jar -Xmx4g $PICARD_HOME//CollectInsertSizeMetrics.jar \
	I=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.sorted.bam \
	O=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.insertsizemetrics \
	H=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.insertsizemetrics.pdf \
	VALIDATION_STRINGENCY=LENIENT \
	TMP_DIR=$WORKDIR/tmp/processing/
	
	# Overwrite the PDFs that were just created by nicer onces:
	$WORKDIR/tools/createInsertSizePlot/createInsertSizePlot.R \
	--insertSizeMetrics $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.insertsizemetrics \
	--pdf $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.insertsizemetrics.pdf

java -jar -Xmx4g $PICARD_HOME//MeanQualityByCycle.jar \
I=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.sorted.bam \
O=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.meanqualitybycycle \
CHART=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.meanqualitybycycle.pdf \
VALIDATION_STRINGENCY=LENIENT \
TMP_DIR=$WORKDIR/tmp/processing/

java -jar -Xmx4g $PICARD_HOME//QualityScoreDistribution.jar \
I=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.sorted.bam \
O=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.qualityscoredistribution \
CHART=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.qualityscoredistribution.pdf \
VALIDATION_STRINGENCY=LENIENT \
TMP_DIR=$WORKDIR/tmp/processing/

	java -jar -Xmx4g $PICARD_HOME//CalculateHsMetrics.jar \
	INPUT=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.sorted.bam \
	OUTPUT=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.hsmetrics \
	BAIT_INTERVALS=$WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_baits_hg19_human_g1k_v37.chr1.interval_list \
	TARGET_INTERVALS=$WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_exons_hg19_human_g1k_v37.chr1.interval_list \
	VALIDATION_STRINGENCY=LENIENT \
	TMP_DIR=$WORKDIR/tmp/processing/

java -jar -Xmx4g $PICARD_HOME//BamIndexStats.jar \
INPUT=$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.sorted.bam \
VALIDATION_STRINGENCY=LENIENT \
TMP_DIR=$WORKDIR/tmp/processing/ \
> $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.bamindexstats


putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.alignmentmetrics
putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.gcbiasmetrics
putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.gcbiasmetrics.pdf
putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.insertsizemetrics
putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.insertsizemetrics.pdf
putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.meanqualitybycycle
putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.meanqualitybycycle.pdf
putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.qualityscoredistribution
putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.qualityscoredistribution.pdf
putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.hsmetrics
putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.bamindexstats

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s06_PicardQC_1 at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s06_PicardQC_1.finished
######## END ########

