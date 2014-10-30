#   The path/name of this file is 'java/plugin/eten/voedingsadvies_create_plots.R'.
#   This file is part of the DIADVIES project.
#   
#   Version:     0.1
#   Copyright:   2010 (c) Dr. M. Dijkstra. All rights reserved.
#   Email:       MartijnDijkstra1980@gmail.com

getmycolor = function(x) {
    # assume 0 <= x <= 1
    # 0 returns red, and 1 returns green, else return inbetween
    resolution = 50
    mycolvec = c(rainbow(resolution, start=0, end=.3))
    for (i in 1:resolution) substr(mycolvec[i],8,9) = "AA"
    mycolvec[ x * (resolution-1) + 1]
}

get.barsize.col = function(P, my.AH. = my.ah, my.MAX. = my.max) {
    # 0 nothing
    # 1 ADH
    # 2 MAX
    # 3 2*MAX
    
    i.little = which(0 < P & P <= my.AH.)
    i.lot    = which(!is.na(my.MAX.) & my.AH. < P)
    i.MAX.na = which(is.na(my.MAX.))
    
    barsize = colvec = rep(0, length(P))    
    if (0 < length(i.little)) {
        barsize[i.little] = P[i.little] / my.AH.[i.little]
        barsize.help      = 2 * barsize[i.little] - 1 # make color 'twice as worse'...
        barsize.help      = pmax(0, barsize.help)
        colvec[i.little]  = getmycolor(barsize.help)
    }
    if (0 < length(i.lot))    {
        barsize[i.lot]    = 1 + (P[i.lot] - my.AH.[i.lot])  / (my.MAX.[i.lot] - my.AH.[i.lot])
        barsize[i.lot]    = pmin(3, barsize[i.lot])
        colvec[i.lot]     = getmycolor(pmax(0, 2 - barsize[i.lot]))
    }
    if (0 < length(i.MAX.na)) {
        barsize[i.MAX.na] = P[i.MAX.na] / my.AH.[i.MAX.na]
        barsize[i.MAX.na] = pmin(2, barsize[i.MAX.na])
        
        barsize.help      = barsize[i.MAX.na]
        index             = which(barsize.help < 1)
        if (0 < length(index)) {
            barsize.help[index] = 2 * barsize.help[index] - 1
            barsize.help[index] = pmax(0, barsize.help[index])
        }
        
        colvec[i.MAX.na]  = getmycolor(pmin(1,barsize.help))
    }
    
    list(barsize = barsize, colvec = colvec)
}

add.space = function(vec, add.space.after) {
    for (i in length(add.space.after) : 1) vec = c(vec[1 : add.space.after[i]], NA, vec[(add.space.after[i] + 1) : length(vec)])
    vec
}

