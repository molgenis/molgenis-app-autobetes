#   The path/name of this file is 'java/plugin/eten/voedingsadvies.R'.
#   This file is part of the DIADVIES project.
#   
#   Version:     0.1
#   Copyright:   2011 (c) Dr. M. Dijkstra. All rights reserved.
#   Email:       MartijnDijkstra1980@gmail.com

# Load the Molgenis R-api. This seems system dependent :-s
if (.Platform$pkgType == "mac.binary.leopard") {
	source('http://localhost:8080/celiac/api/R')
} else { # 'unix'
	source('http://localhost:8080/api/R')
}

screen.bg.color = "#F8FAFB"
advice.color.positive = "'#387C44'" #38AC44
advice.color.negative = "'#800517'" #A00517

if (!exists('offline')) offline = T
if (offline) {
    gebruikerID = 4
    setwd('C:/molgenis/DiadviesWorkspace2010/diadvies/handwritten/java/plugin/eten')
    FigVoedingsstoffenFile = 'voedingsstoffen.png'
    AdviceFile  = 'advice.txt'
    AdviceFile1  = 'advice1.txt'
    
    gebruikerID =7
    
    beginday = '2011-03-11'
    endday = '2011-04-11'
    
#    exclude.product = c(520, 1966, 2248, 1034, 147, 579)
    exclude.product = c(423, 440, 891, 484, 1865, 1079, 147, 144)
    
    offline = T
    this.file = 'C:/molgenis/DiadviesWorkspace2010/.metadata/.plugins/org.eclipse.wst.server.core/tmp4/wtpwebapps/diadvies/WEB-INF/classes/plugin/eten/voedingsadvies.R'
    gebruikerID =7
    beginday = '2011-03-15'
    endday = '2011-04-15'
    exclude.product = c(423, 440, 891, 484, 1865, 1079, 147, 144)
    beginday = '2011-03-15'
    endday = '2011-04-15'
    exclude.product = NULL    
    
    gebruikerID =4
    beginday = '2011-03-16'
    endday = '2011-04-16'
    exclude.product = c(113, 4)    
    exclude.product = NULL
} else {
    setwd(substr(this.file, 1, nchar(this.file) - nchar(tail(strsplit(this.file,'/')[[1]],1)) - 1))
}
source('../advies/model.R')
source('../advies/model_InsulinFoodBMR.R') # for BMR
source('voedingsadvies_create_tables.R')
source('voedingsadvies_nutrition_guidelines.R')
source('voedingsadvies_derive_advice.R')
source('voedingsadvies_create_plots.R')

# Preprocessing:
if (as.POSIXct(endday) < as.POSIXct(beginday)) {
    beginday.tmp = beginday
    beginday     = endday
    endday       = beginday.tmp
}
beginday = as.POSIXct(beginday)
endday   = as.POSIXct(endday) + 24 * 3600
# This line is moved to voedingsadvies_create_table.R: T.product.voedingsstoffen.columns.initial = c(1, 5:15, 17:39)#c(1, 5:14, 16:39)
T.product.voedingsstoffen.columns = 8:35
gebruiker.info  = find.gebruiker(id = gebruikerID)
gebruiker.BMR   = get.BMR(gebruikerID) * find.overzicht(gebruiker_id = gebruikerID)$activitymultiplier

# Create tables based on this data
eten.all.events         = find.eten()#as.product(find.eten())
eten.all.events.as.products = as.product(eten.all.events)
    
if (0 == nrow(eten.all.events.as.products)) {    
    T.product               = NULL
    T.food                  = NULL
    T.overview.everybody    = NULL
    T.advice.own            = NULL
    E.my.total              = 0
    n.days.no.hunger        = 0
} else {
    # T.products:           [ product.id, all relevant properties of the product]
    T.product               = get.T.product(eten.all.events.as.products)
    
    # T.food contains for each product the weight (Gerechten are already processed)
    # T.food:               [ gebruikerID, day, product.id, weight ]
    T.food                  = get.T.food(eten.all.events.as.products, T.product)
    
    # T.overview.everybody contains the 75% quantile of 1. frequency and 2. energy percentage of all the products eaten by everybody
    # T.overview.everybody  [ product.id, n.per.day, E.pct ]
    T.overview.everybody    = get.T.overview.everybody(gebruikerID, T.food, T.product)

    # T.food:               Filter days with few calories:
#    T.food                  = filter.T.food(T.food, T.product, 500)
    
    # T.advice.own          [ product.id, weight, Voedingsstoffen ]
    T.advice.own            = get.T.advice.own(gebruikerID, beginday, endday, T.food, T.product)
    # // SIDE EFFECT: MAKES n.days.no.hunger global
    
    # E.my.total            Total daily energy
    E.my.total              = sum(T.advice.own[, "energie"])
}

standard.daily.energy = 24 * gebruiker.BMR

# check whether we ate enough to make an advice sensible
advice.sensible = standard.daily.energy * 6 < E.my.total
    
    #### What do we do if gebruiker has no days > 500 calories?
    if (n.days.no.hunger == 0) { # manually set E.my.total to 'normal' and days to ndays in period
        period.empty     = T
        n.days.no.hunger = abs(as.numeric(difftime(endday, beginday, units="days")))
        E.my.total       = n.days.no.hunger * standard.daily.energy
    } else period.empty  = F


    # Contains all products eaten on 'hunger days' or by other users
    # T.advice.everybody    [ product.id, weight, Voedingsstoffen ]
    T.advice.everybody      = get.T.advice.everybody(T.overview.everybody, T.advice.own, T.product)
    
	   # Determine personal voedingstoffen-guidelines: Aanbevolen ... Maximum
	   ADH.Max = get.ADH.Max(gebruikerID, E.my.total, n.days.no.hunger);
	   my.ah   = ADH.Max$ah
	   my.max  = ADH.Max$max
            print("Haal grenzen Energie uit Internationale aanbevelingen, net als voedingscentrum <- dat doe ik nu min-of-meer")
            print("Later: Verhoog Energie-behoefte met activiteiten! En, zet ADH op het DOEL!")


    # T.food.own            [ day, product.name, voedingsstoffen]
    T.food.own              = get.T.food.own(gebruikerID, beginday, endday, T.food, T.product)
