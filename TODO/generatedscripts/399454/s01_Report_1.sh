# Header
mkdir -p /Users/mdijkstra/Documents/pompgemak/molgenis_distro/results/report/399454
# save latex template in file
echo "\documentclass[a4paper,12pt]{article}
\usepackage[dutch]{babel}
\usepackage{hyperref}
\usepackage{datetime}
\usepackage{nameref}
\usepackage{grffile}
\usepackage{graphicx}
\usepackage[strings]{underscore}
\usepackage{verbatim}
\usepackage{wrapfig}
\usepackage{lastpage}
\usepackage{palatino}
\usepackage{amsmath}

% Redefine caption name of Table of Content, and Figure and Table
\\\\renewcommand{\\\\contentsname}{Inhoudsopgave}
\\\\renewcommand{\\\\figurename}{Figuur}
\\\\renewcommand{\\\\tablename}{Tabel}

\\\\title{Verbeter je suikerspiegel}
\\\\author{Pompgemak.nl}

\\\\linespread{1.3}

\\\\begin{document}
\\\\maketitle
\\\\thispagestyle{empty}
\\\\vspace{40mm}

\\\\begin{table}[h]
        \\\\centering
        \\\\begin{tabular}{l l}
                \hline
                \multicolumn{2}{l}{\\\\textbf{Gebruiker}} \\\\\\\\
                Naam & Martijn Dijkstra \\\\\\\\
                Pomp ID & 399454 \\\\\\\\
                \\\\\\\\
                \multicolumn{2}{l}{\\\\textbf{Rapport}} \\\\\\\\
                Periode & van ... tot \\\\\\\\
                Aantal pagina's & \\pageref{LastPage} \\\\\\\\
                \hline
        \end{tabular}
\end{table}

\\\\clearpage
\\\\tableofcontents

\\\\clearpage

\section*{Samenvatting}
\\\\addcontentsline{toc}{section}{Samenvatting}

\section*{Introductie}
\\\\addcontentsline{toc}{section}{Introductie}

\section*{Maaltijden}
\\\\addcontentsline{toc}{section}{Maaltijden}

\subsection*{Ontbijt}
\\\\addcontentsline{toc}{subsection}{Ontbijt}

\subsection*{Lunch}
\\\\addcontentsline{toc}{subsection}{Lunch}

\subsection*{Diner}
\\\\addcontentsline{toc}{subsection}{Diner}

\section*{Hypo's en hypers}
\\\\addcontentsline{toc}{section}{Hypo's en hypers}

\\\\begin{figure}[h]
        \\\\caption{Overzicht van je bloedglucosewaarden.}
        \\\\begin{center}
                \\\\includegraphics[width=.9\\\\textwidth]{/Users/mdijkstra/Documents/pompgemak/molgenis_distro/results/report/399454/399454_BGPie.pdf}
        \end{center}
        \label{fig:dry}
\end{figure}


\end{document}
" > /Users/mdijkstra/Documents/pompgemak/molgenis_distro/results/report/399454/399454_report.tex

pdflatex -output-directory=/Users/mdijkstra/Documents/pompgemak/molgenis_distro/results/report/399454 /Users/mdijkstra/Documents/pompgemak/molgenis_distro/results/report/399454/399454_report.tex
pdflatex -output-directory=/Users/mdijkstra/Documents/pompgemak/molgenis_distro/results/report/399454 /Users/mdijkstra/Documents/pompgemak/molgenis_distro/results/report/399454/399454_report.tex 

# Footer
