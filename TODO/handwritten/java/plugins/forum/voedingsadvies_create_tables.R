#   The path/name of this file is 'java/plugin/eten/voedingsadvies_create_tables.R'.
#   This file is part of the DIADVIES project.
#   
#   Version:     0.1
#   Copyright:   2011 (c) Dr. M. Dijkstra. All rights reserved.
#   Email:       MartijnDijkstra1980@gmail.com

T.product.voedingsstoffen.columns.initial = c(1, 5:15, 17:39)

get.T.product = function(eten.all.events) { #[ product.id, all relevant properties of the product]
    product.id  = eten.all.events$product
    T.product   = find.product(id = product.id)
    T.product   = T.product[, T.product.voedingsstoffen.columns.initial]
    T.product
}

get.T.product.beta = function(eten.all.events) { #[ product.id, all relevant properties of the product]
    # leaving Gerechten intact

    # first add the Producten
    product.id  = eten.all.events$product
    T.product   = find.product(id = product.id)
    T.product   = T.product[, T.product.voedingsstoffen.columns.initial]
    
    # next add the Gerechten
    gerecht.id  = setdiff(unique(product.id), T.product$id)
    
    if (0 < length(gerecht.id)) for (g.id in gerecht.id) {
        T.product = rbind(T.product, gerecht.as.one.product(g.id))
    }
    
    T.product
}

    # The following two functions are helpers of get.T.product.beta
    empty.product = function(id) {
        vec = find.product(id=-1)
        vec[1,1] = id
        vec[1, 2:ncol(vec)] = 0
        vec
    }
    
    gerecht.as.one.product = function(gerecht_id) {
        # FIXME:
        # known bug: a gerecht may contain other gerechten
        # Even worse: a gerecht may refer to another gerecht that refers to this gerecht
        components = find.component(gerecht_id = gerecht_id)
        
        aproduct = empty.product(gerecht_id)[, T.product.voedingsstoffen.columns.initial]
        aproduct["eenheid"] = "Gerecht"
        aproduct["productnaam"] = find.gerecht(gerecht_id)$naam
    
        if (0 < nrow(components)) {
            T.gerecht.component = find.product(id = components$product)
            gerecht.gewicht = 0
            for (i in 1:nrow(components)) {
                gewicht     = components[i, "gewicht"]
                n.eenheden  = components[i, "eenheden"]
                this.component.id   = components[i, "product"]
                this.component.index  = which(T.gerecht.component[, "id"] == this.component.id)
        
                if (is.na(gewicht)) {
                    # use n.eenheden to derive and overwrite the gewicht
                    eenheidgewicht      = T.gerecht.component[this.component.index, "eenheidgewicht"]
                    gewicht             = eenheidgewicht * n.eenheden
                }
                
                this.product = T.gerecht.component[this.component.index, c(11:14,16:39)]
                if (any(is.na(this.product))) this.product[which(is.na(this.product))] = 0
    
                aproduct[8:35]  = aproduct[8:35] + gewicht / 100 * this.product
                gerecht.gewicht = gerecht.gewicht + gewicht
            }
            # alle 8:35 delen door gewicht * 100
            # (als er dan 1 eenheid gegeten wordt dan komt het vanzelf goed)
            aproduct[8:35] = aproduct[8:35] / gerecht.gewicht * 100
            aproduct["eenheidgewicht"] = gerecht.gewicht
        }
    
        aproduct
    }

get.T.food = function(eten.all.events, T.product) { #[ gebruikerID, day, product.id, weight ] (Gerechten are already processed)
    T.food = cbind(gebruikerID = eten.all.events$gebruiker, product.id = eten.all.events$product, weight = eten.all.events$gewicht, moment = eten.all.events$moment)
    
    index.na = which(is.na(T.food[, "weight"]))
    for (i in index.na) {
        index.product   = which(T.product$id == T.food[i, "product.id"])
        unit.weight     = T.product[index.product, "eenheidgewicht"]
        if (unit.weight == 0) unit.weight = 100
        T.food[i, "weight"] = unit.weight * eten.all.events[i, "eenheden"]
    }
    
    T.food
}