#    T.food.own.P.cumsum     = get.T.food.own.P.cumsum(T.food.own)


# Equalize column names:
    voedingsstof.columns    = 3:30
    empty.matrix = matrix(NA, nc = 2 + length(voedingsstof.columns), dimnames = list(NULL, c('product.id', 'weight', names(my.ah))))[-1,]
    if (!is.null(T.advice.own)) {
        colnames(T.advice.own)[voedingsstof.columns] = names(my.ah)
    } else T.advice.own = empty.matrix
    if (!is.null(T.advice.everybody)) {
        colnames(T.advice.everybody)[voedingsstof.columns] = names(my.ah)
    } else T.advice.everybody = empty.matrix

# Calculate Advice List     
    advice.list             = get.advice.list(n.advice = 6, T.advice.own, T.advice.everybody, my.ah, my.max, exclude.product)
    
# Convert advice to text
    advice.text = create.output.table.vec(advice.to.text(advice.list))
    advice.text = cbind(create.output.table.vec(advice.to.id(advice.list)), advice.text)
    # remove "do nothing's"
    if (is.null(advice.text)) {
        advice.text = t(c("-1", "Er is geen advies!"))
    } else {
        index.do.nothing = which(is.na(advice.text[,1]))
        if (0 < length(index.do.nothing)) {
            advice.text = advice.text[-index.do.nothing,]
            if (is.vector(advice.text)) advice.text = t(advice.text)
            if (nrow(advice.text) == 0) advice.text = t(c("-1", "Er is geen advies!"))  
        }
    }
    write.table(advice.text, if (offline) stdout() else AdviceFile, sep=";",row.names = F, col.names = c("id", "Advies"))

# Create voedingsstoffen barplot
    P.current   = get.totals(T.advice.own)
    P.advice    = P.apply.advice.list(P.current, advice.list)
    plot.food.components(P.current, P.advice, FigVoedingsstoffenFile)

# Calculate the improvement:
    health.current  = food.health.percentage(P.current, my.ah) #max(round(100 * (1-distance(P.current) / distance(my.ah*0))), 0) # Value may be negative if you eat so much that you exceed the maximal values for many nutrients
    health.advice   = food.health.percentage(P.advice, my.ah)  #round(100 * (1-distance(P.advice) / distance(my.ah*0)))

# Create text.strings:
    period.n.days   = as.numeric(difftime(endday, beginday, units="days"))
    n.days.food     = if (period.empty) 0 else n.days.no.hunger
    # alle dagen geschikt
        if (n.days.food == period.n.days)
            text.before.advice = paste("Het advies en de figuur hieronder hebben betrekking op alle", period.n.days, "dagen in de gehele periode.")
    # sommige dagen geschikt
        if (0 < n.days.food & n.days.food < period.n.days) 
            text.before.advice = paste("Dit advies heeft betrekking op de", n.days.food, "dag(en) in deze periode waarop u 500 Cal of meer at.")
    # geen dagen geschikt
        if (0 == n.days.food) 
            text.before.advice = paste("Deze periode beslaat", period.n.days, "dag(en). Echter, op geen enkele dag at u 500 Cal of meer. Om een advies te krijgen zou u eerst meer gegevens in moeten voeren. Dit advies is slechts een suggestie om aan te sterken.")
        # if we didn't eat enough to make an advice sensible, then mention this!
        if (!advice.sensible) {
            text.alarm = "not sensible"
        } else {
            text.alarm = ""
        }
        
    # text after advice:
    text.after.advice = paste("In deze periode is uw voeding <b>", health.current, "%</b> van optimaal. Het opvolgen van dit advies kan uw voeding verbeteren tot <b>", color.text(paste(health.advice, "% van optimaal", sep=''), 'good'),"</b>.", sep='')
    # write to file:
    write.table(create.output.table.vec(c(text.alarm, text.before.advice, text.after.advice)),if (offline) stdout() else AdviceFile1, sep=";",row.names = F, col.names = "Advies")
    
# Create progress graph
#    health.progress = get.health.progress(gebruikerID, T.food.own.P.cumsum)
#    plot.health.progress(T.food.own.P.cumsum[, "unique.day"], health.progress)
    
# Create day report

    # The following is an inefficient work around. The point is that the advice (above) should be based on individual products,
    # so the Gerechten need to be decomposed into individual products. And, the Day report below is based on the intact Gerechten and products
    T.product   = get.T.product.beta(eten.all.events)
    T.food      = get.T.food(eten.all.events, T.product)
    T.food.own  = get.T.food.own(gebruikerID, beginday, endday, T.food, T.product) 
    T.day.report = get.T.day.report(T.food.own, beginday, endday, Inf)
    write.table(T.day.report, if (offline) stdout() else DayReportFile, sep=";",row.names = F, col.names = c("Dag", "Evaluatie", "Product","Voedingsstof", "Energie (kcal)", "Last product today"))

# Save excluded products
    T.exclude.product = get.T.exclude.product(exclude.product, T.product)
    write.table(T.exclude.product, if (offline) stdout() else excludeProductFile, sep=";",row.names = F, col.names = c("id", "name"))


























#
