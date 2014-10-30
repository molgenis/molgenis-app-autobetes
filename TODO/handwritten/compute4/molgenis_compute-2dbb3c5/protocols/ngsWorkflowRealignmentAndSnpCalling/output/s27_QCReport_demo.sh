



##### BEFORE #####
touch $PBS_O_WORKDIR/s27_QCReport_demo.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s27_QCReport_demo at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=00:05:00
#FOREACH project
#DOCUMENTATION Documentation of QCReport.ftl, $WORKDIR/tools/getStatistics_20121127/getStatistics.R


# We need some parameters folded per sample:

# parameters in *.tex template:

inputs "$WORKDIR/tmp//demo/output//Test_DNA.hsmetrics"
inputs "$WORKDIR/tmp//demo/output//Test_DNA.alignmentmetrics"
inputs "$WORKDIR/tmp//demo/output//Test_DNA.insertsizemetrics"
inputs "$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.dedup.metrics"
inputs "$WORKDIR/tmp//demo/output//Test_DNA.concordance.ngsVSarray.txt"
inputs $WORKDIR/tools/getStatistics_20121127/NiceColumnNames.csv

export PATH=$WORKDIR/tools/R//bin:${PATH}
export R_LIBS=$WORKDIR/tools/GATK-1.3-24-gc8b1c92/gsalib/

# get general sample statistics
Rscript $WORKDIR/tools/getStatistics_20121127/getStatistics.R \
--hsmetrics "$WORKDIR/tmp//demo/output//Test_DNA.hsmetrics" \
--alignment "$WORKDIR/tmp//demo/output//Test_DNA.alignmentmetrics" \
--insertmetrics "$WORKDIR/tmp//demo/output//Test_DNA.insertsizemetrics" \
--dedupmetrics "$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.dedup.metrics" \
--concordance "$WORKDIR/tmp//demo/output//Test_DNA.concordance.ngsVSarray.txt" \
--sample "Test_DNA" \
--colnames $WORKDIR/tools/getStatistics_20121127/NiceColumnNames.csv \
--csvout $WORKDIR/groups/in-house/projects/demo/output/qc/demo_QCStatistics.csv \
--tableout $WORKDIR/groups/in-house/projects/demo/output/qc/demo_qcstatisticstable.tex \
--descriptionout $WORKDIR/groups/in-house/projects/demo/output/qc/demo_qcstatisticsdescription.tex \
--baitsetout $WORKDIR/groups/in-house/projects/demo/output/qc/projectbaitset.txt \
--qcdedupmetricsout $WORKDIR/groups/in-house/projects/demo/output/qc/dedupmetrics.txt

# get dedup info per flowcell-lane-barcode/sample
Rscript $WORKDIR/tools/scripts/getDedupInfo_23mar2012/getDedupInfo.R \
--dedupmetrics "$WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT.human_g1k_v37.dedup.metrics" \
--flowcell "BD0E5CACXX" \
--lane "4" \
--sample "Test_DNA" \
--paired TRUE \
--qcdedupmetricsout "$WORKDIR/groups/in-house/projects/demo/output/qc/dedupmetrics.txt"

# get snp stats per sample
Rscript $WORKDIR/tools/scripts/createSNPTable_19mar2012/createSNPTable.R \
--sample "Test_DNA" \
--type "$WORKDIR/tmp//demo/output//Test_DNA.snps.final.type.txt" \
--class "$WORKDIR/tmp//demo/output//Test_DNA.snps.final.class.txt" \
--impact "$WORKDIR/tmp//demo/output//Test_DNA.snps.final.impact.txt" \
--typetableout "$WORKDIR/tmp//demo/output//demo.snps.final.type.tex" \
--classtableout "$WORKDIR/tmp//demo/output//demo.snps.final.class.tex" \
--impacttableout "$WORKDIR/tmp//demo/output//demo.snps.final.impact.tex"