plot.food.components = function(P.current, P.advice, imagefile = NA) {
    add.space.after = c(10, 18) #1, 4, 8, 9, 
    my.colors = c('#EEEEFF', '#A0A0FF')
    cex.x.axis          = if (offline) 1 else 3
    cex.y.axis          = if (offline) 1 else 3
 
    bsc = get.barsize.col(P.current)
    barsize = bsc$barsize
    colvec  = bsc$colvec
    
    barsize.advice = get.barsize.col(P.advice)$barsize
    
    barsize         = add.space(barsize, add.space.after)
    colvec          = add.space(colvec, add.space.after)
    x.labels        = names(my.ah)
    x.labels[which(my.ah==0)] = paste(x.labels[which(my.ah==0)], "*", sep='')
    x.labels        = add.space(x.labels, add.space.after)
    barsize.advice  = add.space(barsize.advice, add.space.after)
    
    if (!offline) png(filename = imagefile, width = 4 * 480, height = 1.3 * 480)
        if (offline) {
            par(mai = c(1.3, 1, .2, 0), bg = screen.bg.color)
        } else {
            par(mai = c(3.5, 2.8, .2, 0), bg = screen.bg.color) #mai=c(3.2
        }
        
        y.lim = c(0, 3)
        xpos = barplot(barsize, ylim = y.lim, col = colvec, axes = F, las = 2, names = '')
        x.delta = xpos[2] - xpos[1]
        rect(-xpos[2],  0, tail(xpos,1) + x.delta, 3, col=my.colors[1], border=0)
        rect(-xpos[2],  1, tail(xpos,1) + x.delta, 2, col='#D0D0FF', border=0) # groen: '#D0FFD0' '#D0D0FF'

        # draw horizontal connecting lines
        x.is.na = c(0, which(is.na(barsize)), length(barsize) + 1)
        for (i in 1:(length(x.is.na)-1)) lines(c(xpos[x.is.na[i] + 1] - .44, xpos[x.is.na[i+1] - 1] + .44), rep(par("usr")[3], 2), lwd=8, col="#5B82A4")
        
        # plot 'headers'
        text(mean(xpos[c(1,10)]),  3, 'Algemeen',  col="#D0D0FF", cex=6, pos = 1)
        text(mean(xpos[c(12,19)]), 3, 'Mineralen', col="#D0D0FF", cex=6, pos = 1)
        text(mean(xpos[c(21,30)]), 3, 'Vitaminen', col="#D0D0FF", cex=6, pos = 1)
        if (all(P.current==0)) text(mean(xpos), 1.5, 'Voer meer gegevens in!', col=my.colors[1], cex=6)

        # plot the bars!
        par(new=T)
        xpos = barplot(barsize, ylim = y.lim, col = colvec, axes = F, las = 2, names = '')           

        # show advice
        for (i in 1:length(xpos)) {
            lines(c(xpos[i], xpos[i]), c(barsize[i], barsize.advice[i]), lwd=2, col="#5B82A4")        
            lines(c(xpos[i] - .3 * x.delta, xpos[i] + .3 * x.delta), c(barsize.advice[i], barsize.advice[i]), lwd=4, col="#5B82A4")
        }

        text(xpos, par("usr")[3] - 0.2, srt = 45, adj = 1, labels = x.labels, xpd = TRUE, cex = cex.x.axis)
        text(xpos[1] - 2, par("usr")[3] - par("mai")[1]*.55, "*Neem zo weinig mogelijk van deze stof", adj = 0, xpd = TRUE, cex = cex.x.axis)
        axis(2, at = c(0,1,2,3), labels = c('Niets', 'Aanbevolen', 'Maximum', ''), las = 1, cex.axis = cex.y.axis, lwd = 2)
        #axis(2, at = c(0,1,2), labels = c('Niets', 'Aanbevolen', 'Maximum'), las = 1, cex.axis = cex.y.axis, lwd = 0, lwd.ticks = 2)
    if (!offline) dev.off()
}
#plot.food.components(P.current, P.advice, FigVoedingsstoffenFile)

