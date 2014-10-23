#load RCurl and bitops
library(bitops)
library(RCurl)

#robust sourcing function for URLs
msource <- function(murl = 'http://localhost:8080/api/R', verbose = TRUE){
  if(verbose) cat("Creating connection",murl,"\n")
  data <- getURLContent(murl)
  t <- tempfile()
  writeLines(data, con=t)
  sys.source(t,globalenv())
  unlink(t)
}

if ("x86_64-apple-darwin9.8.0" == version$platform) u = 'http://localhost:8080/api/R' else u = 'http://localhost:8080/voedingsdagboek/api/R'

msource(u)
