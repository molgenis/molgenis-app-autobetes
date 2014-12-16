



##### BEFORE #####
touch $PBS_O_WORKDIR/s25_VariantAnnotator_Test_DNA.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s25_VariantAnnotator_Test_DNA at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=45:00:00 mem=10
#FOREACH externalSampleID

getFile $WORKDIR/tools/snpEff_2_0_5/snpEff.jar
getFile $WORKDIR/tools/snpEff_2_0_5/snpEff.config 
getFile $WORKDIR/tmp//demo/output//Test_DNA.snps.genomic.annotated.vcf
getFile $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam
getFile $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam.bai
getFile $WORKDIR/resources/hg19/dbsnp/dbsnp_135.b37.vcf
getFile $WORKDIR/resources/hg19/dbsnp/dbsnp_135.b37.vcf.idx
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

####Create snpEFF annotations on original input file####
java -Xmx4g -jar $WORKDIR/tools/snpEff_2_0_5/snpEff.jar \
eff \
-v \
-c $WORKDIR/tools/snpEff_2_0_5/snpEff.config \
-i vcf \
-o vcf \
GRCh37.64 \
-onlyCoding true \
-stats $WORKDIR/tmp//demo/output//Test_DNA.snps.final.snpEff_summary.html \
$WORKDIR/tmp//demo/output//Test_DNA.snps.genomic.annotated.vcf \
> $WORKDIR/tmp//demo/output//Test_DNA.snps.intermediate.snpEff.vcf

####Annotate SNPs with snpEff information####
java -jar -Xmx4g $WORKDIR/tools/GATK-1.4-11-g845c0b1/dist/GenomeAnalysisTK.jar \
-T VariantAnnotator \
--useAllAnnotations \
--excludeAnnotation MVLikelihoodRatio \
--excludeAnnotation TechnologyComposition \
-I $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam \
--snpEffFile $WORKDIR/tmp//demo/output//Test_DNA.snps.intermediate.snpEff.vcf \
-D $WORKDIR/resources/hg19/dbsnp/dbsnp_135.b37.vcf \
-R $WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
--variant $WORKDIR/tmp//demo/output//Test_DNA.snps.genomic.annotated.vcf \
-L $WORKDIR/resources/hg19/intervals/SureSelect_All_Exon_50MB_baits_hg19_human_g1k_v37.chr1.bed \
-o $WORKDIR/tmp//demo/output//Test_DNA.snps.final.vcf

putFile $WORKDIR/tmp//demo/output//Test_DNA.snps.final.snpEff_summary.html
putFile $WORKDIR/tmp//demo/output//Test_DNA.snps.intermediate.snpEff.vcf
putFile $WORKDIR/tmp//demo/output//Test_DNA.snps.final.vcf

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s25_VariantAnnotator_Test_DNA at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s25_VariantAnnotator_Test_DNA.finished
######## END ########

