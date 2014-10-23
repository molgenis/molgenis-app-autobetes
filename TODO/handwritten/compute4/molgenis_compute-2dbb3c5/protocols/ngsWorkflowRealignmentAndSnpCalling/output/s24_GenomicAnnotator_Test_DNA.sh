



##### BEFORE #####
touch $PBS_O_WORKDIR/s24_GenomicAnnotator_Test_DNA.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s24_GenomicAnnotator_Test_DNA at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=24:00:00 mem=10
#FOREACH externalSampleID

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
getFile $WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_baits_hg19_human_g1k_v37.chr1.bed
getFile $WORKDIR/resources/hg19/dbsnp/dbSNP135.tabdelim.table
getFile $WORKDIR/resources/hg19/dbsnp/dbSNP135.tabdelim.table.idx
getFile $WORKDIR/tmp//demo/output//Test_DNA.snps.vcf
getFile $WORKDIR/tmp//demo/output//Test_DNA.snps.vcf.idx

#####Annotate with dbSNP135 SNPs only#####
java -Xmx10g -jar $GATK_HOME/GenomeAnalysisTK.jar \
-T GenomicAnnotator \
-l info \
-R $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
-B:variant,vcf $WORKDIR/tmp//demo/output//Test_DNA.snps.vcf \
-B:dbSNP135,AnnotatorInputTable $WORKDIR/resources/hg19/dbsnp/dbSNP135.tabdelim.table \
-s dbSNP135.AF,dbSNP135.ASP,dbSNP135.ASS,dbSNP135.CDA,dbSNP135.CFL,dbSNP135.CLN,dbSNP135.DSS,dbSNP135.G5,\
dbSNP135.G5A,dbSNP135.GCF,dbSNP135.GMAF,dbSNP135.GNO,dbSNP135.HD,dbSNP135.INT,dbSNP135.KGPROD,dbSNP135.KGPilot1,dbSNP135.KGPilot123,\
dbSNP135.KGVAL,dbSNP135.LSD,dbSNP135.MTP,dbSNP135.MUT,dbSNP135.NOC,dbSNP135.NOV,dbSNP135.NS,dbSNP135.NSF,dbSNP135.NSM,dbSNP135.OM,\
dbSNP135.OTH,dbSNP135.PH1,dbSNP135.PH2,dbSNP135.PH3,dbSNP135.PM,dbSNP135.PMC,dbSNP135.R3,dbSNP135.R5,dbSNP135.REF,dbSNP135.RSPOS,\
dbSNP135.RV,dbSNP135.S3D,dbSNP135.SAO,dbSNP135.SCS,dbSNP135.SLO,dbSNP135.SSR,dbSNP135.SYN,dbSNP135.TPA,dbSNP135.U3,dbSNP135.U5,dbSNP135.VC,\
dbSNP135.VLD,dbSNP135.VP,dbSNP135.WGT,dbSNP135.WTD,dbSNP135.dbSNPBuildID \
-o $WORKDIR/tmp//demo/output//Test_DNA.snps.genomic.annotated.vcf \
-L $WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_baits_hg19_human_g1k_v37.chr1.bed

putFile $WORKDIR/tmp//demo/output//Test_DNA.snps.genomic.annotated.vcf

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s24_GenomicAnnotator_Test_DNA at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s24_GenomicAnnotator_Test_DNA.finished
######## END ########

