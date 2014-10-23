



##### BEFORE #####
touch $PBS_O_WORKDIR/s02_BwaAlignRight_1.out
source $WORKDIR/tools/scripts/import.sh
before="$(date +%s)"
echo "Begin job s02_BwaAlignRight_1 at $(date)" >> $PBS_O_WORKDIR/RUNTIME.log

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

#MOLGENIS walltime=15:00:00 nodes=1 cores=4 mem=6
#FOREACH

module load bwa/0.5.8c_patched

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
getFile $WORKDIR/groups/in-house/projects/demo/output/rawdata/ngs//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_2.fq.gz

mkdir -p "$WORKDIR/tmp//demo/output/"

bwa aln \
$WORKDIR/resources/hg19/indices/human_g1k_v37.chr1.fa \
$WORKDIR/groups/in-house/projects/demo/output/rawdata/ngs//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_2.fq.gz \
-t 4 \
-f $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_2.bwa_align.human_g1k_v37.sai

putFile $WORKDIR/tmp//demo/output//120308_SN163_0457_BD0E5CACXX_L4_CAACCT_2.bwa_align.human_g1k_v37.sai


###### AFTER ######
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo Completed s02_BwaAlignRight_1 at $(date) in $elapsed_seconds seconds >> $PBS_O_WORKDIR/RUNTIME.log
touch $PBS_O_WORKDIR/s02_BwaAlignRight_1.finished
######## END ########