# create workflow figure
echo "digraph G {Fastqc; BwaAlignLeft; BwaAlignRight; BwaSampe; BwaAlignLeft->BwaSampe;BwaAlignRight->BwaSampe;SamToBam; BwaSampe->SamToBam;SamSort; SamToBam->SamSort;PicardQC; SamSort->PicardQC;Markduplicates; SamSort->Markduplicates;RealignTargetCreator; Markduplicates->RealignTargetCreator;Realign; RealignTargetCreator->Realign;Fixmates; Realign->Fixmates;CovariatesBefore; Fixmates->CovariatesBefore;Recalibrate; Fixmates->Recalibrate;CovariatesBefore->Recalibrate;SamSortRecal; Recalibrate->SamSortRecal;CovariatesAfter; SamSortRecal->CovariatesAfter;AnalyzeCovariates; CovariatesBefore->AnalyzeCovariates;CovariatesAfter->AnalyzeCovariates;MergeBam; SamSortRecal->MergeBam;PicardQCrecal; MergeBam->PicardQCrecal;Coverage; MergeBam->Coverage;CoverageGATK; MergeBam->CoverageGATK;IndelGenotyper; MergeBam->IndelGenotyper;FilterIndels; IndelGenotyper->FilterIndels;UnifiedGenotyper; MergeBam->UnifiedGenotyper;MakeIndelMask; FilterIndels->MakeIndelMask;GenomicAnnotator; UnifiedGenotyper->GenomicAnnotator;VariantAnnotator; GenomicAnnotator->VariantAnnotator;VcfToTable; VariantAnnotator->VcfToTable;QCReport; PicardQCrecal->QCReport;Markduplicates->QCReport;Coverage->QCReport;VcfToTable->QCReport;CopyToResultsDir; Fastqc->CopyToResultsDir;MergeBam->CopyToResultsDir;PicardQCrecal->CopyToResultsDir;VariantAnnotator->CopyToResultsDir;VcfToTable->CopyToResultsDir;QCReport->CopyToResultsDir;}" | $WORKDIR/tools/graphviz-2.28.0/bin/dot -Tpng > $WORKDIR/groups/in-house/projects/demo/output/qc/demo_workflow.png

# save latex template in file
echo "\documentclass[a4paper,12pt]{article}
\usepackage{nameref}
\usepackage{grffile}
\usepackage{graphicx}
\usepackage[strings]{underscore}
\usepackage{verbatim}
\usepackage{wrapfig}
\usepackage{lastpage}

\begin{comment}
#
# =====================================================
# $Id$
# $URL$
# $LastChangedDate$
# $LastChangedRevision$
# $LastChangedBy$
# =====================================================
#
\end{comment}

