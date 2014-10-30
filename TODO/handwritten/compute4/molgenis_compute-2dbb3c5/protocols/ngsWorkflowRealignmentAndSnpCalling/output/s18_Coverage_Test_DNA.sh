



##### BEFORE #####
touch $PBS_O_WORKDIR/s18_Coverage_Test_DNA.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s18_Coverage_Test_DNA at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=65:59:00 mem=12 cores=1
#FOREACH externalSampleID


getFile $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam
getFile $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam.bai
getFile $WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_exons_hg19_human_g1k_v37.chr1.interval_list

getFile $WORKDIR/tools/scripts/coverage.R

module load R/2.14.2

#export R_LIBS=$WORKDIR/tools/GATK-1.3-24-gc8b1c92/gsalib/

Rscript $WORKDIR/tools/scripts/coverage.R \
--bam $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
--chromosome 1 \
--interval_list $WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_exons_hg19_human_g1k_v37.chr1.interval_list \
--csv $WORKDIR/tmp//demo/output//Test_DNA.coverage.csv \
--pdf $WORKDIR/tmp//demo/output//Test_DNA.coverageplot.pdf \
--Rcovlist $WORKDIR/tmp//demo/output//Test_DNA.coverage.Rdata

putFile $WORKDIR/tmp//demo/output//Test_DNA.coverage.csv
putFile $WORKDIR/tmp//demo/output//Test_DNA.coverageplot.pdf
putFile $WORKDIR/tmp//demo/output//Test_DNA.coverage.Rdata

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s18_Coverage_Test_DNA at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s18_Coverage_Test_DNA.finished
######## END ########

