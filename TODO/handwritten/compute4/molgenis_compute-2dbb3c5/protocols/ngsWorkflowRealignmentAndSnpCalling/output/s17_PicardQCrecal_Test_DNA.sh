



##### BEFORE #####
touch $PBS_O_WORKDIR/s17_PicardQCrecal_Test_DNA.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s17_PicardQCrecal_Test_DNA at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

echo Running on node: `hostname`

sleep 60
###### MAIN ######

#MOLGENIS walltime=20:00:00 mem=5

#FOREACH externalSampleID

###### Renaming because we call another protocol:
#inputs:

#outputs:

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
getFile $WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_baits_hg19_human_g1k_v37.chr1.interval_list
getFile $WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_exons_hg19_human_g1k_v37.chr1.interval_list

java -jar -Xmx4g $PICARD_HOME//CollectAlignmentSummaryMetrics.jar \
I=$WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
O=$WORKDIR/tmp//demo/output//Test_DNA.alignmentmetrics \
R=$WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
VALIDATION_STRINGENCY=LENIENT \
TMP_DIR=$WORKDIR/tmp/processing/

java -jar -Xmx4g $PICARD_HOME//CollectGcBiasMetrics.jar \
R=$WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
I=$WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
O=$WORKDIR/tmp//demo/output//Test_DNA.gcbiasmetrics \
CHART=$WORKDIR/tmp//demo/output//Test_DNA.gcbiasmetrics.pdf \
VALIDATION_STRINGENCY=LENIENT \
TMP_DIR=$WORKDIR/tmp/processing/

	java -jar -Xmx4g $PICARD_HOME//CollectInsertSizeMetrics.jar \
	I=$WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
	O=$WORKDIR/tmp//demo/output//Test_DNA.insertsizemetrics \
	H=$WORKDIR/tmp//demo/output//Test_DNA.insertsizemetrics.pdf \
	VALIDATION_STRINGENCY=LENIENT \
	TMP_DIR=$WORKDIR/tmp/processing/
	
	# Overwrite the PDFs that were just created by nicer onces:
	$WORKDIR/tools/createInsertSizePlot/createInsertSizePlot.R \
	--insertSizeMetrics $WORKDIR/tmp//demo/output//Test_DNA.insertsizemetrics \
	--pdf $WORKDIR/tmp//demo/output//Test_DNA.insertsizemetrics.pdf

java -jar -Xmx4g $PICARD_HOME//MeanQualityByCycle.jar \
I=$WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
O=$WORKDIR/tmp//demo/output//Test_DNA.meanqualitybycycle \
CHART=$WORKDIR/tmp//demo/output//Test_DNA.meanqualitybycycle.pdf \
VALIDATION_STRINGENCY=LENIENT \
TMP_DIR=$WORKDIR/tmp/processing/

java -jar -Xmx4g $PICARD_HOME//QualityScoreDistribution.jar \
I=$WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
O=$WORKDIR/tmp//demo/output//Test_DNA.qualityscoredistribution \
CHART=$WORKDIR/tmp//demo/output//Test_DNA.qualityscoredistribution.pdf \
VALIDATION_STRINGENCY=LENIENT \
TMP_DIR=$WORKDIR/tmp/processing/

	java -jar -Xmx4g $PICARD_HOME//CalculateHsMetrics.jar \
	INPUT=$WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
	OUTPUT=$WORKDIR/tmp//demo/output//Test_DNA.hsmetrics \
	BAIT_INTERVALS=$WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_baits_hg19_human_g1k_v37.chr1.interval_list \
	TARGET_INTERVALS=$WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_exons_hg19_human_g1k_v37.chr1.interval_list \
	VALIDATION_STRINGENCY=LENIENT \
	TMP_DIR=$WORKDIR/tmp/processing/

java -jar -Xmx4g $PICARD_HOME//BamIndexStats.jar \
INPUT=$WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
VALIDATION_STRINGENCY=LENIENT \
TMP_DIR=$WORKDIR/tmp/processing/ \
> $WORKDIR/tmp//demo/output//Test_DNA.bamindexstats.pdf


putFile $WORKDIR/tmp//demo/output//Test_DNA.alignmentmetrics
putFile $WORKDIR/tmp//demo/output//Test_DNA.gcbiasmetrics
putFile $WORKDIR/tmp//demo/output//Test_DNA.gcbiasmetrics.pdf
putFile $WORKDIR/tmp//demo/output//Test_DNA.insertsizemetrics
putFile $WORKDIR/tmp//demo/output//Test_DNA.insertsizemetrics.pdf
putFile $WORKDIR/tmp//demo/output//Test_DNA.meanqualitybycycle
putFile $WORKDIR/tmp//demo/output//Test_DNA.meanqualitybycycle.pdf
putFile $WORKDIR/tmp//demo/output//Test_DNA.qualityscoredistribution
putFile $WORKDIR/tmp//demo/output//Test_DNA.qualityscoredistribution.pdf
putFile $WORKDIR/tmp//demo/output//Test_DNA.hsmetrics
putFile $WORKDIR/tmp//demo/output//Test_DNA.bamindexstats.pdf

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s17_PicardQCrecal_Test_DNA at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s17_PicardQCrecal_Test_DNA.finished
######## END ########

