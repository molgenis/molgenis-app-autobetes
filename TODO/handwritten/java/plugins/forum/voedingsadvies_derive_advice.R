#   The path/name of this file is 'java/plugin/eten/voedingsadvies_derive_advice.R'.
#   This file is part of the DIADVIES project.
#   
#   Version:     0.1
#   Copyright:   2010 (c) Dr. M. Dijkstra. All rights reserved.
#   Email:       MartijnDijkstra1980@gmail.com

round.days = function(x) {
    if (x < 3.5) return(round(2*x)/2)
    return(round(x))
}
round.grams = function(x) {
    if (x <=  20) return(round(x))              # round upto wholes
    if (x <=  50) return(round( x/5 ) * 5)      # round upto 5's
    if (x <= 150) return(round( x/10 ) * 10)    # round upto 10's
    return(round( x/50 ) * 50)                  # else round upto 50's
}

factor.to.text  = function(f) {
    if (f < .5)     return("veel minder vaak")
    if (f < .75)     return("minder vaak")
    if (f < 1)      return("wat minder vaak")
    if (f < 2)      return("wat vaker")
    if (f < 3)      return("vaker")
    return("veel vaker")
}

color.text = function(txt, val) paste('<font color=', if (val=='good') advice.color.positive else advice.color.negative,'>', txt, '</font>', sep='')

advice.to.id = function(advice.list) {
    id.vec = NULL
    if (0 < length(advice.list)) for (i in 1:length(advice.list)) {
        id.vec = c(id.vec, as.numeric(advice.list[[i]]$product.id))
    }
    id.vec
}

replace.blanks = function(wrd) gsub(wrd, pattern=' ', replacement="&nbsp;")