filter.T.food = function(T.food, T.product, min.cal) { # Return T.food, but remove days on which < min.cal was eaten
    T.food.original = T.food
    
    # keep day, cut a way time:
    T.food[, "moment"]  = as.numeric(format(as.POSIXct(T.food[, "moment"]),"%Y%m%d"))
    T.food              = apply(T.food, 2, as.numeric)
    if (!is.matrix(T.food)) T.food = t(T.food)
    energy = apply(T.food, 1, function(food.event) T.product[which(T.product[, "id"] == food.event["product.id"]), "energie"] * food.event["weight"] / 100)
    if (any(is.na(energy))) energy[which(is.na(energy))] = 0
    
    unique.gebruikers   = unique(T.food[, "gebruikerID"])
    unique.day         = unique(T.food[, "moment"])
    
    index.remove.event  = NULL
    for (g in unique.gebruikers) for (d in unique.day) {
        index = which(T.food[, "gebruikerID"] == g & T.food[, "moment"] == d)
        if (sum(energy[index]) < min.cal) index.remove.event = c(index.remove.event, index)
    }
    
    if (0 < length(index.remove.event)) T.food.original = T.food.original[-index.remove.event, ]
    
    T.food.original
}

get.T.overview.everybody = function(gebruikerID, T.food, T.product) { #[ product.id, n.per.day, E.pct of total Energy]
    if (nrow(T.food)==0) {
        mat = rbind(NULL, c(product.id = 1, n.per.day = 1, E.pct = 1))
        mat = mat[-1,]
        return(mat)
    }

    # keep day, cut a way time:
    T.food[, "moment"]  = as.numeric(format(as.POSIXct(T.food[, "moment"]),"%Y%m%d"))
    T.food              = apply(T.food, 2, as.numeric)
    if (!is.matrix(T.food)) T.food = t(T.food)
    energy = apply(T.food, 1, function(food.event) T.product[which(T.product[, "id"] == food.event["product.id"]), "energie"] * food.event["weight"] / 100)
    if (any(is.na(energy))) {
        energy[which(is.na(energy))] = 0
        print("some products have NA energy, in get.T.overview.everybody")
    }
    
    # Temporary add 'energy' to T.food:
    T.food = cbind(T.food, energy = energy)
    
    unique.products     = T.product[, "id"]
    unique.gebruikers   = unique(T.food[, "gebruikerID"])
    
    # Total energy and Total number of days filled in per gebruiker
    gebruiker.total.energy = gebruiker.n.days = NULL
    for (i in unique.gebruikers) {
        index = which(T.food[, "gebruikerID"] == i)
        gebruiker.total.energy = c(gebruiker.total.energy, sum( T.food[index, "energy"] ))
        gebruiker.n.days = c(gebruiker.n.days, length(unique(T.food[index, "moment"])))
    }
    
    # make the table by iterating over the products:
    T.overview.everybody = NULL
    for (i in unique.products) {
        index = which(T.food[, "product.id"] == i)
        
        # select subset with product i
        T.food.i = T.food[index,]
        
        # repair if only 1 row:
        if (class(T.food.i) == "numeric") T.food.i = t(as.matrix(T.food.i))

        # which gebruikers ate product i?
        i.gebruiker = unique(T.food.i[, "gebruikerID"])
        
        i.overview = NULL
        for (g in i.gebruiker) {
            index.g = which(T.food.i[, "gebruikerID"] == g)

            n.per.day = length(index.g) / gebruiker.n.days[which(unique.gebruikers == g)]
            E.pct = sum(T.food.i[index.g, "energy"]) / gebruiker.total.energy[which(unique.gebruikers == g)]
            
            vec = c(n.per.day = n.per.day, E.pct = E.pct)
            i.overview = rbind(i.overview, vec)
        }
        
        vec = c(product.id = i, n.per.day = quantile(i.overview[, "n.per.day"], .75), E.pct = quantile(i.overview[, "E.pct"], .75))
        T.overview.everybody = rbind(T.overview.everybody, vec)
    }
    
    T.overview.everybody
}

