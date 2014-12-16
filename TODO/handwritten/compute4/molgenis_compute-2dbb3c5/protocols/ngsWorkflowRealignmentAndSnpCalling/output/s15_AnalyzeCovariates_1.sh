



##### BEFORE #####
touch $PBS_O_WORKDIR/s15_AnalyzeCovariates_1.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s15_AnalyzeCovariates_1 at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=00:45:00
#FOREACH

module load GATK/1.0.5069
module load R/2.14.2

getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.matefixed.covariates.table.csv
getFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.recal.covariate.table.csv

#export PATH=$WORKDIR/tools/R//bin:${PATH}
#export R_LIBS=$WORKDIR/tools/GATK-1.3-24-gc8b1c92/gsalib/ 

java -jar -Xmx4g $GATK_HOME/AnalyzeCovariates.jar -l INFO \
-resources $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
--recal_file $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.matefixed.covariates.table.csv \
-outputDir $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.recal.stats.before \
-Rscript $WORKDIR/tools/R//bin/Rscript \
-ignoreQ 5

java -jar -Xmx4g $GATK_HOME/AnalyzeCovariates.jar -l INFO \
-resources $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
--recal_file $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.recal.covariate.table.csv \
-outputDir $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.recal.stats.after \
-Rscript $WORKDIR/tools/R//bin/Rscript \
-ignoreQ 5

putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.recal.stats.before/4.CycleCovariate.dat
putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.recal.stats.after/4.CycleCovariate.dat

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s15_AnalyzeCovariates_1 at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s15_AnalyzeCovariates_1.finished
######## END ########