\newenvironment{narrow}[2]{
  \begin{list}{}{
    \setlength{\leftmargin}{#1}
    \setlength{\rightmargin}{#2}
    \setlength{\listparindent}{\parindent}
    \setlength{\itemindent}{\parindent}
    \setlength{\parsep}{\parskip}
  }
  \item[]
}{\end{list}}

\title{Next Generation Sequencing report}
\author{\small Genome Analysis Facility (GAF), Genomics Coordination Centre (GCC)\\\\
\small University Medical Centre Groningen}

\begin{document}
\maketitle
\thispagestyle{empty}
\vspace{40mm}

\begin{table}[h]
	\centering
	\begin{tabular}{l l}
		\hline
		\multicolumn{2}{l}{\textbf{Report}} \\\\
		Created on & \today \\\\
		Number of pages & \\pageref{LastPage} \\\\ \\\\
		Generated by & MOLGENIS Compute \\\\
		\\\\
		\multicolumn{2}{l}{\textbf{Project}} \\\\
		Project name & demo \\\\
		Number of samples & 1 \\\\
		\\\\
		\multicolumn{2}{l}{\textbf{Customer}} \\\\
		Principal investigator & researcher \\\\
		\\\\
		\multicolumn{2}{l}{\textbf{Contact}} \\\\
		Name & Cleo C. van Diemen \\\\
		E-mail & c.c.van.diemen@umcg.nl \\\\
		\hline
	\end{tabular}
\end{table}

\clearpage
\tableofcontents

\clearpage
\section*{Introduction}
\addcontentsline{toc}{section}{Introduction}
This report describes a series of statistics about your sequencing data. Together with this report you'll receive a SNP-list. If you, in addition, also want the raw data, then please notify us via e-mail. In any case we'll delete the raw data, three months after \today.

\clearpage
\section*{Project analysis results}
\addcontentsline{toc}{section}{Project analysis results}

\subsection*{Overview statistics}
\addcontentsline{toc}{subsection}{Overview statistics}
\label{subsect:overviewstatistics}
% statistics table
\input{$WORKDIR/groups/in-house/projects/demo/output/qc/demo_qcstatisticstable.tex}

\begin{minipage}{\textwidth}
	Name of the bait set(s) used in the hybrid selection for this project:\\\\
	\textbf{\input{$WORKDIR/groups/in-house/projects/demo/output/qc/projectbaitset.txt}}
\end{minipage}

\clearpage
\subsection*{Description statistics table}
\addcontentsline{toc}{subsection}{Description statistics table}
\begin{table}[h!]
	\centering
	\begin{tabular}{r p{12cm}}
		\input{$WORKDIR/groups/in-house/projects/demo/output/qc/demo_qcstatisticsdescription.tex}
	\end{tabular}
\end{table}

\clearpage
\subsection*{Capturing}
\addcontentsline{toc}{subsection}{Capturing}
The following figures show the cumulative depth distribution in the target regions that are located on \emph{chromosome 1}. The fractions of bases that is covered with at least 10x, 20x and 30x are marked with a dot. Please see section \"\\nameref{subsect:overviewstatistics}\" for the full coverage statistics per sample; \emph{i.e.}, in the target regions on \emph{all chromosomes}.
\begin{figure}[ht]\begin{minipage}{0.5\linewidth}\caption{sample \textbf{Test_DNA}}\centering\includegraphics[width=\textwidth]{$WORKDIR/tmp//demo/output//Test_DNA.coverageplot.pdf}\end{minipage}\hspace{1cm}\end{figure}

\clearpage
\subsection*{Insert size distribution}
\addcontentsline{toc}{subsection}{Insert size distribution}
The following figures show the insert size distribution per sample. Insert refers to the base pairs that are ligated between the adapters.
\begin{figure}[ht]\begin{minipage}{0.5\linewidth}\caption{sample \textbf{Test_DNA}}\centering\includegraphics[width=\textwidth]{$WORKDIR/tmp//demo/output//Test_DNA.insertsizemetrics.pdf}\end{minipage}\hspace{1cm}\end{figure}

%\clearpage
%\subsection*{Demultiplex statistics}
%\addcontentsline{toc}{subsection}{Demultiplex statistics}
%Under construction...
%displaystats(demultiplexstats)

%\clearpage
%\subsection*{GC metrics}
%\addcontentsline{toc}{subsection}{GC metrics}
%The following figures show the GC-content distribution per sample.
%\begin{figure}[ht]\begin{minipage}{0.5\linewidth}\caption{sample \textbf{Test_DNA}}\centering\includegraphics[width=\textwidth]{$WORKDIR/tmp//demo/output//Test_DNA.gcbiasmetrics.pdf}\end{minipage}\hspace{1cm}\end{figure}

%\clearpage
%\subsection*{SNP statistics}
%\addcontentsline{toc}{subsection}{SNP statistics}
%The tables with caption 'Functional type', 'Functional class' and 'Functional impact', classify the SNPs, based on Ensembl, build 37.64.
%\input{$WORKDIR/tmp//demo/output//demo.snps.final.type.tex}
%\input{$WORKDIR/tmp//demo/output//demo.snps.final.class.tex}
%\input{$WORKDIR/tmp//demo/output//demo.snps.final.impact.tex}

\clearpage
\subsection*{Duplication rates}
\addcontentsline{toc}{subsection}{Duplication rates}
\input{$WORKDIR/groups/in-house/projects/demo/output/qc/dedupmetrics.txt}

\clearpage
\section*{Appendix 1: Genome Analysis Facility Pipeline}
\addcontentsline{toc}{section}{Appendix 1: Genome Analysis Facility Pipeline}
\subsection*{Exome sequencing}
\addcontentsline{toc}{subsection}{Exome sequencing}
\begin{wrapfigure}{r}{0.5\textwidth}
	\begin{center}
		\includegraphics[width=.5\textwidth]{$WORKDIR/tools/getStatistics_20121127/GAFpipeline.png}
	\end{center}
	\caption{Workflow in the lab}
	\label{fig:wet}
\end{wrapfigure}
Figure \ref{fig:wet} illustrated the basic experimental process of exome capture sequencing. The Genomic DNA sample was randomly fragmented using Nebulisation. Then barcoded adapters were ligated to both ends of the resulting fragments, according the standard New England Biolabs protocol. Fragments with an insert size of 220 bp on average were excised using the Caliper XT gel system and the extracted DNA was amplified with PCR.

The quality of the product was verified on the BioRad Experion instrument. If the quality of the product meets the criteria, the product is multiplexed in an equimolar pool of 4 simular products. This pool is hybridized to the Agilent SureSelect All exon V2, according the provided protocol. After amplification of the enriched products with PCR the quality of the products is verified on the BioRad Experion instrument and Paired End sequenced on the HiSeq2000 with 100 bp reads. Image Files were processed using standard Illumina basecalling software and the generated reads are ready for downstream processing after demultiplexing.

\clearpage
\section*{Appendix 2: Bioinformatics pipeline}
\addcontentsline{toc}{section}{Appendix 2: Bioinformatics pipeline}
Your samples have been anlayzed with bioinformatics pipeline shown in Figure \ref{fig:dry}.
\begin{figure}[h]
	\caption{Bioinformatics pipeline. The ovals describe the steps in the pipeline. The arrows indicate the work flow of data between the steps.}
	\begin{center}
		\includegraphics[width=.9\textwidth]{$WORKDIR/groups/in-house/projects/demo/output/qc/demo_workflow.png}
	\end{center}
	\label{fig:dry}
\end{figure}
\end{document}" > $WORKDIR/groups/in-house/projects/demo/output/qc/demo_QCReport.tex

pdflatex -output-directory=$WORKDIR/groups/in-house/projects/demo/output/qc $WORKDIR/groups/in-house/projects/demo/output/qc/demo_QCReport.tex
pdflatex -output-directory=$WORKDIR/groups/in-house/projects/demo/output/qc $WORKDIR/groups/in-house/projects/demo/output/qc/demo_QCReport.tex 

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s27_QCReport_demo at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s27_QCReport_demo.finished
######## END ########

