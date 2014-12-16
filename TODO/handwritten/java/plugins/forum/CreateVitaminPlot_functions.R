plot.food.components = function(P.current, imagefile = NA) {
    add.space.after = c(10, 18) #1, 4, 8, 9, 
    cex.x.axis          = if (is.na(imagefile)) 1 else 3
    cex.y.axis          = if (is.na(imagefile)) 1 else 3
 
    bsc = get.barsize.col(P.current)
    barsize = bsc$barsize
    colvec  = bsc$colvec
    
    barsize         = add.space(barsize, add.space.after)
    colvec          = add.space(colvec, add.space.after)
    x.labels        = names(my.ah)
    x.labels[which(my.ah==0)] = paste(x.labels[which(my.ah==0)], "*", sep='')
    x.labels        = add.space(x.labels, add.space.after)
    
    if (!is.na(imagefile)) png(filename = imagefile, width = 4 * 480, height = 1.3 * 480)
        if (is.na(imagefile)) {
            par(mai = c(1.3, 1, .2, 0), bg = screen.bg.color)
        } else {
            par(mai = c(3.5, 2.8, .2, 0), bg = screen.bg.color) #mai=c(3.2
        }
        
        y.lim = c(0, 3)
        xpos = barplot(barsize, ylim = y.lim, col = colvec, axes = F, las = 2, names = '')
        x.delta = xpos[2] - xpos[1]
        rect(-xpos[2],  0, tail(xpos,1) + x.delta, 3, col=color.rect.background, border=0)
        rect(-xpos[2],  1, tail(xpos,1) + x.delta, 2, col=color.rect.okay, border=0) # groen: '#D0FFD0' '#D0D0FF'

        # draw horizontal connecting lines
        x.is.na = c(0, which(is.na(barsize)), length(barsize) + 1)
        for (i in 1:(length(x.is.na)-1)) lines(c(xpos[x.is.na[i] + 1] - .44, xpos[x.is.na[i+1] - 1] + .44), rep(par("usr")[3], 2), lwd=8, col= color.horizontal.lines)
        
        # plot 'headers'
        text(mean(xpos[c(1,10)]),  3, 'Algemeen',  col=color.heading, cex=6, pos = 1)
        text(mean(xpos[c(12,19)]), 3, 'Mineralen', col=color.heading, cex=6, pos = 1)
        text(mean(xpos[c(21,30)]), 3, 'Vitaminen', col=color.heading, cex=6, pos = 1)
        if (all(P.current==0)) text(mean(xpos), 1.5, 'Je hebt nog geen voeding ingevoerd!', col=color.no.food, cex=6)

        # plot the bars!
        par(new=T)
        xpos = barplot(barsize, ylim = y.lim, col = colvec, axes = F, las = 2, names = '')           

        text(xpos, par("usr")[3] - 0.2, srt = 45, adj = 1, labels = x.labels, xpd = TRUE, cex = cex.x.axis, col = col.axis.text)
        text(xpos[1] - 2, par("usr")[3] - par("mai")[1]*.55, "*Neem zo weinig mogelijk van deze stof", adj = 0, xpd = TRUE, cex = cex.x.axis, col = col.axis.text)
        axis(2, at = c(0,1,2,3), labels = c('Niets', 'Aanbevolen', 'Maximum', 'Te veel'), las = 1, cex.axis = cex.y.axis, lwd = 2, col = col.axis.text, col.axis = col.axis.text)
        #axis(2, at = c(0,1,2), labels = c('Niets', 'Aanbevolen', 'Maximum'), las = 1, cex.axis = cex.y.axis, lwd = 0, lwd.ticks = 2)
    if (!is.na(imagefile)) dev.off()
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

get.ADH.Max = function(E.my.total = 2500, n.days.no.hunger = 1, path.to.ADH.file = '.') {
    adh = read.csv(paste(path.to.ADH.file, '/ADH_vanaf13jaar.csv', sep=''), sep=';', as.is=T)
    
    # determine respective row
        age.col = as.numeric(adh[2:nrow(adh),1])
        i = 1
        while (age.col[i] < age) i = i + 1
        i = i + (sex == "vrouw") + 1
    
    # Determine upper limits:
        my.adh = as.numeric(adh[i, 3:ncol(adh)])
        names(my.adh) = colnames(adh)[3:ncol(adh)]
        my.max = sapply(my.adh, function(x) NA)
        my.max["VitamineA"]                 = 3000
        my.max["VitamineB3..Nicotinezuur."] = 35
        my.max["VitamineB6"]                = 25
        my.max["VitamineB11..Foliumzuur."]  = 1000
        my.max["VitamineD"]                 = 50
        my.max["VitamineE"]                 = 300
        my.max["Ca"]                        = 2500
        my.max["Se"]                        = 300
        my.max["Zn"]                        = 25
        
    # Eigen aanvulling hiervan op basis van voedingscentrum:
        my.max["VitamineB1"]                = 10
        my.max["VitamineB2"]                = 35    
        my.max["VitamineB12"]               = 40
        my.max["VitamineC"]                 = 3000
        
        my.adh["Na"]                        = 1500
        my.max["Na"]                        = 2400
    
        my.adh["K"]                         = 2600
        my.max["K"]                         = 4700
    
        my.max["Fe"]                        = 45
        
        my.max["Mg"]                        = 660
		
		my.max["P"]							= 4000
            
        
    # Remove the columns of which we don't have data in the food table:
        my.adh = my.adh[-c(8, 14, 16)]
        my.max = my.max[-c(8, 14, 16)]    
    
        adh.min.index = 1:8
        adh.vit.index = 9:18
    
        vit.nice.names = c('Vitamine A', 'Vitamine B1', 'Vitamine B2', 'Nicotinezuur (B3)', 'Vitamine B6', 'Foliumzuur (B11)', 'Vitamine B12', 'Vitamine C', 'Vitamine D', 'Vitamine E')
        min.nice.names = c('Natrium', 'Kalium', 'Calcium', 'IJzer', 'Magnesium', 'Seleen', 'Zink', 'Fosfor')
    
    names(my.adh) = names(my.max) = c(min.nice.names,vit.nice.names)
    # order columns such that they match the colnames in eten.table:
    new.order = c(1, 3:7, 2, 8:11, 13, 15:18, 14, 12)
    my.adh = my.adh[new.order]
    my.max = my.max[new.order]
    
    # Scale to total amount:
    my.adh = my.adh * n.days.no.hunger
    my.max = my.max * n.days.no.hunger

    # Add ADH and MAX for energy, fat, alcohol...
    my.adh.add = my.max.add = NULL
    
    BMI = gebruiker.info$gewicht / (gebruiker.info$lengte / 100)^2
    BMI.upper = if (sex == "man") 25 else 24

    E.my.total.aimed = n.days.no.hunger * 24 * gebruiker.BMR
    
    # The question is whether these depend on the _actual_ energy intake, or on the _aimed_ intake
    # In general: I choose for the _aimed_ intake
    # For 'vezels': I choose for the maximum of the _aimed_ and the _acutal_ intake
    energy.percent = max(E.my.total.aimed, E.my.total) / 100 # <- as energy.percent, I choose the maximum of the actual intake and of the amount that one whom never sports should eat.
    fat.energy  = 9 # 9 Cal per gram
    carb.energy = 4
    prot.energy = 4
    
    my.adh.add["Energie"]       = E.my.total.aimed
    my.max.add["Energie"]       = E.my.total.aimed * 1.2

    my.adh.add["Eiwit"]         = 10 * energy.percent / prot.energy # min scaled below to adh # deze min-E% zijn van <- van modifast.nl; maar minima zijn geen adh!
    my.max.add["Eiwit"]         = 60 * energy.percent / prot.energy 

    my.adh.add["Koolhydraten"]   = 40 * energy.percent / carb.energy # min scaled below to adh
    my.max.add["Koolhydraten"]   = 70 * energy.percent / carb.energy 
        
    my.adh.add["Vet (totaal)"]  = 20 * energy.percent / fat.energy  # min scaled below to adh
    if (BMI < BMI.upper) {
        my.max.add["Vet (totaal)"] = 40 * energy.percent / fat.energy
    } else {
        my.max.add["Vet (totaal)"] = 35 * energy.percent / fat.energy
    }

    ### Scale minimum up to ADH such that their total sums to 100%  ( (10 + 40 + 20) / 100 = .7 ):
    my.adh.add["Eiwit"]         = my.adh.add["Eiwit"]         / .7
    my.adh.add["Koolhydraten"]  = my.adh.add["Koolhydraten"]  / .7
    my.adh.add["Vet (totaal)"]  = my.adh.add["Vet (totaal)"]  / .7

    my.adh.add["Verzadigd vet"] = 0
    my.max.add["Verzadigd vet"] = 10 * energy.percent / fat.energy
    
    my.adh.add["Transvet"]      = 0
    my.max.add["Transvet"]      = 1 * energy.percent / fat.energy
    
    my.adh.add["Linolzuur"]     = 2  * energy.percent / fat.energy
    my.max.add["Linolzuur"]     = 12 * energy.percent / fat.energy # Max 12% van totale dagelijkse energie (http://www.voedingscentrum.nl/encyclopedie/onverzadigd-vet.aspx)

    my.adh.add["Cholesterol"]   = 0
    my.max.add["Cholesterol"]   = n.days.no.hunger * 300 # dit zijn mg toch?

    my.adh.add["Alcohol"]       = 0
    my.max.add["Alcohol"]       = n.days.no.hunger * if (sex == "man") 20 else 10

    my.adh.add["Vezels"]        = 3.4 * max(E.my.total, E.my.total.aimed)/ 239 # voedingscentrum: 3.4g / MJ
                                  # For the adviced amount 'vezels': I choose for the maximum of the _aimed_ and the _acutal_ intake
    my.max.add["Vezels"]        = 5 * my.adh.add["Vezels"] # <-zelf ingevuld!

    return(list(ah = c(my.adh.add, my.adh), max = c(my.max.add, my.max)))
}

getmycolor = function(x) {
    # assume 0 <= x <= 1
    # 0 returns red, and 1 returns green, else return inbetween
    resolution = 50
    mycolvec = c(rainbow(resolution, start=0, end=.3))
    for (i in 1:resolution) substr(mycolvec[i],8,9) = advice.transparency
    mycolvec[ x * (resolution-1) + 1]
}

add.space = function(vec, add.space.after) {
    for (i in length(add.space.after) : 1) vec = c(vec[1 : add.space.after[i]], NA, vec[(add.space.after[i] + 1) : length(vec)])
    vec
}

productToStandard = function(P) {
	names(P) = tolower(names(P))
	
	result = my.ah # get 'standard vector'
	result["Energie"] = P["energie"]
	result["Eiwit"] = P["eiwit"]
	result["Koolhydraten"] = P["koolhydraat"]
	result["Vet (totaal)"] = P["vettotaal"]
	result["Verzadigd vet"] = P["vetverzadigd"]
	#result[""] = P["vetonverzadigd"]
	result["Transvet"] = P["vetzurentrans"]
	result["Linolzuur"] = P["linolzuur"]
	result["Cholesterol"] = P["cholesterol"]
	result["Alcohol"] = P["alcohol"]
	result["Vezels"] = P["vezels"]
	result["Natrium"] = P["na"]
	result["Calcium"] = P["ca"]
	result["IJzer"] = P["fe"]
	result["Magnesium"] = P["mg"]
	result["Seleen"] = P["se"]
	result["Zink"] = P["zn"]
	result["Kalium"] = P["k"]
	result["Fosfor"] = P["p"]
	result["Vitamine A"] = P["vitaminea"]
	result["Vitamine B1"] = P["vitamineb1"]
	result["Vitamine B2"] = P["vitamineb2"]
	result["Vitamine B6"] = P["vitamineb6"]
	result["Vitamine B12"] = P["vitamineb12"]
	result["Vitamine C"] = P["vitaminec"]
	result["Vitamine D"] = P["vitamined"]
	result["Vitamine E"] = P["vitaminee"]
	result["Foliumzuur (B11)"] = P["foliumzuur"]
	result["Nicotinezuur (B3)"] = P["nicotz"]
	#result[""] = P["totmdisac"]
	#result[""] = P["totpdisac"]
	#result[""] = P["eiwitp"]
	#result[""] = P["peiw"]
	
	# convert list to 'named vector'
	resultVec = as.numeric(result)
	names(resultVec) = names(result)
	
	resultVec
}

getAmounts = function(pid, weight) {
	weight/100 * productToStandard(find.product(id = pid))
}