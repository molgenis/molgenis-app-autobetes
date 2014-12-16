DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

touch $DIR/workflow_csv.started
export PBS_O_WORKDIR=${DIR}
echo Starting with s00_Fastqc_1...
sh s00_Fastqc_1.sh
#Dependencies: 

echo Starting with s01_BwaAlignLeft_1...
sh s01_BwaAlignLeft_1.sh
#Dependencies: 

echo Starting with s02_BwaAlignRight_1...
sh s02_BwaAlignRight_1.sh
#Dependencies: 

echo Starting with s03_BwaSampe_1...
sh s03_BwaSampe_1.sh
#Dependencies: 

echo Starting with s04_SamToBam_1...
sh s04_SamToBam_1.sh
#Dependencies: 

echo Starting with s05_SamSort_1...
sh s05_SamSort_1.sh
#Dependencies: 

echo Starting with s06_PicardQC_1...
sh s06_PicardQC_1.sh
#Dependencies: 

echo Starting with s07_Markduplicates_1...
sh s07_Markduplicates_1.sh
#Dependencies: 

echo Starting with s08_RealignTargetCreator_1...
sh s08_RealignTargetCreator_1.sh
#Dependencies: 

echo Starting with s09_Realign_1...
sh s09_Realign_1.sh
#Dependencies: 

echo Starting with s10_Fixmates_1...
sh s10_Fixmates_1.sh
#Dependencies: 

echo Starting with s11_CovariatesBefore_1...
sh s11_CovariatesBefore_1.sh
#Dependencies: 

echo Starting with s12_Recalibrate_1...
sh s12_Recalibrate_1.sh
#Dependencies: 

echo Starting with s13_SamSortRecal_1...
sh s13_SamSortRecal_1.sh
#Dependencies: 

echo Starting with s14_CovariatesAfter_1...
sh s14_CovariatesAfter_1.sh
#Dependencies: 

echo Starting with s15_AnalyzeCovariates_1...
sh s15_AnalyzeCovariates_1.sh
#Dependencies: 

echo Starting with s16_MergeBam_Test_DNA...
sh s16_MergeBam_Test_DNA.sh
#Dependencies: 

echo Starting with s17_PicardQCrecal_Test_DNA...
sh s17_PicardQCrecal_Test_DNA.sh
#Dependencies: 

echo Starting with s18_Coverage_Test_DNA...
sh s18_Coverage_Test_DNA.sh
#Dependencies: 

echo Starting with s19_CoverageGATK_Test_DNA...
sh s19_CoverageGATK_Test_DNA.sh
#Dependencies: 

echo Starting with s20_IndelGenotyper_Test_DNA...
sh s20_IndelGenotyper_Test_DNA.sh
#Dependencies: 

echo Starting with s21_FilterIndels_Test_DNA...
sh s21_FilterIndels_Test_DNA.sh
#Dependencies: 

echo Starting with s22_UnifiedGenotyper_Test_DNA...
sh s22_UnifiedGenotyper_Test_DNA.sh
#Dependencies: 

echo Starting with s23_MakeIndelMask_Test_DNA...
sh s23_MakeIndelMask_Test_DNA.sh
#Dependencies: 

echo Starting with s24_GenomicAnnotator_Test_DNA...
sh s24_GenomicAnnotator_Test_DNA.sh
#Dependencies: 

echo Starting with s25_VariantAnnotator_Test_DNA...
sh s25_VariantAnnotator_Test_DNA.sh
#Dependencies: 

echo Starting with s26_VcfToTable_Test_DNA...
sh s26_VcfToTable_Test_DNA.sh
#Dependencies: 

echo Starting with s27_QCReport_demo...
sh s27_QCReport_demo.sh
#Dependencies: 

echo Starting with s28_CopyToResultsDir_demo...
sh s28_CopyToResultsDir_demo.sh
#Dependencies: 

