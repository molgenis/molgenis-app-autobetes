rm(list=ls(all=T))
library(RCurl, lib.loc='~/libs'); msource <- function(murl = 'http://localhost:8080/molgenis_distro/api/R', verbose = TRUE){if(verbose) cat('Creating connection',murl,'\n'); data <- getURLContent(murl); t <- tempfile(); writeLines(data, con=t); sys.source(t,globalenv()); unlink(t) }; msource()

#############################################################################################
##################################### Load BG data ##########################################
#############################################################################################
#
##
### Define settings
##
#
delta.t.grid = 5 # minutes

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
bin.index.max = 24 * 60 %/% delta.t.grid

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
bin.index	= day.minutes %/% delta.t.grid + 1
t.shift		= day.minutes %% delta.t.grid # Adding this to the 'grid times' results in realistic times

#
##
### Fill grid << bgsensor
##
#
for (i in 1:nrow(bgsensor)) grid.bgsensor[as.character(day[i]), bin.index[i]] = bgsensor[i, "value"]

#############################################################################################
##################################### Set BG.delta ##########################################
#############################################################################################
#
##
### Define settings
##
#

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
if (any(diff(basal[,"unixtime"]) <= 0)) stop(">> Please sort basal insulin before proceding...")

#
##
### Create grid.insulin
##
#
grid.insulin = matrix(0, nrow = ndays, ncol = bin.index.max, dimnames = list(unique.day, 1:bin.index.max))


#
##
### Fill grid.insulin << basal
##
#














