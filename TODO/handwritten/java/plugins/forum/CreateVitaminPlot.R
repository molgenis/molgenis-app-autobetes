if (F) {
	setwd('/Users/mdijkstra/Documents/pompgemak/molgenis_distro/handwritten/java/plugins/forum/')
	# matrix 'nutr' is originating from 'java'
	pngVitaminFile = pngHealthFile = NA
	nutr = NULL
	nutr = rbind(nutr, c(pid = 292, weight = 150.0))
	nutr = rbind(nutr, c(pid = 20, weight = 150.0))
	nutr = rbind(nutr, c(pid = 929, weight = 160.0))
	nutr = rbind(nutr, c(pid = 1, weight = 123.0))
	
	age             = 40
	sex             = "man"
}

source('MolgenisConnect.R')
source('CreateVitaminPlot_functions.R')

screen.bg.color = "#FFFFFF"#"#F8FAFB"
advice.color.positive = "'#387C44'" #38AC44
advice.color.negative = "'#800517'" #A00517
advice.transparency = "88"
color.rect.background = "#FFFFFF"#"#EEEEFF"
color.rect.okay = "#EFFFEF"#"#AAFFFF"#"#00AEEF"
color.heading = "#cee3ea"#"#d8ffd8"#"#D1FFFF"
col.axis.text = "#888888"
color.horizontal.lines = color.heading #"#5B82A4"
color.no.food = "#FF7F00"

# Personal characteristics -- Move this to 'java'
gebruiker.info = list()
gebruiker.info$gewicht = 75
gebruiker.info$lengte = 1.85
gebruiker.BMR = 2500/24

# Calculate adviced/max values, based on 'personal characteristics'
ADH.Max = get.ADH.Max();
my.ah   = ADH.Max$ah
my.max  = ADH.Max$max

# Init vector amounts
amounts = getAmounts(nutr[1, "pid"], nutr[1, "weight"])

# Determine current 'food content'
if (1 < nrow(nutr)) for (i in 2:nrow(nutr)) {
	amounts = amounts + getAmounts(nutr[i, "pid"], nutr[i, "weight"])
}

# Create Vitamin plot
plot.food.components(amounts, imagefile = pngVitaminFile)

# Increase amounts with 'average food composition' up to adviced calory intake
energy.shortage = my.ah["Energie"] - amounts["Energie"]
if (0 < energy.shortage) {
	# get mean consumption per kcal
	meanProduct = productToStandard(dget("ExternalData/celiac/meanConsumedProduct.RData"))
	
	# increase amounts with this
	amounts = amounts + energy.shortage * meanProduct
}

# Create health plot
plot.food.components(amounts, imagefile = pngHealthFile)