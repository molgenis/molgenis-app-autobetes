#   The path/name of this file is 'java/plugin/eten/voedingsadvies_nutrition_guidelines.R'.
#   This file is part of the DIADVIES project.
#   
#   Version:     0.1
#   Copyright:   2011 (c) Dr. M. Dijkstra. All rights reserved.
#   Email:       MartijnDijkstra1980@gmail.com

get.ADH.Max = function(gebruikerID, E.my.total, n.days.no.hunger, path.to.ADH.file = '.') {
    #gebruiker.info  = find.gebruiker(id = gebruikerID)
    age             = as.numeric(difftime(Sys.time(), as.POSIXct(gebruiker.info$geboortedatum), units="days")) / 365.242199
    sex             = gebruiker.info$geslacht
    
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