get.T.advice.own = function(gebruikerID, beginday, endday, T.food, T.product) { #[ product.id, weight, Voedingsstoffen ] // SIDE EFFECT: MAKES n.days.no.hunger global
    # select only my products eaten between beginday and endday
    T.food = T.food[which(T.food[, "gebruikerID"] == gebruikerID), ]
    if (class(T.food) == "character") T.food = t(T.food)
    moment.vec = as.POSIXct(T.food[, "moment"])
    T.food = T.food[which(beginday <= moment.vec & moment.vec < endday),]

    # TO DO: if T.food empty, then ...    
    if (is.vector(T.food)) T.food = t(T.food)
    
    n.days.no.hunger <<- length(unique(as.numeric(format(as.POSIXct(T.food[, "moment"]),"%Y%m%d"))))
    
    if (n.days.no.hunger == 0) return(NULL)
    
    unique.products = as.numeric(unique(T.food[, "product.id"]))
    
    # Fill the table by iterating over the unique products
    T.advice.own = NULL
    for (i in unique.products) {
        food.event.i    = which(T.food[, "product.id"] == i)
        i.weight        = sum(as.numeric(T.food[food.event.i, "weight"]))
        
        # Search in T.product for the voedingsstoffen
        T.product.index = which(T.product[, "id"] == i)
        vitvec          = as.matrix(T.product[T.product.index, T.product.voedingsstoffen.columns] * i.weight / 100)
         
        T.advice.own    = rbind(T.advice.own, c(product.id = i, weight = i.weight, vitvec))
     }
    
    index.NA = which(is.na(T.advice.own))
    if (0 < length(index.NA)) T.advice.own[index.NA] = 0
    
    colnames(T.advice.own)[3:30] = colnames(vitvec)
    T.advice.own
}

get.T.advice.everybody = function(T.overview.everybody, T.advice.own, T.product) {#[ product.id, weight, Voedingsstoffen ]
    my.product.id       = T.advice.own[, "product.id"]

    T.advice.everybody  = NULL
    if (!is.null(T.overview.everybody)) if (0 < nrow(T.overview.everybody)) for (i in 1:nrow(T.overview.everybody)) {
        this.id = T.overview.everybody[i, "product.id"]
        if (!is.element(this.id, my.product.id)) {
            T.product.index = which(T.product[, "id"] == this.id)
            E.gram          = T.product[T.product.index, "energie"] / 100 # energy per gram
            i.weight        = T.overview.everybody[i, "E.pct.75%"] * E.my.total / E.gram # the amount scaled to our period
            vitvec          = as.matrix(T.product[T.product.index, T.product.voedingsstoffen.columns] * i.weight / 100)

            T.advice.everybody = rbind(T.advice.everybody, c(product.id = this.id, weight = i.weight, vitvec))
        }
    }

    if (is.null(T.advice.everybody)) return(T.advice.everybody)

    index.NA = which(is.na(T.advice.everybody))
    if (0 < length(index.NA)) T.advice.everybody[index.NA] = 0
    
    colnames(T.advice.everybody)[3:30] = colnames(vitvec)
    T.advice.everybody
}

