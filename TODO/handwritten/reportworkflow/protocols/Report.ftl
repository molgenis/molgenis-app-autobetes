# save latex template in file
echo "<#include "ReportTemplate.tex"/>" > ${texReport}

pdflatex -output-directory=${reportDir} ${texReport}
pdflatex -output-directory=${reportDir} ${texReport} <#--do twice to fill all cross references-->