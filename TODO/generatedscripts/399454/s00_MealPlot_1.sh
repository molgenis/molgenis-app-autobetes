# Header
mkdir -p /Users/mdijkstra/Documents/pompgemak/molgenis_distro/results/report/399454
R --slave <<EOF

#
##
### Load data and fill bins
##
#

library(RCurl, lib.loc='~/libs'); msource <- function(murl = 'http://localhost:8080/molgenis_distro/api/R', verbose = TRUE){if(verbose) cat('Creating connection',murl,'\n'); data <- getURLContent(murl); t <- tempfile(); writeLines(data, con=t); sys.source(t,globalenv()); unlink(t) }; msource()

carbs.data	= find.carbs()

# Remove zero carbs:
index.keep	= which(0 < carbs.data[,4])
carbs.data	= carbs.data[index.keep, ]

unixtime	= carbs.data[,"unixtime"]
carbs		= carbs.data[,"value"]

# Convert unixtime (milli seconds) to hours on a day (0 ... 23.99)
secondsInDay	= 86400
secondsInHour	=  3600

hour = ((unixtime/1e3) %% secondsInDay) / secondsInHour

# Define bins for histogram
bin.minutes	= 5
bin.size	= bin.minutes / 60

bin.right = seq(bin.size, 24, by = bin.size)	# Right side of bin (bin does not contain this value)
bin.count = rep(0, length(bin.right))			# Grams of carbohydrates eaten in corresponding bin

# Fill bin.right and bin.count for the intervals
for (i in 1 : length(carbs))
{
	index = hour[i] %/% bin.size + 1
	bin.count[index] = bin.count[index] + carbs[i]
}

#
##
### Initialize model [uni + 2 * norm (lunch + dinner; skip breakfast)]
##
#

prop = c(lunch=1/3, dinner=1/3)
prop[3] = 1 - sum(prop) # uniform


EOF

# Footer
