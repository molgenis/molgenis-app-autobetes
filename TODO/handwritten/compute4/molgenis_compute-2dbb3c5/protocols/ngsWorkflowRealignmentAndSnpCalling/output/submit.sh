DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
touch $DIR/workflow_csv.started

#s00_Fastqc_1
s00_Fastqc_1=$(qsub -N s00_Fastqc_1 s00_Fastqc_1.sh)
echo $s00_Fastqc_1
sleep 0
#s01_BwaAlignLeft_1
s01_BwaAlignLeft_1=$(qsub -N s01_BwaAlignLeft_1 s01_BwaAlignLeft_1.sh)
echo $s01_BwaAlignLeft_1
sleep 0
#s02_BwaAlignRight_1
s02_BwaAlignRight_1=$(qsub -N s02_BwaAlignRight_1 s02_BwaAlignRight_1.sh)
echo $s02_BwaAlignRight_1
sleep 0
#s03_BwaSampe_1
s03_BwaSampe_1=$(qsub -N s03_BwaSampe_1 -W depend=afterok:$s01_BwaAlignLeft_1:$s02_BwaAlignRight_1 s03_BwaSampe_1.sh)
echo $s03_BwaSampe_1
sleep 0
#s04_SamToBam_1
s04_SamToBam_1=$(qsub -N s04_SamToBam_1 -W depend=afterok:$s03_BwaSampe_1 s04_SamToBam_1.sh)
echo $s04_SamToBam_1
sleep 0
#s05_SamSort_1
s05_SamSort_1=$(qsub -N s05_SamSort_1 -W depend=afterok:$s04_SamToBam_1 s05_SamSort_1.sh)
echo $s05_SamSort_1
sleep 0
#s06_PicardQC_1
s06_PicardQC_1=$(qsub -N s06_PicardQC_1 -W depend=afterok:$s05_SamSort_1 s06_PicardQC_1.sh)
echo $s06_PicardQC_1
sleep 0
#s07_Markduplicates_1
s07_Markduplicates_1=$(qsub -N s07_Markduplicates_1 -W depend=afterok:$s05_SamSort_1 s07_Markduplicates_1.sh)
echo $s07_Markduplicates_1
sleep 0
#s08_RealignTargetCreator_1
s08_RealignTargetCreator_1=$(qsub -N s08_RealignTargetCreator_1 -W depend=afterok:$s07_Markduplicates_1 s08_RealignTargetCreator_1.sh)
echo $s08_RealignTargetCreator_1
sleep 0
#s09_Realign_1
s09_Realign_1=$(qsub -N s09_Realign_1 -W depend=afterok:$s08_RealignTargetCreator_1 s09_Realign_1.sh)
echo $s09_Realign_1
sleep 0
#s10_Fixmates_1
s10_Fixmates_1=$(qsub -N s10_Fixmates_1 -W depend=afterok:$s09_Realign_1 s10_Fixmates_1.sh)
echo $s10_Fixmates_1
sleep 0
#s11_CovariatesBefore_1
s11_CovariatesBefore_1=$(qsub -N s11_CovariatesBefore_1 -W depend=afterok:$s10_Fixmates_1 s11_CovariatesBefore_1.sh)
echo $s11_CovariatesBefore_1
sleep 0
#s12_Recalibrate_1
s12_Recalibrate_1=$(qsub -N s12_Recalibrate_1 -W depend=afterok:$s10_Fixmates_1:$s11_CovariatesBefore_1 s12_Recalibrate_1.sh)
echo $s12_Recalibrate_1
sleep 0
#s13_SamSortRecal_1
s13_SamSortRecal_1=$(qsub -N s13_SamSortRecal_1 -W depend=afterok:$s12_Recalibrate_1 s13_SamSortRecal_1.sh)
echo $s13_SamSortRecal_1
sleep 0
#s14_CovariatesAfter_1
s14_CovariatesAfter_1=$(qsub -N s14_CovariatesAfter_1 -W depend=afterok:$s13_SamSortRecal_1 s14_CovariatesAfter_1.sh)
echo $s14_CovariatesAfter_1
sleep 0
#s15_AnalyzeCovariates_1
s15_AnalyzeCovariates_1=$(qsub -N s15_AnalyzeCovariates_1 -W depend=afterok:$s11_CovariatesBefore_1:$s14_CovariatesAfter_1 s15_AnalyzeCovariates_1.sh)
echo $s15_AnalyzeCovariates_1
sleep 0
#s16_MergeBam_Test_DNA
s16_MergeBam_Test_DNA=$(qsub -N s16_MergeBam_Test_DNA -W depend=afterok:$s13_SamSortRecal_1 s16_MergeBam_Test_DNA.sh)
echo $s16_MergeBam_Test_DNA
sleep 0
#s17_PicardQCrecal_Test_DNA
s17_PicardQCrecal_Test_DNA=$(qsub -N s17_PicardQCrecal_Test_DNA -W depend=afterok:$s16_MergeBam_Test_DNA s17_PicardQCrecal_Test_DNA.sh)
echo $s17_PicardQCrecal_Test_DNA
sleep 0
#s18_Coverage_Test_DNA
s18_Coverage_Test_DNA=$(qsub -N s18_Coverage_Test_DNA -W depend=afterok:$s16_MergeBam_Test_DNA s18_Coverage_Test_DNA.sh)
echo $s18_Coverage_Test_DNA
sleep 0
#s19_CoverageGATK_Test_DNA
s19_CoverageGATK_Test_DNA=$(qsub -N s19_CoverageGATK_Test_DNA -W depend=afterok:$s16_MergeBam_Test_DNA s19_CoverageGATK_Test_DNA.sh)
echo $s19_CoverageGATK_Test_DNA
sleep 0
#s20_IndelGenotyper_Test_DNA
s20_IndelGenotyper_Test_DNA=$(qsub -N s20_IndelGenotyper_Test_DNA -W depend=afterok:$s16_MergeBam_Test_DNA s20_IndelGenotyper_Test_DNA.sh)
echo $s20_IndelGenotyper_Test_DNA
sleep 0
#s21_FilterIndels_Test_DNA
s21_FilterIndels_Test_DNA=$(qsub -N s21_FilterIndels_Test_DNA -W depend=afterok:$s20_IndelGenotyper_Test_DNA s21_FilterIndels_Test_DNA.sh)
echo $s21_FilterIndels_Test_DNA
sleep 0
#s22_UnifiedGenotyper_Test_DNA
s22_UnifiedGenotyper_Test_DNA=$(qsub -N s22_UnifiedGenotyper_Test_DNA -W depend=afterok:$s16_MergeBam_Test_DNA s22_UnifiedGenotyper_Test_DNA.sh)
echo $s22_UnifiedGenotyper_Test_DNA
sleep 0
#s23_MakeIndelMask_Test_DNA
s23_MakeIndelMask_Test_DNA=$(qsub -N s23_MakeIndelMask_Test_DNA -W depend=afterok:$s21_FilterIndels_Test_DNA s23_MakeIndelMask_Test_DNA.sh)
echo $s23_MakeIndelMask_Test_DNA
sleep 0
#s24_GenomicAnnotator_Test_DNA
s24_GenomicAnnotator_Test_DNA=$(qsub -N s24_GenomicAnnotator_Test_DNA -W depend=afterok:$s22_UnifiedGenotyper_Test_DNA s24_GenomicAnnotator_Test_DNA.sh)
echo $s24_GenomicAnnotator_Test_DNA
sleep 0
#s25_VariantAnnotator_Test_DNA
s25_VariantAnnotator_Test_DNA=$(qsub -N s25_VariantAnnotator_Test_DNA -W depend=afterok:$s24_GenomicAnnotator_Test_DNA s25_VariantAnnotator_Test_DNA.sh)
echo $s25_VariantAnnotator_Test_DNA
sleep 0
#s26_VcfToTable_Test_DNA
s26_VcfToTable_Test_DNA=$(qsub -N s26_VcfToTable_Test_DNA -W depend=afterok:$s25_VariantAnnotator_Test_DNA s26_VcfToTable_Test_DNA.sh)
echo $s26_VcfToTable_Test_DNA
sleep 0
#s27_QCReport_demo
s27_QCReport_demo=$(qsub -N s27_QCReport_demo -W depend=afterok:$s17_PicardQCrecal_Test_DNA:$s07_Markduplicates_1:$s18_Coverage_Test_DNA:$s26_VcfToTable_Test_DNA s27_QCReport_demo.sh)
echo $s27_QCReport_demo
sleep 0
#s28_CopyToResultsDir_demo
s28_CopyToResultsDir_demo=$(qsub -N s28_CopyToResultsDir_demo -W depend=afterok:$s00_Fastqc_1:$s16_MergeBam_Test_DNA:$s17_PicardQCrecal_Test_DNA:$s25_VariantAnnotator_Test_DNA:$s26_VcfToTable_Test_DNA:$s27_QCReport_demo s28_CopyToResultsDir_demo.sh)
echo $s28_CopyToResultsDir_demo
sleep 0

touch $DIR/workflow_csv.finished