plot.health.progress = function(day, health.progress, imagefile = NA) {
    my.colors = c('#EEEEFF', '#A0A0FF')
    shift.x.axis = .6
    day   = as.POSIXct(day)
    xlim. = range(day)
    ylim. = c(40, 100)
    if (!offline) png(filename = imagefile, width = 2 * 480, height = 1.3 * 480)
        if (offline) {
            par(mai = c(1.3, 1, .2, 0), bg = screen.bg.color)
        } else {
            par(mai = c(1.5, 1.8, .2, 0), bg = screen.bg.color) #mai=c(3.2
        }
        x = day
        y = health.progress
        plot(x, y, xlab="", ylab="", axes=F, xlim = xlim., ylim = ylim., t='n')
        rect(par("usr")[1], par("usr")[3], par("usr")[2], mean(par("usr")[4]), col=my.colors[1], border=0)        
        
        # mark begin/end
        beginday = max(x[1], beginday)
        endday   = min(tail(x,1), endday)
        rect(beginday, par("usr")[3], endday, mean(par("usr")[4]), col="#D0D0FF", border=0)        

#        text(par("usr")[2], par("usr")[4], 'Gezond',  col="#D0D0FF", cex=6, pos = 1)
#        text(par("usr")[2], par("usr")[3], 'Ongezond',  col="#D0D0FF", cex=6, pos = 3)
        
#        #color increase/decrease
#        for (i in 1:(length(x)-1)) {
#            this.col = if (0 <= diff(y[i+c(0,1)])) "darkgreen" else "darkblue"
#            par(new=T)
#            plot(x[i+c(0,1)], y[i+c(0,1)], xlab="", ylab="", axes=F, xlim = xlim., ylim = ylim., t='l',lwd= if (offline) 3 else 3*3, col= this.col)
#        }

        par(new=T)
        plot(x, y, xlab="", ylab="", axes=F, xlim = xlim., ylim = ylim., t='l',lwd= if (offline) 3 else 3*3, col="darkblue")
        x.txt = tail(x,1)
        y.txt = tail(y,1)
        arrows(x.txt, y.txt + .2 * (100 - y.txt), x.txt, y.txt + .05 * (100 - y.txt), lwd = if (offline) 1 else 3)
        text(x.txt, y.txt + .3 * (100 - y.txt), paste(y.txt,'% ',sep=''), cex = if (offline) 1 else 3)
        axis.POSIXct(1, as.POSIXct(day), cex.axis=if (offline) 1 else 3, padj = shift.x.axis, lwd=2)
        axis(2, las = 2, cex.axis=if (offline) 1 else 3, lwd=2, at = 2:5 * 20)
        title(ylab = "Kwaliteit voeding (%)", cex.lab = if (offline) 1 else 3, line = 6.5)
    if (!offline) dev.off()
}
#offline=F; plot.health.progress(T.food.own.P.cumsum[, "unique.day"], health.progress, "healt.png"); offline=T

make.legend = function(imagefile = 'legend.png') {
    my.colors = c('#EEEEFF', '#A0A0FF')
    cex.x.axis          = if (offline) 1 else 3
    if (!offline) png(filename = imagefile, width = .8 * 480, height = 1.3 * 480)
        bar.width = .45
        bar.space = .2
        bar.xlim  = c(0.1,2.6)

        if (offline) {
            par(mai = c(1.3, 1, .2, 0), bg = screen.bg.color)
        } else {
            par(mai = c(3.2, .5, .2, .1), bg = screen.bg.color)
        }
        
        y.lim = c(0, 3)
        barsize = c(NA, 2.1, 1, .25, NA)
        colvec  = c(NA, getmycolor(c(0, 1, .25)), NA)
        xpos = barplot(barsize, ylim = y.lim, col = colvec, axes = F, las = 2, names = '', width = bar.width, xlim = bar.xlim)
        x.delta = xpos[2] - xpos[1]
        rect(-xpos[2],  0, tail(xpos,1) + x.delta, 3, col=my.colors[1], border=0)
        rect(-xpos[2],  1, tail(xpos,1) + x.delta, 2, col='#D0D0FF', border=0) # groen: '#D0FFD0' '#D0D0FF'

        # draw horizontal connecting lines
        lines(c(xpos[2],xpos[4]), rep(par("usr")[3], 2), lwd=8, col="#5B82A4")

        # plot 'headers'
        text(mean(xpos),  3, 'Uitleg',  col="#D0D0FF", cex=6, pos = 1)
        
        # Plot bars
        par(new=T)
        xpos = barplot(barsize, ylim = y.lim, col = colvec, axes = F, las = 2, names = '', width = bar.width, xlim = bar.xlim)           

        # show advice
        barsize.advice = c(NA, 1.3,1.2,1, NA)
        for (i in 1:length(xpos)) {
            lines(c(xpos[i], xpos[i]), c(barsize[i], barsize.advice[i]), lwd=2, col="#5B82A4")        
            lines(c(xpos[i] - .3 * x.delta, xpos[i] + .3 * x.delta), c(barsize.advice[i], barsize.advice[i]), lwd=4, col="#5B82A4")
        }

        text.pos.x = mean(xpos[c(4)])
        text.pos.y = 1.8
        x.distance = .4
        y.distance = .1
        text(text.pos.x, text.pos.y, 'Na advies', cex = cex.x.axis, col="#5B82A4")
#        lines(c(.66 * text.pos.x, xpos[1] + x.distance * x.delta + .05), c(text.pos.y, text.pos.y), lwd = 2, col="#5B82A4")     
#        lines(c(text.pos.x, xpos[2] + x.distance * x.delta * .85), c(text.pos.y - y.distance*2, barsize.advice[2] + y.distance), lwd = 2, col="#5B82A4")
#        lines(c(text.pos.x, xpos[3] - x.distance * x.delta * .85), c(text.pos.y - y.distance*2, barsize.advice[3] + y.distance), lwd = 2, col="#5B82A4")      
        lines(c(text.pos.x, xpos[4]), c(text.pos.y - y.distance*2, barsize.advice[4] + y.distance), lwd = 2, col="#5B82A4")      
        arrows(text.pos.x, text.pos.y - y.distance*2, xpos[4], barsize.advice[4] + y.distance, lwd = 2, col="#5B82A4")              
    
        x.labels = c('','Te veel', 'Goed', 'Te weinig','')
        text(xpos, par("usr")[3] - 0.2, srt = 45, adj = 1, labels = x.labels, xpd = TRUE, cex = cex.x.axis)
        #axis(2, at = c(0,1,2), labels = c('Niets', 'Aanbevolen', 'Maximum'), las = 1, cex.axis = cex.y.axis, lwd = 0, lwd.ticks = 2)
    if (!offline) dev.off()
}
#make.legend()

