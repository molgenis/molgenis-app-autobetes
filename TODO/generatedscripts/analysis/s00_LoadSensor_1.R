rm(list=ls(all=T))
library(RCurl, lib.loc='~/libs'); msource <- function(murl = 'http://localhost:8080/molgenis_distro/api/R', verbose = TRUE){if(verbose) cat('Creating connection',murl,'\n'); data <- getURLContent(murl); t <- tempfile(); writeLines(data, con=t); sys.source(t,globalenv()); unlink(t) }; msource()

#
##
### Define settings
##
#
delta.t.grid.bgsensor = 5 # minutes

#
##
### Define constants
##
#
ms.minute = 60 * 1000
ms.hour = 3600 * 1000
ms.day = 24 * ms.hour

#
##
### Load data
##
#
bgsensor = find.bgsensor()
timestamp = bgsensor[,"unixtime"] # time in ms

#
##
### Create grid
##
#
day = timestamp %/% ms.day # day number since 1-1-1970
unique.day = sort(unique(day))
ndays = length(unique.day)
bin.index.max = 24 * 60 %/% delta.t.grid.bgsensor

grid.bgsensor = matrix(NA, nrow = ndays, ncol = bin.index.max, dimnames = list(unique.day, 1:bin.index.max))

#
##
### Prepare to fill grid.bgsensor
### bin.left  = 5 * (b - 1) + t.shift (min since 0:00), where b is the bin.index = 1 ... 288 = 12 * 24 
### bin.right = 5 * b + t.shift (min since 0:00)
### bin.left <= bin < bin.right
##
#
day.minutes	= timestamp %% ms.day %/% ms.minute # The number of minutes since 0:00h.
bin.index	= day.minutes %/% delta.t.grid.bgsensor + 1
t.shift		= day.minutes %% delta.t.grid.bgsensor # Adding this to the 'grid times' results in realistic times

#
##
### Fill grid << bgsensor
##
#
for (i in 1:nrow(bgsensor)) grid.bgsensor[as.character(day[i]), bin.index[i]] = bgsensor[i, "value"]

#############################################################################################
##################################### Load Insulin data #####################################
#############################################################################################
#
##
### Define settings
##
#
insulin.peak					=  60 	# minutes
insulin.95pct					= 120	# minutes
basal.insulin.pulse.frequency	=   1 	# minutes

insulin.root = function(insulin.sigma) { qlnorm(.95, meanlog = log(insulin.peak) + insulin.sigma^2, insulin.sigma) - insulin.95pct }
insulin.sigma.test = 1 : 90 / 100
# plot(insulin.sigma.test, insulin.root(insulin.sigma.test), t='l')
insulin.sigma	= uniroot(insulin.root, range(insulin.sigma.test))$root
insulin.mu		= log(insulin.peak) + insulin.sigma^2
# plot(1:180, dlnorm(1:180, meanlog = insulin.mu, sdlog = insulin.sigma), t='l')

#
##
### Load 'basal' insulin
##
#
basal = find.basal()

# Stop if basal not sorted
if (1 < nrow(basal)) for (i in 2:nrow(basal))
{
	if (!(basal$unixtime[i-1] < basal$unixtime[i]))
	{
		basal = NULL
		stop(">> Please sort basal insulin before proceding...")
	}
}

# Basal values after last sensor measurement are of no use:
# [ 1 ] Strip basal-tail, dependent on bgsensor values
last.bgsensor.unixtime = max(bgsensor[, "unixtime"])
index.remove = which(last.bgsensor.unixtime <= basal$unixtime)
if (0 < length(index.remove)) basal = basal[-index.remove,]

# [ 2 ] Add "basal" that starts at last.bgsensor.unixtime
basal.help = c(id = NA, moment = NA, unixtime = last.bgsensor.unixtime, patternname = NA, profileindex = NA, rate = 0, starttime = NA)
basal = rbind(basal, basal.help)

#
##
### Load basal temp insulin
##
#

#
##
### Combine basal and basal temp into one list
##
#

#
##
### Create grid.insulin
##
#
delta.t.grid.insulin = 5 # minutes

bin.index.max = 24 * 60 %/% delta.t.grid.insulin

grid.insulin = matrix(0, nrow = ndays, ncol = bin.index.max, dimnames = list(unique.day, 1:bin.index.max))


#
##
### Fill grid.insulin << basal
##
#
for (i in 1 : (nrow(basal) - 1))
{
	rate	= basal[i, "rate"]
	from	= basal[i, "unixtime"]
	to		= basal[i + 1, "unixtime"]
	
	grid.insulin[as.character(day[i]), bin.index[i]] = bgsensor[i, "value"]
}


















