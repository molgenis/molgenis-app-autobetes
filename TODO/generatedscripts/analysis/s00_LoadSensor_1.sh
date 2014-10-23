R --slave <<EOF

library(RCurl, lib.loc='~/libs'); msource <- function(murl = 'http://localhost:8080/molgenis_distro/api/R', verbose = TRUE){if(verbose) cat('Creating connection',murl,'\n'); data <- getURLContent(murl); t <- tempfile(); writeLines(data, con=t); sys.source(t,globalenv()); unlink(t) }; msource()

#
##
### Define constants
##
#
delta.t.grid = 5 # minutes
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

grid = matrix(NA, nrow = ndays, ncol = bin.index.max, dimnames = list(unique.day, 1:bin.index.max))

#
##
### Prepare to fill grid
### bin.left  = 5 * (b - 1) + t.shift, where b is the bin.index = 1 ... 288 = 60 * 24 
### bin.right = 5 * b - 1 + t.shift
##
#
day.minutes = timestamp %% ms.day %/% ms.minute # The number of minutes since 0:00h.

bin.index = day.minutes %/% delta.t.grid + 1
t.shift = day.minutes %% delta.t.grid

#
##
### Fill grid
##
#
for (i in 1:nrow(bgsensor)) grid[as.character(day[i]), bin.index[i]] = bgsensor[i, "value"]

EOF