plot.basisvoeding = function(basisvoeding.summary, imagefile) {
    my.colors = c('#EEEEFF', '#A0A0FF')
    #eiwit koolh vettotaal vetverz
    vet.verz = basisvoeding.summary[4]
    basisvoeding.summary = basisvoeding.summary[1:3]
    x.axis.labels = c('Eiwit', 'koolhydraten', 'Vet (verzadigd)')
    
    if (!offline) png(filename = imagefile)
        oldmargins = par()$mai 
        margins=c(3,2.3*0.82,.2,0.42)
        par(mai = margins, bg = screen.bg.color)
        if (!is.null(basisvoeding.summary)) {
            xpos = barplot(basisvoeding.summary, col = my.colors[1], names= '', las = 2, axes=F, ylim = c(0, 1.4 * max(basisvoeding.summary)))#1.2
            rect(xpos[3]-.5, 0, xpos[3]+.5, vet.verz, lwd = 3, density=10, col = my.colors[2], border=0)#"darkgray")
            rect(xpos[3]-.5, 0, xpos[3]+.5, vet.verz, lwd = 1, density=10, col = my.colors[2], border=1)
            text(xpos, par("usr")[3] - .1 * max(basisvoeding.summary), srt = 45, adj = 1, labels = x.axis.labels, xpd = TRUE, cex = 3)
            axis(2, at = round(c(0, max(basisvoeding.summary) / 2, max(basisvoeding.summary)),0), las = 2, cex.axis = 3, lwd = 3)
            text(xpos,  basisvoeding.summary + c(.1,.1,.3) * max(basisvoeding.summary), paste(signif(100 * basisvoeding.summary / sum(basisvoeding.summary), 2), '%', sep = ''), cex = 3)
            text(xpos[3],  basisvoeding.summary[3] + .1 * max(basisvoeding.summary), paste('(', signif(100 * vet.verz / sum(basisvoeding.summary), 2), '%)', sep = ''), cex = 2)
            title(ylab = "       gram", cex.lab = 3, line = 7)
        } else {
            plot(1,1,t="n",axes=F,xlab="",ylab="")
            text(1,1,"Voer eerst gegevens in!",cex=3.5,col="darkred")
        }
    if (!offline) dev.off()
}