advice.to.text = function(advice.list) {
    advice.text.list = NULL
    
    # create a table with the **eenheid name** and the **eenheid gewicht** for each of the products in the advice
    if (0 < length(advice.list)) {
        ids = NULL
        for (i in 1:length(advice.list)) ids = c(ids, advice.list[[i]]$product.id)
        mat = find.product(id = ids)

        eenheidtable = NULL
        for (i in 1:length(ids)) {
            index = which(mat$id == ids[i])
            if (0 < length(index)) {
                # this is a product
                vec = c(ids[i], mat[index, "eenheid"], mat[index, "eenheidgewicht"])
                if (is.na(vec[2]) | vec[2] == "") vec[2] = "eenheid"
            } else {
                # this is a gerecht
                vec = c(ids[i], "eenheid", "")
            }          
            
            eenheidtable = rbind(eenheidtable, vec)
        }
    }
        
    if (0 < length(advice.list)) for (i in 1:length(advice.list)) {
        advice = advice.list[[i]]
        if (advice$type == "nothing") {
            advice.text.list[i] = ""
        } else {
            # product name:
            product.name = T.product[which(T.product[, "id"] == advice$product.id), "productnaam"]
            product.name = replace.blanks(product.name)
            if (advice$type == "scale") {
                if (advice$factor == 0) {
                    advice.text.list[i] = paste("Probeer voorlopig <b>", color.text(paste('geen', product.name), 'bad'), "</b>te nemen.")
                } else {
                    direction       = factor.to.text(advice$factor)
                    n.per.day       = n.times.eaten.in.period(gebruikerID, beginday, endday, advice$product.id, T.food) / n.days.no.hunger
                    daily.amount    = T.advice.own[ which(T.advice.own[, "product.id"] == advice$product.id), "weight"] / n.days.no.hunger
                    #amount          = round.grams(daily.amount / n.per.day)
                    
                    # n.units :: grams / the weight of one eenheid of this food
                    grams       = daily.amount / n.per.day
                    unit.weight = as.numeric(eenheidtable[i, 3])
                    
                    if (is.na(unit.weight)) unit.weight = grams
                    #### FOUT:
                    #                print("XXXX FOUT in advice.to.text << unit.weight van een gerecht moet gewoon uit DB gehaadl worden als dit werkt")
                    # <--- is dat wel zo? Op de huidige manier krijg je toch juist het 'unit.weight' wat gangbaar is? (...) weet niet zeker
                    #### :FOUT
                    
                    n.units     = round.days(grams / unit.weight)
                    unit.name   = eenheidtable[i, 2]
                   
                    current.n.per.day = round.days(1/n.per.day)
                    advice.n.per.day  = round.days(1/(n.per.day*advice$factor))
                    if (current.n.per.day == advice.n.per.day) {
                        current.n.per.day = round(1/n.per.day,1)
                        advice.n.per.day  = round(1/(n.per.day*advice$factor),1)
                    }
                    advice.text.list[i] =
                        paste("Probeer <b>",
                        color.text(paste(direction, product.name), if (1 < advice$factor) 'good' else 'bad'),
                        "</b> te nemen. Neem bv. <B>per ", advice.n.per.day, " dag(en) ", 
                        n.units, " keer een ", unit.name, 
                        if (unit.weight != "") paste(" &agrave;", unit.weight, "gram</B>.") else ".")
                        # amount, "gram</b>. i.p.v. eens per", current.n.per.day,"dag(en) nu.")
                }
            } else if (advice$type == "tip") {
                # derive current frequency of others
                n.per.day       = T.overview.everybody[ which(T.overview.everybody[, "product.id"] == advice$product.id), "n.per.day.75%"]
        
                # derive amount (portion)
                daily.amount    = T.advice.everybody[which(T.advice.everybody[, "product.id"] == advice$product.id), "weight"] / n.days.no.hunger
                #amount          = round.grams(daily.amount / n.per.day)
                
                grams       = daily.amount / n.per.day
                unit.weight = as.numeric(eenheidtable[i, 3])
                unit.name   = eenheidtable[i, 2]

                if (is.na(unit.weight)) unit.weight = grams
                #### FOUT:
                #                print("XXXX FOUT in advice.to.text << unit.weight van een gerecht moet gewoon uit DB gehaadl worden als dit werkt")
                # <--- is dat wel zo? Op de huidige manier krijg je toch juist het 'unit.weight' wat gangbaar is? (...) weet niet zeker
                #### :FOUT
                
                # scale the frequency:
                n.per.day       = n.per.day * advice$factor
                
                product.name = color.text(product.name, 'good')
                if (n.per.day <= 1) {
                    once.per.n.days = if (1 / n.per.day < 2) round.days(1 / n.per.day) else round(1 / n.per.day)
                    #advice.text.list[i] = paste("Tip: probeer <b>eens per", once.per.n.days, "dag(en)", amount, "gram", product.name, "</b>te nemen.")
                    advice.text.list[i] = paste("Tip: probeer <b>eens per", once.per.n.days, "dag(en) een", unit.name, product.name,
                    if (unit.weight != "") paste(" &agrave;", unit.weight, "gram</B>") else "", "</b>te nemen.")
                } else {
                    #advice.text.list[i] = paste("Tip: probeer<b>", round.days(n.per.day), "keer per dag", amount, "gram", product.name, "</b>te nemen.")
                    advice.text.list[i] = paste("Tip: probeer<b>", round.days(n.per.day), "keer per dag een", unit.name, product.name,
                    if (unit.weight != "") paste(" &agrave;", unit.weight, "gram</B>") else "", "</b>te nemen.")
                }
            }
        }
    }

    advice.text.list
}

