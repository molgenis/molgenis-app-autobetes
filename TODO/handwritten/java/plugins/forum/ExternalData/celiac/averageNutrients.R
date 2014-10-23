setwd('/Users/mdijkstra/Documents/pompgemak/molgenis_distro/handwritten/java/plugins/forum/ExternalData/celiac')
tab = read.csv('VoedingCeliac_2013-05-22.csv', as.is = T)

# delete row with units
tab = tab[-1, ]

# inspect data
#gewicht = as.numeric(tab[, "Gewicht"])
#index = which(500 < gewicht & gewicht < 1000) 
#tab[index,]

# get nutrients only, in matrix
mat = data.matrix(tab[, 10:42])

# remove 'gerechten'
mat = mat[-which(is.na(mat[,1])),]

# remove consumptions with zero calories (like tea)
mat = mat[-which(0 == mat[,1]), ]

# normalize vitamins per calory
mat.normalized = t(apply(mat, 1, function(vit) vit / vit["energie"]))

# derive mean per vitamin
means = colMeans(mat.normalized)

# save as 'product'
dput(means, file = "meanConsumedProduct.RData")