get.T.food.own = function(gebruikerID, beginday, endday, T.food, T.product) {#[ day, product.name, voedingsstoffen]
    if (is.null(T.food)) return(NULL)
    # select my products in my period
    T.food = T.food[which(T.food[, "gebruikerID"] == gebruikerID), ]
    if (class(T.food) == "character") T.food = t(T.food)
    
    moment.vec = as.POSIXct(T.food[, "moment"])
    T.food = T.food[which(beginday <= moment.vec & moment.vec < endday),]

    if (is.vector(T.food)) T.food = t(T.food)
    if (nrow(T.food) == 0) return(NULL)

    # for each event: get scaled voedingsstoffen
    T.product.voedingsstoffen = apply(T.product[, T.product.voedingsstoffen.columns], 2, as.numeric)
    if (is.vector(T.product.voedingsstoffen)) T.product.voedingsstoffen = t(T.product.voedingsstoffen)
    
    get.scaled.totals = function(this.product) {
        index = which(T.product[, "id"] == this.product["product.id"])
        return(T.product.voedingsstoffen[index, ] * this.product["weight"] / 100)
    }
    T.food.subset = T.food[,c("product.id", "weight")]
    if (is.vector(T.food.subset)) T.food.subset = t(T.food.subset)
    T.food.subset = apply(T.food.subset, 2, as.numeric)
    if (is.vector(T.food.subset)) T.food.subset = t(T.food.subset)
    vitmat = t(apply(T.food.subset, 1, get.scaled.totals))
    product.name  = as.vector(sapply(T.food[, "product.id"], function(id) T.product[which(T.product[, "id"] == id), "productnaam"]))
    
    # round moment to date
    moment.vec = as.POSIXct(T.food[, "moment"], format="%Y-%m-%d")
    
    # indices of sorted dates
    ix = sort(as.numeric(moment.vec), index.return = TRUE)$ix
   
    if (length(product.name[ix]) == 1) {
        T.food.own = c(as.character(moment.vec), product.name, vitmat)
    } else {
        # matrix sorted by date:
        T.food.own = cbind(product.name[ix], vitmat[ix,])
        T.food.own = cbind(as.character(moment.vec[ix]), T.food.own)
    }
        
    index.NA = which(is.na(T.food.own))
    if (0 < length(index.NA)) T.food.own[index.NA] = 0
    
    if (is.vector(T.food.own)) {
        names(T.food.own) = c("dag", "product.name", names(my.ah))    
    } else {
        colnames(T.food.own) = c("dag", "product.name", names(my.ah))    
    }
    T.food.own
}

get.T.food.own.P.cumsum = function(T.food.own){#[day, cumsum(P) until today]
    unique.day = sort(unique(T.food.own[, "dag"]))
    
    # For each day, combine products
    T.food.own.subset   = apply(T.food.own[, 3:ncol(T.food.own)], 2, as.numeric)
    T.food.own.P.cumsum = NULL
    for (i in 1:length(unique.day)) {
        index               = which(T.food.own[, "dag"] == unique.day[i])
        vitvec              = if (length(index) == 1) T.food.own.subset[index,] else colSums(T.food.own.subset[index,])
        T.food.own.P.cumsum = rbind(T.food.own.P.cumsum, vitvec)
    }
    T.food.own.P.cumsum = apply(T.food.own.P.cumsum, 2, cumsum)
    cbind(unique.day, T.food.own.P.cumsum)
}

compress.products = function(T.day.report.subset) {
    product.name    = T.day.report.subset[, "product.name"]
    unique.product.name  = unique(product.name)
    if (length(unique.product.name) == length(product.name)) {
        return(T.day.report.subset)
    } else {
        T.new = NULL
        for (p in unique.product.name) {
            index.current = which(product.name == p)
            if (1 == length(index.current)) {
                T.new = rbind(T.new, T.day.report.subset[index.current,])
            } else {
                vec   = colSums(apply(T.day.report.subset[index.current, 3:ncol(T.day.report.subset)], 2, as.numeric))
                T.new = rbind(T.new, c(T.day.report.subset[index.current[1],1:2], vec))
            }
        }
        return(T.new)
    }    
}

date.nice.layout = function(d) {
    d = as.POSIXct(format(as.POSIXct(d), "%Y-%m-%d"))
    weekday = c("Zondag", "Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag")
#    if (as.character(format(Sys.time(), "%Y-%m-%d"))==d) {
#        day = "Vandaag"
#    } else if (as.character(format(Sys.time() - 24 * 3600, "%Y-%m-%d"))==d) {
#        day = "Gisteren"
#    } else {
        day = weekday[as.numeric(format(d, "%w")) + 1]
#    }
    day.int = format(d, "%d")
    month   = format(d, "%b")
    return(paste(day, ", ", day.int, " ", month, '.', sep=''))
}

get.delta.product = function(P.total.today, product, ADH.Max) {
    distance(P.total.today, ADH.Max$ah, ADH.Max$max) - distance(P.total.today - product, ADH.Max$ah, ADH.Max$max)
}