get.advice = function(T.advice.own, T.advice.everybody, my.ah = my.ah, my.max = my.max, P = NULL, exclude.product = NULL) { #list(type = "nothing" | "scale" | "tip", product.id, factor)
    f.scale.own     = c(-1, -.9, -.75, -.5,  -.25,  .25,  .5, 1, 2)
    f.scale.others  = c(.1, .25,  .5,   .75, 1,    1.25, 1.5)
        
    if (is.null(P)) P = get.totals(T.advice.own)

    # Remove products which were already adviced before
    index.exclude = which(T.advice.own[, "product.id"] %in% exclude.product)
    if (0 < length(index.exclude)) T.advice.own = T.advice.own[-index.exclude, ]
    index.exclude = which(T.advice.everybody[, "product.id"] %in% exclude.product)
    if (0 < length(index.exclude)) T.advice.everybody = T.advice.everybody[-index.exclude, ]

    if (is.vector(T.advice.own)) T.advice.own = t(T.advice.own)
    if (is.vector(T.advice.everybody)) T.advice.everybody = t(T.advice.everybody)    
    
    if (0 < nrow(T.advice.own)) { # are there own products to analyze?
        # Scale own products:
        f.own.distance   = NULL
        for (i.factor in 1:length(f.scale.own)) {
            f               = f.scale.own[i.factor]
            if (nrow(T.advice.own) == 1) {
                P.test = P + f * t(T.advice.own[, voedingsstof.columns])
            } else {
                P.test = t(apply(f * T.advice.own[, voedingsstof.columns], 1, function(product) P + product))
            }
            f.own.distance  = cbind(f.own.distance, apply(P.test, 1, distance))
        }
    
        # Which of our own products is best?
        min.own.distance = min(f.own.distance)
        f.own.min.index = which(f.own.distance == min.own.distance, arr.ind = T)
        if (1 < nrow(f.own.min.index)) f.own.min.index = f.own.min.index[1,]
    } else {
        # There are no products to analyze
        min.own.distance = Inf
    }

    if (0 < nrow(T.advice.everybody)) { # are there products of others to analyze?
        # Use other's products:
        f.others.distance   = NULL
        for (i.factor in 1:length(f.scale.others)) {
            f               = f.scale.others[i.factor]
            if (nrow(T.advice.everybody) == 1) {
                P.test = P + f * t(T.advice.everybody[, voedingsstof.columns])
            } else {
                P.test = t(apply(f * T.advice.everybody[, voedingsstof.columns], 1, function(product) P + product))
            }
            f.others.distance  = cbind(f.others.distance, apply(P.test, 1, distance))
        }
    
        # Which of the other's products is best?
        min.others.distance = min(f.others.distance)
        f.other.min.index = which(f.others.distance == min.others.distance, arr.ind = T)
        if (1 < nrow(f.other.min.index)) f.other.min.index = f.other.min.index[1,]
    } else {
        # There are no products of others to analyze
        min.others.distance = Inf
    }
    
    # How well do we currently do?
    current.distance = distance(P)
    
    # Advice "nothing", or select (the / a) best product and amount
    if (current.distance <= min(min.own.distance, min.others.distance)) {
        # Do nothing
        return(list(type = "nothing", product.id = NA, factor = NA))
    } else if (min.own.distance < min.others.distance) {
        # Scale one of our own products
        return(list(type = "scale", product.id = T.advice.own[f.own.min.index[1], "product.id"], factor = 1 + f.scale.own[f.own.min.index[2]]))
    } else {
        # Tip other's product
        return(list(type = "tip",   product.id = T.advice.everybody[f.other.min.index[1], "product.id"], factor = f.scale.others[f.other.min.index[2]]))        
    }
}

P.apply.advice = function(P, advice) {
    if (advice$type == "scale") P = P + (advice$factor - 1) * T.advice.own[ which(T.advice.own[, "product.id"] == advice$product.id), voedingsstof.columns ]
    if (advice$type == "tip")   P = P + advice$factor       * T.advice.everybody[ which(T.advice.everybody[, "product.id"] == advice$product.id), voedingsstof.columns]
    P
}

P.apply.advice.list = function(P, advice.list) {
    if (0 < length(advice.list)) for (i in 1:length(advice.list)) P = P.apply.advice(P, advice.list[[i]])
    P
}

get.advice.list = function(n.advice = 4, T.advice.own, T.advice.everybody, my.ah, my.max, exclude.product = NULL, min.improvement = 1) {
    P               = get.totals(T.advice.own)
    
    health.before.advice = food.health.percentage(P, my.ah)
    advice.list = list()
#    for (i in 1:n.advice) {
    ready = F
    i = 1
    while (!ready) { # only give advice as long as the improvement >= min.improvement
        # get advice
        advice.candidate = get.advice(T.advice.own, T.advice.everybody, my.ah = my.ah, my.max = my.max, P = P, exclude.product = exclude.product)
        P.test.candidate = P.apply.advice(P, advice.candidate)
        
        health.after.advice = food.health.percentage(P.test.candidate, my.ah)
        
        if (min.improvement <= health.after.advice - health.before.advice) {
            advice.list[[i]] = advice.candidate
            health.before.advice = health.after.advice
            exclude.product  = c(exclude.product, advice.list[[i]]$product.id)
            # assume we follow the advice
            P = P.apply.advice(P, advice.list[[i]])
            i = i + 1
        } else {
            ready = T
        }
    }
#    }    
    advice.list
}

