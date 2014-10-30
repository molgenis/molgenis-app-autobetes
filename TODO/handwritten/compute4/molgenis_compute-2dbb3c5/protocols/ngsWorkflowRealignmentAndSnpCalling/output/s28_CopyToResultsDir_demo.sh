



##### BEFORE #####
touch $PBS_O_WORKDIR/s28_CopyToResultsDir_demo.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s28_CopyToResultsDir_demo at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

echo Running on node: `hostname`

sleep 60
###### MAIN ######

#
# =====================================================
# $Id: CopyToResultsDir.ftl 12202 2012-06-15 09:10:27Z freerkvandijk $
# $URL: http://www.molgenis.org/svn/molgenis_apps/trunk/modules/compute/protocols/CopyToResultsDir.ftl $
# $LastChangedDate: 2012-06-15 11:10:27 +0200 (Fri, 15 Jun 2012) $
# $LastChangedRevision: 12202 $
# $LastChangedBy: freerkvandijk $
# =====================================================
#

#MOLGENIS walltime=23:59:00
#FOREACH project


alloutputsexist "$WORKDIR/groups/in-house/projects/demo/output/results//demo.zip"

# Change permissions

umask 0007


# Make result directories
mkdir -p $WORKDIR/groups/in-house/projects/demo/output/results//rawdata
mkdir -p $WORKDIR/groups/in-house/projects/demo/output/results//alignment
mkdir -p $WORKDIR/groups/in-house/projects/demo/output/results//coverage
mkdir -p $WORKDIR/groups/in-house/projects/demo/output/results//snps
mkdir -p $WORKDIR/groups/in-house/projects/demo/output/results//qc/statistics


# Copy error, out and finished logs to project jobs directory

cp $WORKDIR/groups/in-house/projects/demo/output/jobs//*.out $WORKDIR/groups/in-house/projects/demo/output/logs/
cp $WORKDIR/groups/in-house/projects/demo/output/jobs//*.err $WORKDIR/groups/in-house/projects/demo/output/logs/
cp $WORKDIR/groups/in-house/projects/demo/output/jobs//*.log $WORKDIR/groups/in-house/projects/demo/output/logs/

# Copy project csv file to project results directory

cp $WORKDIR/groups/in-house/projects/demo/output/jobs//demo.csv $WORKDIR/groups/in-house/projects/demo/output/results/


# Create symlinks for all fastq and md5 files to the project results directory

cp -rs $WORKDIR/groups/in-house/projects/demo/output/rawdata/ngs/ $WORKDIR/groups/in-house/projects/demo/output/results//rawdata


# Copy fastQC output to results directory
cp $WORKDIR/tmp//demo/output//*_fastqc.zip $WORKDIR/groups/in-house/projects/demo/output/results//qc


# Copy dedup metrics to results directory
cp $WORKDIR/tmp//demo/output//*.dedup.metrics $WORKDIR/groups/in-house/projects/demo/output/results//qc/statistics


# Create md5 sum and copy merged BAM plus index plus md5 sum to results directory
		md5sum $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam > $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam.md5
		md5sum $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam.bai > $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam.bai.md5
		cp $WORKDIR/tmp//demo/output//Test_DNA.human_g1k_v37.merged.bam* $WORKDIR/groups/in-house/projects/demo/output/results//alignment

# Copy alignment stats (lane and sample) to results directory

cp $WORKDIR/tmp//demo/output//*.alignmentmetrics $WORKDIR/groups/in-house/projects/demo/output/results//qc/statistics
cp $WORKDIR/tmp//demo/output//*.gcbiasmetrics $WORKDIR/groups/in-house/projects/demo/output/results//qc/statistics
cp $WORKDIR/tmp//demo/output//*.insertsizemetrics $WORKDIR/groups/in-house/projects/demo/output/results//qc/statistics
cp $WORKDIR/tmp//demo/output//*.meanqualitybycycle $WORKDIR/groups/in-house/projects/demo/output/results//qc/statistics
cp $WORKDIR/tmp//demo/output//*.qualityscoredistribution $WORKDIR/groups/in-house/projects/demo/output/results//qc/statistics
cp $WORKDIR/tmp//demo/output//*.hsmetrics $WORKDIR/groups/in-house/projects/demo/output/results//qc/statistics
cp $WORKDIR/tmp//demo/output//*.bamindexstats $WORKDIR/groups/in-house/projects/demo/output/results//qc/statistics
cp $WORKDIR/tmp//demo/output//*.pdf $WORKDIR/groups/in-house/projects/demo/output/results//qc/statistics


