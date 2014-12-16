



##### BEFORE #####
touch $PBS_O_WORKDIR/s00_Fastqc_1.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s00_Fastqc_1 at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=08:00:00 nodes=1 cores=1 mem=1
#FOREACH

module load fastqc/v0.7.0

    getFile "$WORKDIR/groups/in-house/projects/demo/output/rawdata/ngs//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_1.fq.gz"
    getFile "$WORKDIR/groups/in-house/projects/demo/output/rawdata/ngs//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_2.fq.gz"

# first make logdir...
mkdir -p "$WORKDIR/tmp//demo/output/"
mkdir -p "$WORKDIR/tmp/processing/"

# pair1
fastqc $WORKDIR/groups/in-house/projects/demo/output/rawdata/ngs//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_1.fq.gz \
-Djava.io.tmpdir=$WORKDIR/tmp/processing/ \
-Dfastqc.output_dir=$WORKDIR/tmp//demo/output/ \
-Dfastqc.unzip=false

# pair2
fastqc $WORKDIR/groups/in-house/projects/demo/output/rawdata/ngs//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_2.fq.gz \
-Djava.io.tmpdir=$WORKDIR/tmp/processing/ \
-Dfastqc.output_dir=$WORKDIR/tmp//demo/output/ \
-Dfastqc.unzip=false

      putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_1.fq_fastqc.zip
      putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_1_fastqcsummary.txt
      putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_1_fastqcsummary.log
      putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_2.fq_fastqc.zip
      putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_2_fastqcsummary.txt
      putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_2_fastqcsummary.log

###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s00_Fastqc_1 at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s00_Fastqc_1.finished
######## END ########