get.totals = function(T.advice.own) {
    if (nrow(T.advice.own) == 0) return(rbind(T.advice.own[, voedingsstof.columns], rep(0, length(voedingsstof.columns))))
    if (nrow(T.advice.own) == 1) return(T.advice.own[,voedingsstof.columns])
    colSums(T.advice.own[,voedingsstof.columns])
}

distance = function(P, my.AH. = my.ah, my.MAX. = my.max) {
    i.zero   = which(P == 0)
    i.little = which(0 < P & P <= my.AH.)
    i.lot    = which(!is.na(my.MAX.) & my.AH. < P)
    
    this.distance = rep(0, length(P))
    if (0 < length(i.zero))     this.distance[i.zero]   = (0 < my.AH.[i.zero]) * 2 # distance = distance * 2 iff 0 < my.ah, else distance remains 0

    if (0 < length(i.little))   {
        this.distance[i.little] = 2 * (my.AH.[i.little] - P[i.little]) / my.AH.[i.little]
    }

    # Vezels dubbel gewicht, mits we er niet te veel van gegeten hebben:
    if (is.element(10, c(i.zero, i.little))) this.distance[10] = this.distance[10] * 2 # synched with function 'get.most.influential.voedingsstof'


    if (0 < length(i.lot))      this.distance[i.lot]    = (P[i.lot] - my.AH.[i.lot])       / (my.MAX.[i.lot] - my.AH.[i.lot])
    
    return(sqrt(sum(this.distance^2)))
}

get.most.influential.voedingsstof = function(P.total.today, product, ADH.Max, assessment, n.vstof) { # returns voedingsstof.index of product that is most influential
    # pre processing
    my.AH.  = ADH.Max$ah
    my.MAX. = ADH.Max$max
    
    P.this.distance = NULL
    for (i in 1:2) {
        if (i == 1) P = P.total.today 
        if (i == 2) P = P.total.today - product
        i.zero   = which(P == 0)
        i.little = which(0 < P & P <= my.AH.)
        i.lot    = which(!is.na(my.MAX.) & my.AH. < P)
        
        this.distance = rep(0, length(P))
        if (0 < length(i.zero))     this.distance[i.zero]   = (0 < my.AH.[i.zero]) * 2 # distance = 2 iff 0 < my.ah, else distance remains 0
        if (0 < length(i.little)) {
            this.distance[i.little] = 2 * (my.AH.[i.little] - P[i.little]) / my.AH.[i.little]
        }

        # Vezels dubbel gewicht, mits we er niet te veel van gegeten hebben:
        if (is.element(10, c(i.zero, i.little))) this.distance[10] = this.distance[10] * 2 # synched with function 'distance'


        if (0 < length(i.lot))      this.distance[i.lot]    = (P[i.lot] - my.AH.[i.lot])       / (my.MAX.[i.lot] - my.AH.[i.lot])
        
        P.this.distance = rbind(P.this.distance, this.distance)
    }
    
    diff.distance = P.this.distance[1,] - P.this.distance[2,] # distance met product - distance zonder product
    # 0 < diff.distance --> product ongezond
    # diff.distance < 0 --> product   gezond
    if (assessment == 'good') {
        n.vstof = min(n.vstof, length(which(diff.distance < 0)))
        index = sort(diff.distance, index.return = T)$ix[1:n.vstof]        
    }
    if (assessment == 'bad') {
        n.vstof = min(n.vstof, length(which(0 < diff.distance)))
        index = sort(diff.distance, index.return = T, decr = T)$ix[1:n.vstof]        
    }
    return( index )
}

get.health.progress = function(gebruikerID, T.food.own.P.cumsum) {
    T.food.own.P.cumsum = apply(T.food.own.P.cumsum[,2:ncol(T.food.own.P.cumsum)], 2, as.numeric)
    i.distance = NULL
    for (i in 1:nrow(T.food.own.P.cumsum)) {
        ADH.Max = get.ADH.Max(gebruikerID, T.food.own.P.cumsum[i, "Energie"], i)
        i.distance[i] = distance(T.food.own.P.cumsum[i,], my.AH. = ADH.Max$ah, my.MAX. = ADH.Max$max)
    }
    max.distance = distance(rep(0, length(ADH.Max$ah)))
    round(100 * (1 - i.distance / max.distance))
}

food.health.percentage = function(P, my.ah) {
    max(round(100 * (1-distance(P) / distance(my.ah*0))), 0) # Value may be negative if you eat so much that you exceed the maximal values for many nutrients
}




















#
