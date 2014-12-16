R --slave <<EOF

${sourceRApi}


#
##
### Overview BG values
##
#
bg = find.bgmeter()[,4]
bg = bg[which(0 < bg)]

# determine number of bars (width = 1)
breaks = 0:ceiling(max(bg))

# determine xlim, ylim
h = hist(bg, breaks=breaks, right=F, plot=F)
ylim = c(0, max(h[["counts"]]))
xlim = c(0, ceiling(max(bg)))

# categorize bg
bg.cat = c("Ernstige hypo (< 2.8 mM)", "Hypo (< 4 mM)", "Normaal", "Hyper (> 10 mM)", "Ernstige hyper (> 15 mM)")

bg.list = list()
bg.list[[1]] = bg[which(bg < 2.8)]
bg.list[[2]] = bg[which(bg < 4)]
bg.list[[3]] = bg[which(4 <= bg & bg < 10)]
bg.list[[4]] = bg[which(10 <= bg & bg < 15)]
bg.list[[5]] = bg[which(15 <= bg)]

# colors for severe hypo, hypo, norm, hyper, severe hyper
bg.col = c("cadetblue4", "lightblue", "lightgreen", "indianred3", "indianred4")


pdf("${BGPiePdf}")

par(mai=c(1.1,1.2,0,0))

for (i in c(2,1,3:length(bg.cat)))
{
	if (i != 2) par(new=T)
	hist(bg.list[[i]], breaks=breaks, right=F, xlim=xlim, ylim=ylim, lwd=2, col=bg.col[i], axes=F, xlab="", ylab="", main="")
}

axis(1, at=c(0, 4, seq(10, max(bg), by=5)), cex.axis=2, padj=.5, lwd=2)
axis(2, las=2, cex.axis=2, padj=.5, lwd=2)
title(xlab="BG (mM)", ylab="Aantal metingen", cex.lab=2, line=4)

legend("topright", legend=bg.cat, lwd=10, col=bg.col, border=F, box.lty=0)

dev.off()

EOF