get.T.day.report = function(T.food.own, beginday, endday, show.n.days = Inf) {#[day, product, good/bad voedingsstof]
    if (is.null(T.food.own)) return(t(c("<font color = '#5B82A4'>--</font>", "&nbsp;", "<font color = '#5B82A4'>Er zijn onvoldoende gegevens in deze periode!</font>", "Voer meer gegevens in, of selecteer een grotere periode.", "&nbsp;", "&nbsp;")))
    show.n.days     = min(round(as.numeric(difftime(endday, beginday, units="days"))), show.n.days) # report on the last show.n.days in this period
    if (is.vector(T.food.own)) {
        day.selection   = T.food.own["dag"]
        n.days.period   = 1
        P.total         = sapply(T.food.own[voedingsstof.columns], as.numeric)
        T.food.own      = t(T.food.own)
    } else {
        day.selection   = sort(unique(T.food.own[, "dag"]), decr = T)[1:show.n.days]
        day.selection   = day.selection[!is.na(day.selection)]
        n.days.period   = length(unique(T.food.own[,"dag"]))
        P.total         = colSums(apply(T.food.own[, voedingsstof.columns], 2, as.numeric))
    }
    
    ADH.Max         = get.ADH.Max(gebruikerID, P.total["Energie"], n.days.period)
    
    # get bewegen data
    bewegen         = find.activiteit(gebruiker_id = gebruikerID)
    bewegen.moment  = as.POSIXlt(format(as.POSIXlt(bewegen$vanaf), "%Y-%m-%d"))
    
    # standardize day.selection
    day.selection = as.POSIXlt(format(as.POSIXlt(day.selection), "%Y-%m-%d"))
    
    T.day.report = NULL
    for (i in 1:length(day.selection)) {
        energy.day = 0 # total energy consumed today
        T.day.report.day = NULL
        
        T.food.own.subset = T.food.own[which(T.food.own[, "dag"] == day.selection[i]),]
        if (is.vector(T.food.own.subset)) {
            T.food.own.subset = t(T.food.own.subset)
        } else {
            T.food.own.subset = compress.products(T.food.own.subset)
        }
        
        P.total.today = P.total
        
        if (is.vector(T.food.own.subset)) T.food.own.subset = t(T.food.own.subset)
        for (j in 1:nrow(T.food.own.subset)) { # for each product j
            if (is.vector(T.food.own.subset)) T.food.own.subset = t(T.food.own.subset)
            if (nrow(T.food.own.subset) == 1) {
                T.food.own.voedingsstoffen.subset = sapply(T.food.own.subset[voedingsstof.columns], as.numeric)
                delta.product = get.delta.product(P.total.today, T.food.own.voedingsstoffen.subset, ADH.Max)
            } else {
                T.food.own.voedingsstoffen.subset = apply(T.food.own.subset[, voedingsstof.columns], 2, as.numeric)
                delta.product = apply(T.food.own.voedingsstoffen.subset, 1, function(product) get.delta.product(P.total.today, product, ADH.Max))
            }

            j.product       = which.max(delta.product)
            product.name    = if (is.vector(T.food.own.subset)) T.food.own.subset["product.name"] else T.food.own.subset[j.product, "product.name"]
            product         = if (is.vector(T.food.own.voedingsstoffen.subset)) T.food.own.voedingsstoffen.subset else T.food.own.voedingsstoffen.subset[j.product, ]
            if (0 < delta.product[j.product]) { # unhealthy product
                evaluation  = "<img src='res/img/cross.png' height='13'>"
                product.name.colored = color.text(product.name, 'bad')
                vstof = paste(names(ADH.Max$ah)[ get.most.influential.voedingsstof(P.total.today, product, ADH.Max, 'bad', n.vstof = 3) ],  collapse=', ')
                P.total.today        = P.total.today - product # remove unhealthy product
            } else { # healthy product
                evaluation = "<img src='res/img/check_mark.png' height='13'>"
                product.name.colored = color.text(product.name, 'good')
                vstof = paste(names(ADH.Max$ah)[ get.most.influential.voedingsstof(P.total.today, product, ADH.Max, 'good', n.vstof = 3) ], collapse=', ')
            }
            
            # energy
            energy = round(product[1])
            energy.day = energy.day + product[1] # not rounded yet
            
            if (!is.vector(T.food.own.subset)) T.food.own.subset = T.food.own.subset[-j.product,]
            
            day = if (1 == j) date.nice.layout(day.selection[i]) else "x"
            
            # add a product-line in today's report
            this.row = c(day = day, evaluation = evaluation, product = product.name.colored, voedingsstof = vstof, energy = energy)
            T.day.report.day = rbind(T.day.report.day, this.row)
        } # end of for loop over products within one day
        
        if (1 < nrow(T.day.report.day)) {
            T.day.report.day = T.day.report.day[rev(1:nrow(T.day.report.day)),] # best products first, worst products last :-)
            T.day.report.day[,1] = rev(T.day.report.day[,1]) # but why don't I say j.product = which.MIN... above, then?!
        }
        
        # add a flag that indicates for each product whether it is the last product for that day; this flag is used to align the +-sign in the rapport
        T.day.report.day = cbind(T.day.report.day, lastproduct = F)
        T.day.report.day[nrow(T.day.report.day), "lastproduct"] = T
        
        T.day.report = rbind(T.day.report, T.day.report.day)
        
        # add stuff about the total consumed energy
        energy.day = round(energy.day)

        # now fill "basal.metabolism.energy.burned" with a lot of text (this is not a nice way of working...)
        # It should contain this:
        # "Uw basaalmetabolisme kost ... kcal per dag. U hebt vandaag ... kcal verbrand door te bewegen.
            # Step 1: get current weight of the gebruiker
            
            # Step 2: get basaalmetabolisme
            # This should depend on Step 1. But for now we take the current basaalmetabolisme, i.e. ignoring weights in the past
            basal.metabolism.energy.burned = round(gebruiker.BMR * 24)
            
            # Step 3: get today's bewegen
            index = which(bewegen.moment == day.selection[i])
            bewegen.energy.burned = 0
            if (0 < length(index)) bewegen.energy.burned = sum(bewegen$energie[index])
            

        lost = bewegen.energy.burned + basal.metabolism.energy.burned - energy.day

        weight.text = paste("Uw basaalmetabolisme kost<B>", basal.metabolism.energy.burned, "kcal</B> per dag. U hebt vandaag<B>", bewegen.energy.burned, "kcal</B> verbrand door bewegen.")

        afvallen = if (0 <= lost) "afgevallen" else "aangekomen"
        lost = abs(lost)
        
       
        # NB We use the labels differently now!
        cal.vec = c(day = "sumcal", evaluation = afvallen, product = lost, voedingsstof = weight.text, energy = energy.day, lastproduct = F)
       
        T.day.report = rbind(T.day.report, cal.vec)
        
    } # end of for loop over days
    T.day.report
}

get.T.exclude.product = function(exclude.product, T.product) {#[product.id, product.name]
    exclude.product.length = length(exclude.product)
    if (exclude.product.length == 0) {
        T.exclude.product = t(c(product.id = "-1", product.name =  "-1"))
    } else {
        T.exclude.product = NULL
        for (i in 1:exclude.product.length) {
            this.id = exclude.product[i]
            index   = which(T.product[,"id"] == this.id)
            if (0 < length(index)) {
                vec     = c(product.id = this.id, product.name = T.product[index, "productnaam"])
                T.exclude.product = rbind(T.exclude.product, vec)
            }
        }
    }
    T.exclude.product
}

n.times.eaten.in.period = function(gebruikerID, beginday, endday, product.id, T.food) {
    moment.vec = as.POSIXct(T.food[, "moment"])
    
    return(length(which(beginday <= moment.vec
                        & moment.vec < endday
                        & T.food[, "product.id"] == product.id
                        & T.food[, "gebruikerID"] == gebruikerID))
    )
}

create.output.table.vec = function(vec) {
    this.table = NULL
    if (0 < length(vec)) for (i in 1:length(vec)) this.table = rbind(this.table, vec[i])
    this.table
}





























#