# Copy coverage stats (for future reference) to results directory

cp $WORKDIR/tmp//demo/output//*.coverage* $WORKDIR/groups/in-house/projects/demo/output/results//coverage


# Copy final vcf and vcf.table to results directory


	cp $WORKDIR/tmp//demo/output//Test_DNA.snps.final.vcf $WORKDIR/groups/in-house/projects/demo/output/results//snps
	cp $WORKDIR/tmp//demo/output//Test_DNA.snps.final.vcf.table $WORKDIR/groups/in-house/projects/demo/output/results//snps


	# Copy genotype array vcf to results directory

	if [ -f "$WORKDIR/tmp//demo/output//Test_DNA.genotypeArray.updated.header.vcf" ]
	then
		cp $WORKDIR/tmp//demo/output//Test_DNA.genotypeArray.updated.header.vcf $WORKDIR/groups/in-house/projects/demo/output/results//qc
	fi

	cp $WORKDIR/tmp//demo/output//Test_DNA.concordance.ngsVSarray.txt $WORKDIR/groups/in-house/projects/demo/output/results//qc


# Copy QC report to results directory

cp $WORKDIR/groups/in-house/projects/demo/output/qc/demo_QCReport.pdf $WORKDIR/groups/in-house/projects/demo/output/results/


# save latex README template in file
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


\begin{document}
\thispagestyle{empty}
\vspace{40mm}


\clearpage
\section*{README}

This zip file contains the following files and directories:
\subsection*{SNPs}
*.snps.final.vcf = All SNP calls in VCF format. (eg. to be used for Cartagenia etc.)

\noindent*.snps.final.vcf.table = All SNP calls in tab-delimited format.

\subsection*{QC}
*_fastqc.zip = Information about the quality of the raw sequence reads. Please read the FastQC manual page for detailed information.

\#\#\#OPTIONAL\#\#\#

\noindent*.genotypeArray.updated.header.vcf = Sample genotype calls in VCF format.


\subsection*{QC/statistics}
*metric files = Metrics used to compile the QC report. For information/documentation about all metrics not explained in the QC report we refer to the Picard documentation.


\subsection*{}


demo.csv = This file contains all the information for the project. If samples were pooled information about the used barcodes etc. can be found here.

\noindent demo_QCReport.pdf = QC report containing all important QC metrics per sample.


\subsection*{}


\subsection*{Additional files}

*.merged.bam = The BAM file containing all (un)aligned reads after removing duplicates and quality score recalibration. 
This is the final alignment on which SNP calling was executed.

\noindent*.merged.bam.bai = The index file of the *.merged.bam.

\noindent*.fq.gz = gzipped FASTQ file containing all reads, if multiple samples were pooled these files contain the demultiplexed reads per flowcell, lane, barcode combination. The exact combination for each sample can be found in the demo.csv file.

\noindent*.fq.md5 = the md5sum for the corresponding FASTQ file.


\subsection*{}


\section*{Documentation}
FastQC: http://www.bioinformatics.babraham.ac.uk/projects/fastqc/Help/
Picard: http://picard.sourceforge.net/picard-metric-definitions.shtml

\end{document}" > $WORKDIR/groups/in-house/projects/demo/output/results//README.txt

pdflatex -output-directory=$WORKDIR/groups/in-house/projects/demo/output/results/ $WORKDIR/groups/in-house/projects/demo/output/results//README.txt


# Create zip file for all "small text" files

cd $WORKDIR/groups/in-house/projects/demo/output/results/

zip -r $WORKDIR/groups/in-house/projects/demo/output/results//demo.zip snps
zip -gr $WORKDIR/groups/in-house/projects/demo/output/results//demo.zip qc
zip -g $WORKDIR/groups/in-house/projects/demo/output/results//demo.zip demo.csv
zip -g $WORKDIR/groups/in-house/projects/demo/output/results//demo.zip README.pdf
zip -g $WORKDIR/groups/in-house/projects/demo/output/results//demo.zip demo_QCReport.pdf

# Create md5sum for zip file

cd $WORKDIR/groups/in-house/projects/demo/output/results/

md5sum demo.zip > $WORKDIR/groups/in-house/projects/demo/output/results//demo.zip.md5

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s28_CopyToResultsDir_demo at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s28_CopyToResultsDir_demo.finished
######## END ########

